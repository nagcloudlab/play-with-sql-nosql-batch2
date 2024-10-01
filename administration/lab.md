create keyspace

```cql
create keyspace test_keyspace with replication = {'class': 'NetworkTopologyStrategy', 'replication_factor': 2};
```

create table

name : load_test
id, name, value, created_at

```cql
create table test_keyspace.load_test (
    id uuid primary key,
    name text,
    value text,
    created_at timestamp
);
```

select by uuid = 40b79446-4ed0-4ff4-b718-8c01ce0643e5

```cql
select * from test_keyspace.load_test where id = 40b79446-4ed0-4ff4-b718-8c01ce0643e5;
```
