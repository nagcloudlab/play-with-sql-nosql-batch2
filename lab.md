deploy multi-dc cassandra cluster

```bash
docker-compose -up -d
```

Check Cluster Status with nodetool

```bash
docker-compose exec cassandra-1 nodetool status
docker-compose exec cassandra-1 nodetool gossipinfo
docker-compose exec cassandra-1 nodetool describecluster
docker-compose exec cassandra-1 nodetool ring
docker-compose exec cassandra-1 nodetool repair
```

Check Cluster Status with cqlsh

```bash
docker-compose exec cassandra-1 cqlsh
```

```sql
SELECT peer, data_center, rack FROM system.peers;
```

Using the Multi-DC Cluster

```bash
docker-compose exec cassandra-1 cqlsh
```

```sql
CREATE KEYSPACE my_keyspace WITH REPLICATION = {'class': 'NetworkTopologyStrategy', 'DC1': 2, 'DC2': 2};
```

Specify Consistency Levels Based on DC Requirements:

```sql
CONSISTENCY
CONSISTENCY LOCAL_QUORUM;
```
