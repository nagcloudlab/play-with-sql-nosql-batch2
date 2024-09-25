download cassandra tarball

```bash
wget https://dlcdn.apache.org/cassandra/5.0.0/apache-cassandra-5.0.0-bin.tar.gz
```

extract tarball

```bash
tar -xvzf apache-cassandra-5.0.0-bin.tar.gz
```

make 3 copies of the extracted folder (node1, node2, node3)

```bash
cp -r apache-cassandra-5.0.0 node1
cp -r apache-cassandra-5.0.0 node2
cp -r apache-cassandra-5.0.0 node3
```

start the first node

```bash
cd node1
./bin/cassandra -f
```

verify the first node is running

```bash
./bin/nodetool status
```

start the second node

cassandra.yaml

```yaml
storage_port: 7002
ssl_storage_port: 7003
native_transport_port: 9043
```

cassandra.env.sh

at line - 224

```bash
JMX_PORT="7200"
```

start the second node

```bash
cd node2
./bin/cassandra -f
```

verify the second node is running

```bash
./bin/nodetool status
```

start the third node

cassandra.yaml

```yaml
storage_port: 7004
ssl_storage_port: 7005
native_transport_port: 9044
```

cassandra.env.sh

at line - 224

```bash
JMX_PORT="7201"
```

start the third node

```bash
cd node3
./bin/cassandra -f
```

verify the third node is running

```bash
./bin/nodetool status
```
