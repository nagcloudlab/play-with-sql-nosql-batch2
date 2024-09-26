
USE hotel;

# Load test data for the table containing available room data
COPY available_rooms_by_hotel_date FROM '~/cassandra-guide/resources/available_rooms.csv' WITH HEADER=true;

SELECT * FROM available_rooms_by_hotel_date;

#
# WHERE clause examples
#

# Search for hotel rooms for a specific hotel
SELECT * FROM available_rooms_by_hotel_date WHERE hotel_id='AZ123';

# Search for hotel rooms for a specific hotel and date range:
SELECT * FROM available_rooms_by_hotel_date WHERE hotel_id='AZ123' and date<'2016-01-12' and date>'2016-01-05';

# Search for hotel rooms for a specific hotel and date range and room number
SELECT * FROM available_rooms_by_hotel_date WHERE hotel_id='AZ123' and date='2016-01-12' and room_number=101;

# Why doesn't this query work?
SELECT * FROM available_rooms_by_hotel_date WHERE hotel_id='AZ123' and room_number=101;

# Look at the table again
DESCRIBE TABLE available_rooms_by_hotel_date;

# We can force it to work, but why is this not a good practice?
SELECT * FROM available_rooms_by_hotel_date WHERE date='2016-01-25' ALLOW FILTERING;


#
# IN clause example
#

# Use the IN clause to test equality with multiple possible values for a column
# Find inventory on two dates a week apart
SELECT * FROM available_rooms_by_hotel_date WHERE hotel_id='AZ123' AND date IN ('2016-01-05', '2016-01-12');

#
# SORT clause example
#

# Override the default sort order on the table
SELECT * FROM available_rooms_by_hotel_date
  WHERE hotel_id='AZ123' AND date>'2016-01-05' AND date<'2016-01-12'
  ORDER BY date DESC;


