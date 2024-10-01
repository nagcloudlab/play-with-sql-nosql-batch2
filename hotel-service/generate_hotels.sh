#!/bin/bash

# Array of random names, streets, cities, states, and points of interest
HOTEL_NAMES=("The Grand Hotel" "Ocean View" "Mountain Inn" "City Palace" "Desert Oasis")
STREETS=("123 Main St" "456 Ocean Ave" "789 Mountain Dr" "1011 City Blvd" "202 Desert Rd")
CITIES=("New York" "Los Angeles" "San Francisco" "Chicago" "Miami")
STATES=("NY" "CA" "IL" "FL" "TX")
COUNTRIES=("USA" "Canada" "Mexico")
POIS=("Central Park" "Times Square" "Golden Gate Bridge" "Millennium Park" "Miami Beach" "Hollywood" "Niagara Falls")

# Function to get a random number in a range
rand() {
    awk -v min=$1 -v max=$2 'BEGIN{srand(); print int(min+rand()*(max-min+1))}'
}

# Loop indefinitely to generate random hotels
while true; do
    # Generate a random UUID for the hotel ID
    HOTEL_ID=$(uuidgen)
    
    # Pick random elements from the arrays
    HOTEL_NAME=${HOTEL_NAMES[$(rand 0 $((${#HOTEL_NAMES[@]} - 1)))]}
    STREET=${STREETS[$(rand 0 $((${#STREETS[@]} - 1)))]}
    CITY=${CITIES[$(rand 0 $((${#CITIES[@]} - 1)))]}
    STATE=${STATES[$(rand 0 $((${#STATES[@]} - 1)))]}
    COUNTRY=${COUNTRIES[$(rand 0 $((${#COUNTRIES[@]} - 1)))]}
    POSTAL_CODE=$(rand 10000 99999)
    PHONE="1-$(rand 100 999)-$(rand 100 999)-$(rand 1000 9999)"
    
    # Select 1 to 3 random POIs
    NUM_POIS=$(rand 1 3)
    SELECTED_POIS=$(printf '%s\n' "${POIS[@]}" | awk 'BEGIN{srand()}{print rand(), $0}' | sort -n | cut -d' ' -f2- | head -n $NUM_POIS | jq -R . | jq -s .)

    # Prepare the JSON data for the POST request
    JSON_DATA=$(jq -n \
    --arg id "hotel_$HOTEL_ID" \
    --arg name "$HOTEL_NAME" \
    --arg phone "$PHONE" \
    --arg street "$STREET" \
    --arg city "$CITY" \
    --arg state_or_province "$STATE" \
    --arg postal_code "$POSTAL_CODE" \
    --arg country "$COUNTRY" \
    --argjson pois "$SELECTED_POIS" \
    '{
        id: $id,
        name: $name,
        phone: $phone,
        address: {
            street: $street,
            city: $city,
            state_or_province: $state_or_province,
            postal_code: $postal_code,
            country: $country
        },
        pois: $pois
    }')

    # Make the POST request
    curl -s -X POST http://localhost:8181/api/hotels \
    -H "Content-Type: application/json" \
    -d "$JSON_DATA" | jq

    
    
done
