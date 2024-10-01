step-1 : setup kafka cluster

download kafka

```bash
wget https://downloads.apache.org/kafka/3.8.0/kafka_2.13-3.8.0.tgz
tar -xzf kafka_2.13-3.8.0.tgz
```

make 3 copies of the kafka folder

```bash
cp -r kafka_2.13-3.8.0 kafka1
cp -r kafka_2.13-3.8.0 kafka2
cp -r kafka_2.13-3.8.0 kafka3
```

add kafka1 to PATH

```bash
export PATH=$PATH:/Users/nag/play-with-sql-nosql-batch2/integration-workshop/kafka1/bin
```

start the zookeeper

```bash
cd kafka1
bin/zookeeper-server-start.sh config/zookeeper.properties
```

start the kafka broker

```bash
cd kafka1
bin/kafka-server-start.sh config/server.properties
```

start the second kafka broker

```bash
cd kafka2
bin/kafka-server-start.sh config/server.properties
```

start the third kafka broker

```bash
cd kafka3
bin/kafka-server-start.sh config/server.properties
```

create a topic user_activities

```bash
cd kafka1
bin/kafka-topics.sh --create --topic user_activities --bootstrap-server localhost:9092 --partitions 3 --replication-factor 3
```

download kafka-ui

```bash
mkdir kafka-ui
cd kafka-ui
wget https://github.com/provectus/kafka-ui/releases/download/v0.7.2/kafka-ui-api-v0.7.2.jar
```

configuration for kafka-ui

```bash
touch application.yml
```

```yaml

kafka:
clusters: - name: local
bootstrapServers: localhost:9092

```

start kafka-ui

```bash
java -Dspring.config.additional-location=/Users/nag/play-with-sql-nosql-batch2/integration-workshop/kafka-ui/application.yml --add-opens java.rmi/javax.rmi.ssl=ALL-UNNAMED -jar /Users/nag/play-with-sql-nosql-batch2/integration-workshop/kafka-ui/kafka-ui-api-v0.7.2.jar
```

download cassandra 4.1.3 tarball from the following link:

```bash
wget https://archive.apache.org/dist/cassandra/4.1.3/apache-cassandra-4.1.3-bin.tar.gz
```

extract the tarball

```bash
tar -xvf apache-cassandra-4.1.3-bin.tar.gz
```

make 3 nodes from the extracted tarball (node1, node2, node3)

```bash
cp -r apache-cassandra-4.1.3 node1
cp -r apache-cassandra-4.1.3 node2
cp -r apache-cassandra-4.1.3 node3
```

mac switch java version 11

```bash
export JAVA_HOME=`/usr/libexec/java_home -v 11`
```

start the first node

```bash
cd node1
bin/cassandra -f
```

start the second node

```bash
cd node2
bin/cassandra -f
```

start the third node

```bash
cd node3
bin/cassandra -f
```

check the status of the cluster

```bash
cd node1
bin/nodetool status
```

create a keyspace

```bash
cd node1
bin/cqlsh
```

```sql
CREATE KEYSPACE my_keyspace WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 3};
```

create a table

```sql
CREATE TABLE my_keyspace.user_activities (
    user_id text,
    activity_time timestamp,
    activity text,
    PRIMARY KEY ((user_id, activity_time))
);
```

---

step-2 : create a kafka producer ( console producer )

create a kafka producer

```bash
cd kafka1
bin/kafka-console-producer.sh --topic user_activities --bootstrap-server localhost:9092
```

send json messages

```json
{"user_id": "user1", "activity_time": "2022-01-01T10:00:00Z", "activity": "login"}
{"user_id": "user1", "activity_time": "2022-02-01T10:00:00Z", "activity": "login"}
{"user_id": "user1", "activity_time": "2022-01-01T10:05:00Z", "activity": "logout"}
{"user_id": "user2", "activity_time": "2022-01-01T10:10:00Z", "activity": "login"}
{"user_id": "user2", "activity_time": "2022-01-01T10:15:00Z", "activity": "logout"}
{"user_id": "user3", "activity_time": "2022-01-01T10:20:00Z", "activity": "login"}
{"user_id": "user3", "activity_time": "2022-01-01T10:25:00Z", "activity": "logout"}
```

---

step-3 : create a kafka consumer ( console consumer )

create a kafka consumer

```bash
cd kafka1
bin/kafka-console-consumer.sh --topic user_activities --bootstrap-server localhost:9092 --from-beginning
```

---

deploy kafka connect cluster ( distributed mode )

start connect worker

```bash
cd kafka1
bin/connect-distributed.sh config/connect-distributed.properties
```

---

list the available connectors

```bash
curl http://localhost:8083/connectors
```

list connctor plugins

```bash
curl http://localhost:8083/connector-plugins
```

using confluentinc-kafka-connect-cassandra-1.2.1 sink connector

deploy the connector

```bash
curl -X POST -H "Content-Type: application/json" --data @cassandra-sink-config.json http://localhost:8083/connectors
```

---

cassandra-sink-config.json

```json
{
  "name": "cassandra-sink-connector",
  "config": {
    "connector.class": "com.datastax.oss.kafka.sink.CassandraSinkConnector",
    "topics": "user_activities",
    "contactPoints": "localhost",
    "loadBalancing.localDc": "datacenter1",
    "topic.user_activities.my_keyspace.user_activities.mapping": "user_id=value.user_id, activity_time=value.activity_time, activity=value.activity",
    "max.retries": "5",
    "retry.interval.ms": "5000",
    "tasks.max": "1",
    "consistencyLevel": "LOCAL_QUORUM",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false"
  }
}
```

combine curl & json

```bash

curl -X POST -H "Content-Type: application/json" --data '{
  "name": "cassandra-sink-connector",
  "config": {
    "connector.class": "com.datastax.oss.kafka.sink.CassandraSinkConnector",
    "topics": "user_activities",
    "contactPoints": "localhost",
    "loadBalancing.localDc": "datacenter1",
    "topic.user_activities.my_keyspace.user_activities.mapping": "user_id=value.user_id, activity_time=value.activity_time, activity=value.activity",
    "max.retries": "5",
    "retry.interval.ms": "5000",
    "tasks.max": "1",
    "consistencyLevel": "LOCAL_QUORUM",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",

    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false"
  }
}' http://localhost:8083/connectors

```

---

create users table with cdc enabled

```sql
CREATE TABLE my_keyspace.users (
    user_id text PRIMARY KEY,
    first_name text,
    last_name text
) WITH cdc = {'enabled': 'true'};
```

---

add node4 to the cluster

```bash
cp -r node1 node4
```
