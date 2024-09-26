package com.example.hotel_service;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.data.UdtValue;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraCustomConversions;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


//----------------------------------------------
// domain / entity classes
//----------------------------------------------


@UserDefinedType("address")
@Data
class Address {
	private String street;
	private String city;
	private String state_or_province;
	private String postal_code;
	private String country;

	// Getters and Setters
}

@Table("hotels")
@Data
class Hotel {

	@PrimaryKey
	private String id;
	private String name;
	private String phone;
	@CassandraType(type = CassandraType.Name.UDT, userTypeName = "address")
	private Address address;
	private Set<String> pois; // Points of Interest

	// Getters and Setters

}


@Table("hotels_by_poi")
@Data
class HotelByPoi {
	@PrimaryKeyColumn(name = "poi_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String poi_name;
	@PrimaryKeyColumn(name = "hotel_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	private String hotel_id;
	private String name;
	private String phone;
	// Getters and Setters
}

//----------------------------------------------
// data access / repository layer
//----------------------------------------------

// way-1 : CassandraTemplate
// way-2 : CassandraRepository

// way-2 : CassandraRepository
interface HotelRepository extends CassandraRepository<Hotel, String> {
	// custom query methods
}

//----------------------------------------------
// service layer
//----------------------------------------------

@Service
class HotelService {

	@Autowired
	private HotelRepository hotelRepository;

	@Autowired
	private CassandraTemplate cassandraTemplate;

	// Write a hotel to the database
	public Hotel saveHotel(Hotel hotel) {
		// write-1 : table : hotels_by_poi
		// Insert into hotels_by_poi table for each POI in the hotel using cassandraTemplate with statement
		for (String poi : hotel.getPois()) {
			SimpleStatement statement = SimpleStatement.builder("INSERT INTO hotels_by_poi (poi_name, hotel_id, name, phone) VALUES (?, ?, ?, ?)")
					.setConsistencyLevel(ConsistencyLevel.ONE)
					.addPositionalValues(poi, hotel.getId(), hotel.getName(), hotel.getPhone())
					.build();
			cassandraTemplate.getCqlOperations().execute(statement);
		}
		// write-2: table : hotels
		return hotelRepository.save(hotel);
	}

	// Read a hotel from the database by ID
	public Hotel getHotelById(String hotelId) {
		return hotelRepository.findById(hotelId).orElse(null);
	}

	// Read a hotel from the database by POI
	public List<HotelByPoi> getHotelsByPoi(String poi) {
		// read-1 : table : hotels_by_poi
		SimpleStatement statement = SimpleStatement.builder("SELECT * FROM hotels_by_poi WHERE poi_name = ?")
				.setConsistencyLevel(ConsistencyLevel.ONE)
				.addPositionalValue(poi)
				.build();
		return cassandraTemplate.select(statement, HotelByPoi.class);
	}

}

//----------------------------------------------
// controller layer ( REST API )
//----------------------------------------------

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "*")
class HotelController {

	@Autowired
	private HotelService hotelService;

	// POST /api/hotels
	@PostMapping
	public Hotel createHotel(@RequestBody Hotel hotel) {
		return hotelService.saveHotel(hotel);
	}

	// GET /api/hotels/{hotelId}
	@GetMapping("/{hotelId}")
	public Hotel getHotel(@PathVariable String hotelId) {
		return hotelService.getHotelById(hotelId);
	}

	// GET /api/hotels/poi/{poi}
	@GetMapping("/poi/{poi}")
	public List<HotelByPoi> getHotelsByPoi(@PathVariable String poi) {
		return hotelService.getHotelsByPoi(poi);
	}

}


//----------------------------------------------
// Configuration
//----------------------------------------------

@Configuration
class HotelServiceConfig {

	// Add your custom configurations here

	@Bean
	public CqlSession cqlSession() {
		return CqlSession.builder()
				.withKeyspace(CqlIdentifier.fromCql("hotel"))
				.build();
	}

	@Bean
	public CassandraMappingContext cassandraMapping(CqlSession cqlSession) throws ClassNotFoundException {
		CassandraMappingContext mappingContext = new CassandraMappingContext();
		mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(cqlSession, CqlIdentifier.fromCql("hotel")));
		return mappingContext;
	}

	@Bean
	public CassandraCustomConversions customConversions(CqlSession cqlSession) {
		List<Object> converters = new ArrayList<>();
		// Get the UDT for 'address'
		com.datastax.oss.driver.api.core.type.UserDefinedType addressUdt = cqlSession.getMetadata()
				.getKeyspace(CqlIdentifier.fromCql("hotel"))
				.flatMap(ks -> ks.getUserDefinedType("address"))
				.orElseThrow(() -> new IllegalArgumentException("address UDT not found"));
		// Add a converter from Address to UdtValue
		converters.add(new org.springframework.core.convert.converter.Converter<Address, UdtValue>() {
			@Override
			public UdtValue convert(Address source) {
				return addressUdt.newValue()
						.setString("street", source.getStreet())
						.setString("city", source.getCity())
						.setString("state_or_province", source.getState_or_province())
						.setString("postal_code", source.getPostal_code())
						.setString("country", source.getCountry());
			}
		});
		return new CassandraCustomConversions(converters);
	}

}


@SpringBootApplication
public class HotelServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelServiceApplication.class, args);
	}

}
