import time
import uuid
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider

# Set up Cassandra connection (Update with your cluster information if needed)
cluster = Cluster(['127.0.0.1'])  # Replace with your node IPs
session = cluster.connect()

# Use the created keyspace
session.set_keyspace('test_keyspace')

# Prepare the insert and select statements
insert_stmt = session.prepare("""
    INSERT INTO load_test (id, name, value, created_at)
    VALUES (?, ?, ?, ?)
""")

select_stmt = session.prepare("""
    SELECT * FROM load_test WHERE id = ?
""")

# Generate a batch of unique keys
def generate_keys(n):
    return [uuid.uuid4() for _ in range(n)]

# Function to perform write operations
def simulate_writes(keys):
    for key in keys:
        session.execute(
            insert_stmt,
            (key, 'SampleName', 'SampleValue', int(time.time() * 1000))
        )
    print(f"Inserted {len(keys)} rows.")

# Function to perform read operations
def simulate_reads(keys):
    count = 0
    for key in keys:
        rows = session.execute(select_stmt, (key,))
        for row in rows:
            print(row)
            count += 1
    print(f"Read {count} rows.")

# Configuration for simulation
num_operations = 1000000  # Number of insert-read operations to perform

# Generate keys
primary_keys = generate_keys(num_operations)

# Run write simulation
start_time = time.time()
simulate_writes(primary_keys)
print(f"Time for writing {num_operations} rows: {time.time() - start_time} seconds")

# Run read simulation
start_time = time.time()
simulate_reads(primary_keys)
print(f"Time for reading {num_operations} rows: {time.time() - start_time} seconds")

# Close session and cluster connection
session.shutdown()
cluster.shutdown()
