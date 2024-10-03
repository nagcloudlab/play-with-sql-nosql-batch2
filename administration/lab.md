JMX Prometheus Exporter

step-1:
download jmx_prometheus_javaagent jar
https://github.com/prometheus/jmx_exporter/tree/release-1.0.1/docs

download jmx_prometheus_javaagent cassandra.yml
https://github.com/prometheus/jmx_exporter/blob/release-1.0.1/example_configs/cassandra.yml

---

step-2: update cassandra-env.sh ( on each node )

keep in last line

```bash
JVM_OPTS="$JVM_OPTS -javaagent:/path/to/jmx_prometheus_javaagent.jar=<port>:/path/to/cassandra.yml"
```

---

step-3: start cassandra node(s)

---

step-4: download prometheus and configure prometheus.yml

```yaml
scrape_configs:
  - job_name: "cassandra-metrics"
    scrape_interval: 15s
    scrape_timeout: 10s
    static_configs:
      - targets: ["localhost:7071", "localhost:7073"]
```

start prometheus

```bash
./prometheus --config.file=/path/to//prometheus.yml --web.listen-address="localhost:9090"
```

step-5: download grafana and start grafana

```bash
./grafana server
```

- configure prometheus datasource
- import cassandra dashboard ( 12086)

---

create keyspace

```cql
create keyspace test_keyspace with replication = {'class': 'NetworkTopologyStrategy', 'replication_factor': 2};
```

create table

```cql
create table test_keyspace.load_test (
    id uuid primary key,
    name text,
    value text,
    created_at timestamp
);
```

---

start cassandra-stress

```bash
pip install cassandra-driver
```

```python
python write_read.py
```

---
