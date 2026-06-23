# project-lab-backend

GraphQL API backend for a lodging platform supporting home rentals and hotels. Built with Kotlin, Spring Boot 4, and PostgreSQL.

---

## Requirements

- Java 21
- Maven (or use the included `./mvnw` wrapper)
- Docker & Docker Compose

---

## Running with Docker Compose (recommended)

1. Fill in the required environment variables:

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
| `JWT_SECRET` | Secret key used to sign JWTs — use a random string of at least 64 characters in production |

2. Start the stack:

```bash
docker compose up --build
```

---

## API Reference Docs

A static HTML reference site is generated from the GraphQL schema using [SpectaQL](https://github.com/anvilco/spectaql). The pre-built output lives in `docs/api/` and is always committed alongside schema changes.

### Viewing the docs

**Option 1 — VS Code Live Server (recommended)**

1. Install the [Live Server](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer) extension in VS Code.
2. Right-click `docs/api/index.html` in the Explorer panel and choose **Open with Live Server**.
3. The docs open in your browser at `http://127.0.0.1:5500/docs/api/index.html`.

Live Server hot-reloads whenever you regenerate — useful while editing the schema.

**Option 2 — open directly (WSL2 / Windows)**

```bash
explorer.exe docs/api/index.html
```

**Option 3 — any HTTP server**

```bash
npx serve docs/api
```

### Regenerating after schema changes

Whenever you edit a `.graphqls` file, regenerate the docs before committing:

```bash
pnpm run docs
```

This reads all schema files under `src/main/resources/graphql/` and overwrites `docs/api/`. No running server is required.

> Node.js and pnpm are required. Run `pnpm install` first if you haven't already.

---

## API Overview

The API is **GraphQL-first**. All queries and mutations go through a single endpoint:

```
POST http://localhost:<SPRING_PORT>/graphql
```

An in-browser IDE (GraphiQL) is available at:

```
http://localhost:<SPRING_PORT>/graphiql
```

**One exception**: picture file uploads use a dedicated REST endpoint (see [File Upload](#file-upload)).

---

## Scalars

| Scalar | Java type | Wire format |
|---|---|---|
| `Date` | `LocalDate` | `"YYYY-MM-DD"` |
| `DateTime` | `LocalDateTime` | `"YYYY-MM-DDTHH:MM:SS"` |
| `BigDecimal` | `BigDecimal` | JSON number |

---

## Enums

```graphql
enum PropertyType   { HOTEL HOME }
enum BookingStatus  { PENDING CONFIRMED CANCELLED COMPLETED }
enum AmenityType    { ROOM_AMENITY PROPERTY_AMENITY }
```

---

## Types

### AuthPayload
Returned by `login` and `signup`.
```graphql
type AuthPayload {
    token: String!   # JWT — include as Authorization: Bearer <token>
    user:  User!
}
```

### User
```graphql
type User {
    id:    Int!
    name:  String!
    email: String   # null for users created before auth was added
}
```

### Host
A host profile linked to an existing user by the same `id`.
```graphql
type Host {
    id: Int!
    communicationRating:  BigDecimal   # 0–100
    checkinProcessRating: BigDecimal   # 0–100
    cancellationRate:     BigDecimal   # 0–100
    languages: [Language!]!
}
```

### Stay
```graphql
type Stay {
    id:                               Int!
    name:                             String!
    about:                            String
    propertyType:                     PropertyType!
    isRefundable:                     Boolean!
    starRating:                       BigDecimal
    daysFromBookingCancellationDeadline: Int
    policiesText:                     String
    importantInformation:             String
    host:                             Host!
    propertyBrand:                    PropertyBrand
    address:                          Address!
    rooms:                            [Room!]!
    pictures:                         [StayPicture!]!
    amenities:                        [Amenity!]!
    views:                            [View!]!
    accessibilities:                  [Accessibility!]!
    mealPlans:                        [MealPlan!]!
    paymentTypes:                     [PaymentType!]!
    travelerExperiences:              [TravelerExperience!]!
    startingFromPrice:                BigDecimal   # lowest room price; null if no rooms
}
```

### Address
```graphql
type Address {
    id:              Int!
    streetAddress:   String!
    extendedAddress: String
    city:            String!
    stateProvince:   String
    postalCode:      String
    countryCode:     String!   # ISO 3166-1 alpha-2
}
```

### Room
```graphql
type Room {
    id:            Int!
    stayId:        Int!
    name:          String!
    price:         BigDecimal!
    sleeps:        Int!
    bedroomAmount: Int!
    bathrooms:     BigDecimal!
    size:          BigDecimal    # floor area in m²; optional
}
```

### StayPicture
```graphql
type StayPicture {
    id:           Int!
    stayId:       Int!
    url:          String!     # server-relative path
    caption:      String
    isPrimary:    Boolean!
    displayOrder: Int!
}
```

### Booking
```graphql
type Booking {
    id:           Int!
    user:         User!
    checkInDate:  Date!
    checkOutDate: Date!
    status:       BookingStatus!
    guestsCount:  Int!
    createdAt:    DateTime!
    rooms:        [Room!]!
}
```

### Review
```graphql
type Review {
    id:     Int!
    text:   String!
    userId: Int!
    stayId: Int!
}
```

### Lookup types

All lookup types follow the same minimal shape:

```graphql
type Amenity            { id: Int!  name: String!                   type: AmenityType! }
type Language           { id: Int!  languageName: String! }
type Accessibility      { id: Int!  accessibilityType: String! }
type View               { id: Int!  viewType: String! }
type PaymentType        { id: Int!  paymentType: String! }
type MealPlan           { id: Int!  mealPlanType: String! }
type PropertyBrand      { id: Int!  brandName: String! }
type TravelerExperience { id: Int!  travelerExperienceType: String! }
```

---

## Queries

### Users

```graphql
users: [User!]!
user(id: Int!): User
```

**Example**
```graphql
query {
  users { id name }
  user(id: 1) { id name }
}
```

---

### Hosts

```graphql
hosts: [Host!]!
host(id: Int!): Host
```

**Example**
```graphql
query {
  host(id: 1) {
    id
    communicationRating
    languages { id languageName }
  }
}
```

---

### Stays

```graphql
stays(page: Int = 0, size: Int = 20): [Stay!]!
stay(id: Int!): Stay
```

Pagination is offset/limit: `page=0, size=20` returns the first 20 results.

**Example — list with nested data**
```graphql
query {
  stays(page: 0, size: 10) {
    id
    name
    propertyType
    startingFromPrice
    address { city countryCode }
    host { communicationRating }
    amenities { name type }
  }
}
```

**Example — single stay**
```graphql
query {
  stay(id: 3) {
    id name about isRefundable starRating
    address { streetAddress city stateProvince countryCode }
    rooms { id name price sleeps }
    pictures { url isPrimary }
    host { id communicationRating languages { languageName } }
    amenities { name }
    views { viewType }
    paymentTypes { paymentType }
  }
}
```

---

### Rooms

```graphql
rooms(stayId: Int!, page: Int = 0, size: Int = 20): [Room!]!
room(id: Int!): Room
availableRooms(stayId: Int!, checkIn: Date!, checkOut: Date!): [Room!]!
```

`availableRooms` returns rooms with no conflicting active booking (`PENDING` or `CONFIRMED`) for the requested window. An empty list means the stay is fully booked for those dates.

**Example**
```graphql
query {
  availableRooms(stayId: 2, checkIn: "2026-08-01", checkOut: "2026-08-07") {
    id name price sleeps
  }
}
```

---

### Bookings

```graphql
bookings(page: Int = 0, size: Int = 20): [Booking!]!
booking(id: Int!): Booking
```

**Example**
```graphql
query {
  booking(id: 5) {
    id status checkInDate checkOutDate guestsCount
    user { id name }
    rooms { id name stayId }
  }
}
```

---

### Reviews

```graphql
reviews(page: Int = 0, size: Int = 20): [Review!]!
```

---

### Stay Pictures

```graphql
stayPictures(stayId: Int!): [StayPicture!]!
```

---

### Lookup tables

```graphql
amenities: [Amenity!]!
amenity(id: Int!): Amenity

languages: [Language!]!
accessibilities: [Accessibility!]!
views: [View!]!
paymentTypes: [PaymentType!]!
mealPlans: [MealPlan!]!

propertyBrands: [PropertyBrand!]!
propertyBrand(id: Int!): PropertyBrand

travelerExperiences: [TravelerExperience!]!
```

---

## Authentication

Protected mutations require a JWT in the `Authorization` header:

```
Authorization: Bearer <token>
```

Obtain a token with `login` or `signup`:

```graphql
mutation {
  signup(name: "Alice", email: "alice@example.com", password: "s3cr3t") {
    token
    user { id name email }
  }
}
```

```graphql
mutation {
  login(email: "alice@example.com", password: "s3cr3t") {
    token
    user { id name }
  }
}
```

Tokens are valid for 24 hours. An expired or missing token on a protected mutation returns:

```json
{
  "errors": [{ "message": "authentication required", "extensions": { "classification": "UNAUTHORIZED" } }]
}
```

All mutations are protected except `login`, `signup`, and `createUser`.

---

## Mutations

### Users

```graphql
createUser(input: CreateUserInput!): User!
updateUser(id: Int!, input: UpdateUserInput!): User!
deleteUser(id: Int!): Boolean!
```

**Inputs**
```graphql
input CreateUserInput { name: String! }
input UpdateUserInput { name: String! }
```

**Example**
```graphql
mutation {
  createUser(input: { name: "Alice Johnson" }) { id name }
}
```

---

### Hosts

```graphql
createHost(input: CreateHostInput!): Host!
updateHost(id: Int!, input: UpdateHostInput!): Host!
deleteHost(id: Int!): Boolean!
```

**Inputs**
```graphql
input CreateHostInput {
    id:                   Int!         # must match an existing user id
    communicationRating:  BigDecimal   # 0–100, optional
    checkinProcessRating: BigDecimal   # 0–100, optional
    cancellationRate:     BigDecimal   # 0–100, optional
    languageIds:          [Int!]! = []
}

input UpdateHostInput {
    communicationRating:  BigDecimal
    checkinProcessRating: BigDecimal
    cancellationRate:     BigDecimal
    languageIds:          [Int!]! = []
}
```

**Example**
```graphql
mutation {
  createHost(input: {
    id: 1
    communicationRating: 95.0
    languageIds: [1, 2]
  }) {
    id communicationRating languages { languageName }
  }
}
```

---

### Stays

```graphql
createStay(input: CreateStayInput!): Stay!
updateStay(id: Int!, input: UpdateStayInput!): Stay!
deleteStay(id: Int!): Boolean!
```

**Input** (`CreateStayInput` and `UpdateStayInput` are identical)
```graphql
input CreateStayInput {
    name:                             String!
    about:                            String
    propertyType:                     PropertyType!
    address:                          AddressInput!
    isRefundable:                     Boolean! = false
    starRating:                       BigDecimal
    daysFromBookingCancellationDeadline: Int
    policiesText:                     String
    importantInformation:             String
    hostId:                           Int!
    propertyBrandId:                  Int
    viewIds:              [Int!]! = []
    amenityIds:           [Int!]! = []
    accessibilityIds:     [Int!]! = []
    mealPlanIds:          [Int!]! = []
    paymentTypeIds:       [Int!]! = []
    travelerExperienceIds:[Int!]! = []
}

input AddressInput {
    streetAddress:   String!
    extendedAddress: String
    city:            String!
    stateProvince:   String
    postalCode:      String
    countryCode:     String!
}
```

**Example**
```graphql
mutation {
  createStay(input: {
    name: "Cozy Beachfront House"
    propertyType: HOME
    hostId: 1
    isRefundable: true
    address: {
      streetAddress: "123 Ocean Drive"
      city: "Miami"
      countryCode: "US"
    }
    amenityIds: [1, 2]
    viewIds: [3]
  }) {
    id name propertyType
    address { streetAddress city }
  }
}
```

---

### Rooms

```graphql
createRoom(stayId: Int!, input: CreateRoomInput!): Room!
updateRoom(id: Int!, input: UpdateRoomInput!): Room!
deleteRoom(id: Int!): Boolean!
```

**Input** (`CreateRoomInput` and `UpdateRoomInput` are identical)
```graphql
input CreateRoomInput {
    name:          String!
    price:         BigDecimal!  # must be >= 0
    sleeps:        Int!         # must be >= 1
    bedroomAmount: Int!         # must be >= 0
    bathrooms:     BigDecimal!  # must be >= 0
    size:          BigDecimal   # floor area in m², optional; must be >= 0
}
```

**Example**
```graphql
mutation {
  createRoom(stayId: 3, input: {
    name: "King Suite"
    price: 180.00
    sleeps: 2
    bedroomAmount: 1
    bathrooms: 1.5
    size: 42.0
  }) {
    id name price sleeps
  }
}
```

---

### Bookings

```graphql
createBooking(input: CreateBookingInput!): Booking!
updateBookingStatus(id: Int!, status: BookingStatus!): Booking!
deleteBooking(id: Int!): Boolean!
```

> **Requires authentication** — include `Authorization: Bearer <token>`. The booking is created for the authenticated user; no `userId` is needed in the input.

**Input**
```graphql
input CreateBookingInput {
    checkInDate:  Date!
    checkOutDate: Date!
    guestsCount:  Int!
    roomIds:      [Int!]!
}
```

**Booking rules**
- `checkInDate` must not be in the past and must be within 6 months from today.
- `checkOutDate` must be after `checkInDate`.
- `roomIds` must not be empty; all rooms must belong to the same stay.
- None of the requested rooms may have a `PENDING` or `CONFIRMED` booking that overlaps the requested window.
- `guestsCount` must not exceed the combined `sleeps` capacity of the requested rooms.
- New bookings are always created with status `PENDING`.

**Example — create**
```graphql
mutation {
  createBooking(input: {
    checkInDate: "2026-09-01"
    checkOutDate: "2026-09-05"
    guestsCount: 2
    roomIds: [7]
  }) {
    id status checkInDate checkOutDate
    rooms { id name }
    user { name }
  }
}
```

**Example — status update**
```graphql
mutation {
  updateBookingStatus(id: 5, status: CONFIRMED) {
    id status
  }
}
```

---

### Reviews

```graphql
createReview(input: CreateReviewInput!): Review!
updateReview(id: Int!, input: UpdateReviewInput!): Review!
deleteReview(id: Int!): Boolean!
```

**Input** (`CreateReviewInput` and `UpdateReviewInput` are identical)
```graphql
input CreateReviewInput {
    text:   String!   # must not be blank
    userId: Int!
    stayId: Int!
}
```

**Example**
```graphql
mutation {
  createReview(input: {
    text: "Wonderful stay, highly recommend!"
    userId: 2
    stayId: 1
  }) { id text }
}
```

---

### Stay Pictures (metadata only)

Metadata for existing pictures can be updated via GraphQL. File upload uses the REST endpoint below.

```graphql
updateStayPicture(stayId: Int!, id: Int!, input: UpdateStayPictureInput!): StayPicture!
deleteStayPicture(stayId: Int!, id: Int!): Boolean!
```

```graphql
input UpdateStayPictureInput {
    caption:      String
    isPrimary:    Boolean! = false
    displayOrder: Int! = 0
}
```

**Example**
```graphql
mutation {
  updateStayPicture(stayId: 1, id: 4, input: {
    caption: "Sunset from the terrace"
    isPrimary: false
    displayOrder: 2
  }) { id caption isPrimary displayOrder }
}
```

---

### Lookup table mutations

All eight lookup tables follow the same pattern:

```graphql
create<Type>(input: Create<Type>Input!): <Type>!
update<Type>(id: Int!, input: Update<Type>Input!): <Type>!
delete<Type>(id: Int!): Boolean!
```

| Type | Input field |
|---|---|
| `Amenity` | `name: String!`, `type: AmenityType!` |
| `Language` | `languageName: String!` |
| `Accessibility` | `accessibilityType: String!` |
| `View` | `viewType: String!` |
| `PaymentType` | `paymentType: String!` |
| `MealPlan` | `mealPlanType: String!` |
| `PropertyBrand` | `brandName: String!` |
| `TravelerExperience` | `travelerExperienceType: String!` |

**Example**
```graphql
mutation {
  createAmenity(input: { name: "High-Speed Wi-Fi", type: PROPERTY_AMENITY }) {
    id name type
  }
  createLanguage(input: { languageName: "Spanish" }) { id languageName }
}
```

---

## File Upload

Picture files are uploaded via a REST endpoint — the only non-GraphQL endpoint in the API.

### `POST /api/v1/stays/{stayId}/pictures`

Content type: `multipart/form-data`

| Field | Type | Required | Notes |
|---|---|---|---|
| `file` | binary | yes | Must be an image (`image/*`). Max 10 MB. |
| `caption` | string | no | Alt text / caption |
| `isPrimary` | boolean | no | Defaults to `false`. At most one primary per stay. |
| `displayOrder` | integer | no | Defaults to `0`. Must be >= 0. |

**Response 201**
```json
{
  "id": 4,
  "stayId": 1,
  "url": "/uploads/stays/1/3f2a1b4c-uuid.jpg",
  "caption": "Ocean-facing exterior",
  "isPrimary": true,
  "displayOrder": 0
}
```

`url` is a server-relative path. Prepend the API host to construct the full URL.

**curl example**
```bash
curl -X POST http://localhost:8080/api/v1/stays/1/pictures \
  -F "file=@exterior.jpg" \
  -F "caption=Ocean-facing exterior" \
  -F "isPrimary=true" \
  -F "displayOrder=0"
```

---

## Error Handling

All GraphQL errors are returned in the standard `errors` array with a `extensions.classification` field:

| HTTP concept | `classification` value |
|---|---|
| 400 Bad Request | `BAD_REQUEST` |
| 409 Conflict (e.g. duplicate email) | `BAD_REQUEST` |
| 404 Not Found | `NOT_FOUND` |
| 403 Forbidden | `FORBIDDEN` |
| 401 Unauthorized | `UNAUTHORIZED` |
| 500 Internal | `INTERNAL_ERROR` |

**Example error response**
```json
{
  "errors": [
    {
      "message": "stay not found",
      "locations": [{ "line": 2, "column": 3 }],
      "path": ["stay"],
      "extensions": { "classification": "NOT_FOUND" }
    }
  ],
  "data": { "stay": null }
}
```

Partial success is possible: if one field in a query fails (e.g. a batch-loaded relationship), only that field is `null` and other fields still resolve normally.
