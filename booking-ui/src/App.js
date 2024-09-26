import "./App.css";

import React, { useState } from "react";

function App() {
  const [hotelsByPoi, setHotelsByPoi] = useState([]);

  const handleKeyUp = (e) => {
    if (e.key === "Enter") {
      const poi = e.target.value;
      fetch(`http://localhost:8080/api/hotels/poi/${poi}`)
        .then((response) => response.json())
        .then((data) => {
          setHotelsByPoi(data || []);
        });
    }
  };

  return (
    <div className="container">
      <div className="display-1">booking UI</div>
      <hr />
      <div className="row">
        <div className="col-6">
          <input
            type="text"
            className="form-control"
            placeholder="enter poi"
            onKeyUp={handleKeyUp}
          />
        </div>
      </div>
      <hr />
      <div className="row">
        <div className="col-8">
          <ul className="list-group">
            {hotelsByPoi.map((hotel) => (
              <li key={hotel.hotel_id}>{hotel.name}</li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

export default App;
