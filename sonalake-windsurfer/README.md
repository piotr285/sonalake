# 🏄 Worldwide Windsurfer's Weather Service

A Spring Boot 3 / Java 21 REST API that recommends the best windsurfing location for any day within the 7-day forecast window, powered by the [Weatherbit Forecast API](https://www.weatherbit.io/api/weather-forecast-16-day).

---

## Requirements

- Java 21+
- Maven 3.9+
- A free [Weatherbit API key](https://www.weatherbit.io/account/create)

---

## Quick Start

Set your API key in environment variables in Run/Debug configurations
WEATHERBIT_API_KEY=[key_goes_here]

The service starts locally on **http://localhost:8080**.

---

## API

### `GET /api/best-spot?date=YYYY-MM-DD`

Returns the best windsurfing location for a given date.

**Rules**
- `date` must be in `yyyy-MM-dd` format
- `date` must be within the next 7 days (today inclusive)

**Example**
```
GET http://localhost:8080/api/best-spot?date=2025-06-15
```

**Response `200 OK`**
```json
{
  "date": "2025-06-15",
  "best_location": {
    "name": "Le Morne",
    "country": "Mauritius",
    "score": 68.0,
    "weather": {
      "avg_temp_celsius": 24.5,
      "wind_speed_ms": 11.2
    }
  }
}
```

**Error responses** follow [RFC 7807 Problem Details](https://datatracker.ietf.org/doc/html/rfc7807):

| Status | Reason |
|---|---|
| `400 Bad Request` | Missing/invalid date, date outside 7-day window |
| `404 Not Found` | No forecast data for that date at any location |
| `502 Bad Gateway` | Weatherbit API call failed (invalid key, network error) |

---

### `GET /api/locations`

Returns all registered windsurfing locations.

```json
{
  "locations": [
    { "id": "jastarnia",  "name": "Jastarnia",  "country": "Poland",    "latitude": 54.6961,  "longitude": 18.6786  },
    { "id": "bridgetown", "name": "Bridgetown", "country": "Barbados",  "latitude": 13.0975,  "longitude": -59.6167 },
    { "id": "fortaleza",  "name": "Fortaleza",  "country": "Brazil",    "latitude": -3.7172,  "longitude": -38.5433 },
    { "id": "pissouri",   "name": "Pissouri",   "country": "Cyprus",    "latitude": 34.6693,  "longitude": 32.7007  },
    { "id": "le_morne",   "name": "Le Morne",   "country": "Mauritius", "latitude": -20.4500, "longitude": 57.3167  }
  ]
}
```

---

### Adding a new location

Open `src/main/resources/application.yml` and add a new entry to the list:

```yaml
windsurfing:
  locations:
    - id: jastarnia
      name: Jastarnia
      country: Poland
      latitude: 54.6961
      longitude: 18.6786
    - id: new_location          # unique identifier (no spaces)
      name: City Name           # display name
      country: Country Name     # country
      latitude: 12.3456         # latitude in decimal degrees
      longitude: 78.9012        # longitude in decimal degrees
```
---

