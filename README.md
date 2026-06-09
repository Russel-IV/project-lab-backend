# project-lab-backend

REST API backend for a lodging platform supporting home rentals and hotels. Built with Kotlin, Spring Boot 4, and PostgreSQL.

---

## Requirements

- Java 21
- Maven (or use the included `./mvnw` wrapper)
- Docker & Docker Compose

---

## Instructions

### Running with Docker Compose (recommended)

1. Fill in the required environment values:

Required variables:

| Variable | Description |
|---|---|
| `SPRING_PORT` | Host port the API is exposed on (e.g. `8080`) |
| `SPRING_PROFILES_ACTIVE` | Spring profile (e.g. `dev`) |
| `POSTGRES_HOST` | Database host (`project-lab-database` when using compose) |
| `POSTGRES_PORT` | Database port (e.g. `5432`) |
| `POSTGRES_DB` | Database name |
| `POSTGRES_USER` | Database user |
| `POSTGRES_PASSWORD` | Database password |
| `POSTGRES_SSL_MODE` | SSL mode (e.g. `disable`) |
| `UPLOAD_DIR` | Absolute path where uploaded pictures are stored (e.g. `/app/uploads`) |

2. Start the stack:

```
docker compose up --build
```

The API will be available at `http://localhost:<SPRING_PORT>`.  
The database initialises automatically from the SQL files in `db/`.

---

## Endpoints

All endpoints are prefixed with `/api/v1` unless noted otherwise.  
Dates use the `YYYY-MM-DD` format. Omitted optional fields can be left out of the request body entirely.

---

### Users

#### `GET /api/v1/users`
Returns all users.

**Response 200**
```json
[
  { "id": 1, "name": "Alice Johnson" }
]
```

---

#### `POST /api/v1/users`
Creates a user.

**Request**
```json
{ "name": "Alice Johnson" }
```

**Response 201**
```json
{ "id": 1, "name": "Alice Johnson" }
```

---

#### `PUT /api/v1/users/{id}`
Updates a user.

**Request**
```json
{ "name": "Alice Smith" }
```

**Response 200**
```json
{ "id": 1, "name": "Alice Smith" }
```

---

#### `DELETE /api/v1/users/{id}`
Deletes a user. **Response 204**

---

### Hosts

A host is a user with hosting ratings. Use an existing `user.id` as the host `id`.

#### `GET /api/v1/hosts`
Returns all hosts.

**Response 200**
```json
[
  {
    "id": 1,
    "communicationRating": 98.5,
    "checkinProcessRating": 95.0,
    "cancellationRate": 2.1,
    "languageIds": [1, 2]
  }
]
```

---

#### `GET /api/v1/hosts/{id}`
Returns a host by id. **Response 200** — same shape as above.

---

#### `POST /api/v1/hosts`
Creates a host profile linked to an existing user.

**Request**
```json
{
  "id": 1,
  "communicationRating": 98.5,
  "checkinProcessRating": 95.0,
  "cancellationRate": 2.1,
  "languageIds": [1, 2]
}
```

**Response 201** — same shape as GET response.

---

#### `PUT /api/v1/hosts/{id}`
Updates a host profile. Same request body as POST. **Response 200**

---

#### `DELETE /api/v1/hosts/{id}`
Deletes a host profile. **Response 204**

---

### Stays

A stay is a property — either a `HOME` (single bookable unit) or a `HOTEL` (multiple rooms).

#### `GET /api/v1/stays`
Returns all stays with their rooms, pictures, and address embedded.

**Response 200**
```json
[
  {
    "id": 1,
    "name": "Cozy Beachfront House",
    "about": "Beautiful house right next to the shore.",
    "propertyType": "HOME",
    "isRefundable": true,
    "starRating": 4.5,
    "daysFromBookingCancellationDeadline": 5,
    "policiesText": "No pets allowed.",
    "importantInformation": "Check-in after 3 PM.",
    "hostId": 1,
    "propertyBrandId": 1,
    "address": {
      "id": 1,
      "streetAddress": "123 Ocean Drive",
      "extendedAddress": "Apt 4B",
      "city": "Miami",
      "stateProvince": "Florida",
      "postalCode": "33139",
      "countryCode": "US"
    },
    "rooms": [
      {
        "id": 1,
        "stayId": 1,
        "name": "Beachfront Suite",
        "price": 120.50,
        "sleeps": 4,
        "bedroomAmount": 2,
        "bathrooms": 1.5,
        "size": 85.0
      }
    ],
    "pictures": [
      {
        "id": 1,
        "stayId": 1,
        "url": "https://cdn.example.com/stays/1/exterior.jpg",
        "caption": "Ocean-facing exterior",
        "isPrimary": true,
        "displayOrder": 0
      }
    ],
    "startingFromPrice": 120.50,
    "viewIds": [1],
    "amenityIds": [1, 2],
    "accessibilityIds": [1],
    "mealPlanIds": [1],
    "paymentTypeIds": [1, 2],
    "travelerExperienceIds": [1]
  }
]
```

---

#### `GET /api/v1/stays/{id}`
Returns a stay by id. **Response 200** — same shape as above.

---

#### `POST /api/v1/stays`
Creates a stay. Rooms and pictures are managed separately after creation.

**Request**
```json
{
  "name": "Cozy Beachfront House",
  "about": "Beautiful house right next to the shore.",
  "propertyType": "HOME",
  "isRefundable": true,
  "starRating": 4.5,
  "daysFromBookingCancellationDeadline": 5,
  "policiesText": "No pets allowed.",
  "importantInformation": "Check-in after 3 PM.",
  "hostId": 1,
  "propertyBrandId": 1,
  "address": {
    "streetAddress": "123 Ocean Drive",
    "extendedAddress": "Apt 4B",
    "city": "Miami",
    "stateProvince": "Florida",
    "postalCode": "33139",
    "countryCode": "US"
  },
  "viewIds": [1],
  "amenityIds": [1, 2],
  "accessibilityIds": [1],
  "mealPlanIds": [1],
  "paymentTypeIds": [1, 2],
  "travelerExperienceIds": [1]
}
```

**Response 201** — same shape as GET response.

---

#### `PUT /api/v1/stays/{id}`
Replaces a stay. Same request body as POST. **Response 200**

---

#### `DELETE /api/v1/stays/{id}`
Deletes a stay and its address, rooms, and pictures. **Response 204**

---

### Rooms

Rooms are the bookable units within a stay. Every HOME has exactly one room; HOTELs can have many.

#### `GET /api/v1/stays/{stayId}/rooms`
Returns all rooms for a stay.

**Response 200**
```json
[
  {
    "id": 2,
    "stayId": 2,
    "name": "Standard King",
    "price": 350.00,
    "sleeps": 2,
    "bedroomAmount": 1,
    "bathrooms": 1.0,
    "size": 45.5
  }
]
```

---

#### `POST /api/v1/stays/{stayId}/rooms`
Adds a room to a stay.

**Request**
```json
{
  "name": "Standard King",
  "price": 350.00,
  "sleeps": 2,
  "bedroomAmount": 1,
  "bathrooms": 1.0,
  "size": 45.5
}
```

**Response 201** — same shape as single room above.

---

#### `GET /api/v1/rooms/{id}`
Returns a room by id. **Response 200**

---

#### `PUT /api/v1/rooms/{id}`
Updates a room. Same request body as POST. **Response 200**

---

#### `DELETE /api/v1/rooms/{id}`
Deletes a room. **Response 204**

---

#### `GET /api/v1/stays/{stayId}/availability?checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD`
Returns rooms within the stay that have no conflicting active booking for the requested window.

**Response 200**
```json
[
  {
    "id": 2,
    "stayId": 2,
    "name": "Standard King",
    "price": 350.00,
    "sleeps": 2,
    "bedroomAmount": 1,
    "bathrooms": 1.0,
    "size": 45.5
  }
]
```

An empty array means the stay is fully booked for those dates.

---

### Stay Pictures

#### `GET /api/v1/stays/{stayId}/pictures`
Returns all pictures for a stay.

**Response 200**
```json
[
  {
    "id": 1,
    "stayId": 1,
    "url": "https://cdn.example.com/stays/1/exterior.jpg",
    "caption": "Ocean-facing exterior",
    "isPrimary": true,
    "displayOrder": 0
  }
]
```

---

#### `POST /api/v1/stays/{stayId}/pictures`
Uploads a picture for a stay. At most one picture per stay may have `isPrimary: true`.

Request is `multipart/form-data`:

| Field | Type | Required | Description |
|---|---|---|---|
| `file` | image file | yes | JPEG, PNG, WebP, etc. Max 10 MB |
| `caption` | string | no | Alt text / caption |
| `isPrimary` | boolean | no | Defaults to `false` |
| `displayOrder` | integer | no | Defaults to `0` |

**Response 201**
```json
{
  "id": 1,
  "stayId": 1,
  "url": "/uploads/stays/1/3f2a1b4c-....jpg",
  "caption": "Open-plan living area",
  "isPrimary": false,
  "displayOrder": 1
}
```

The `url` field is a server-relative path. Prepend the API host to get the full URL.  
Uploaded files are served at `GET /uploads/stays/{stayId}/{filename}`.

---

#### `PUT /api/v1/stays/{stayId}/pictures/{id}`
Updates a picture's metadata and optionally replaces the file. Same `multipart/form-data` format as POST; omit `file` to keep the existing image. **Response 200**

---

#### `DELETE /api/v1/stays/{stayId}/pictures/{id}`
Deletes a picture. **Response 204**

---

### Bookings

A booking reserves one or more rooms for the same stay over a continuous date range. Check-in must be today or later and no more than 6 months out. All rooms must belong to the same stay.

#### `GET /api/v1/bookings`
Returns all bookings.

**Response 200**
```json
[
  {
    "id": 1,
    "userId": 2,
    "checkInDate": "2026-07-15",
    "checkOutDate": "2026-07-20",
    "status": "CONFIRMED",
    "guestsCount": 2,
    "createdAt": "2026-06-08T10:30:00",
    "roomIds": [1]
  }
]
```

---

#### `GET /api/v1/bookings/{id}`
Returns a booking by id. **Response 200** — same shape as above.

---

#### `POST /api/v1/bookings`
Creates a booking. Status is set to `PENDING` automatically.

**Request**
```json
{
  "userId": 2,
  "checkInDate": "2026-07-15",
  "checkOutDate": "2026-07-20",
  "guestsCount": 2,
  "roomIds": [1]
}
```

**Response 201** — same shape as GET response.

To book multiple rooms in the same hotel:
```json
{
  "userId": 2,
  "checkInDate": "2026-09-10",
  "checkOutDate": "2026-09-14",
  "guestsCount": 6,
  "roomIds": [2, 3]
}
```

---

#### `PATCH /api/v1/bookings/{id}/status`
Updates the status of a booking. Valid values: `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`.

**Request**
```json
{ "status": "CONFIRMED" }
```

**Response 200** — full booking object with updated status.

---

#### `DELETE /api/v1/bookings/{id}`
Deletes a booking. **Response 204**

---

### Lookup Tables

These endpoints manage reference data (amenity types, languages, etc.). All follow the same pattern: `GET` all, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`.

#### Amenities — `/api/v1/amenities`
`type` is either `ROOM_AMENITY` or `PROPERTY_AMENITY`.

```json
// Request
{ "name": "High-Speed Wi-Fi", "type": "PROPERTY_AMENITY" }
// Response
{ "id": 1, "name": "High-Speed Wi-Fi", "type": "PROPERTY_AMENITY" }
```

#### Accessibility — `/accessibility`

```json
// Request
{ "accessibilityType": "Wheelchair Accessible Path" }
// Response
{ "id": 1, "accessibilityType": "Wheelchair Accessible Path" }
```

#### Languages — `/api/v1/languages`

```json
// Request
{ "languageName": "English" }
// Response
{ "id": 1, "languageName": "English" }
```

#### Views — `/api/v1/views`

```json
// Request
{ "viewType": "Ocean View" }
// Response
{ "id": 1, "viewType": "Ocean View" }
```

#### Meal Plans — `/api/v1/meal-plans`

```json
// Request
{ "mealPlanType": "Breakfast Included" }
// Response
{ "id": 1, "mealPlanType": "Breakfast Included" }
```

#### Payment Types — `/api/v1/payment-types`

```json
// Request
{ "paymentType": "Credit Card" }
// Response
{ "id": 1, "paymentType": "Credit Card" }
```

#### Property Brands — `/api/v1/property-brands`

```json
// Request
{ "brandName": "Marriott International" }
// Response
{ "id": 1, "brandName": "Marriott International" }
```

#### Traveler Experiences — `/api/v1/traveler-experiences`

```json
// Request
{ "travelerExperienceType": "Family Friendly" }
// Response
{ "id": 1, "travelerExperienceType": "Family Friendly" }
```
