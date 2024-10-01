import time
import uuid
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider

# Set up Cassandra connection (Update with your cluster information if needed)
cluster = Cluster(['127.0.0.1'])  # Replace with your node IPs
session = cluster.connect()

# Use the created keyspace
session.set_keyspace('test_keyspace')

# Prepare the insert statement
insert_stmt = session.prepare("""
    INSERT INTO load_test (id, name, value, created_at)
    VALUES (?, ?, ?, ?)
""")

# Simulate write load
def simulate_writes(n):
    for _ in range(n):
        session.execute(
            insert_stmt,
            (uuid.uuid4(), 'SampleName', 'SampleValue', int(time.time() * 1000))
        )
    print(f"Inserted {n} rows.")

# Simulate read load
def simulate_reads(n):
    rows = session.execute('SELECT * FROM load_test LIMIT %s', (n,))
    count = 0
    for row in rows:
        print(row)
        count += 1
    print(f"Read {count} rows.")

# Configuration for simulation
num_writes = 10000  # Number of writes to perform
num_reads = 1000    # Number of reads to perform

# Run simulation
start_time = time.time()
simulate_writes(num_writes)
print(f"Time for writing {num_writes} rows: {time.time() - start_time} seconds")

start_time = time.time()
simulate_reads(num_reads)
print(f"Time for reading {num_reads} rows: {time.time() - start_time} seconds")

# Close session and cluster connection
session.shutdown()
cluster.shutdown()
