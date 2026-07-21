# Domain Model (Class Diagrams)

One diagram per service, reflecting the actual current JPA entity classes (`src/main/kotlin/.../models/`). Cross-service references (`hostId`, `userId`, `roomIds`, `stayId`, `ownerId`) are plain `Int`/`Set<Int>` fields, not object references — no service holds a live JPA relation into another service's tables (docs/adr/0011).

## identity-service

```mermaid
classDiagram
    class User {
        +Int id
        +String name
        +String? email
        -String? passwordHash
        +String? phone
        +String? profilePictureUrl
        +LocalDateTime? deletedAt
    }
    class Host {
        +Int id
        +BigDecimal? communicationRating
        +BigDecimal? checkinProcessRating
        +BigDecimal? cancellationRate
        +Set~Language~ languages
    }
    class Language {
        +Int id
        +String languageName
    }
    class PaymentMethod {
        +Int id
        +Int userId
        +String stripePaymentMethodId
        +String brand
        +String lastFour
        +String type
        +Int expiryMonth
        +Int expiryYear
        +Boolean isDefault
    }

    User "1" --> "0..1" Host : shares id
    User "1" --> "0..*" PaymentMethod : userId
    Host "0..*" --> "0..*" Language : host_language
```

`User.passwordHash` is `@JsonIgnore` — never serialized. `Host.id` is not auto-generated; it's the same value as the `User.id` it subtypes.

## inventory-service

```mermaid
classDiagram
    class Stay {
        +Int id
        +String name
        +String? about
        +PropertyType propertyType
        +Boolean isRefundable
        +BigDecimal? starRating
        +Int? daysFromBookingCancellationDeadline
        +String? policiesText
        +String? importantInformation
        +Int hostId
        +Point? location
    }
    class PropertyType {
        <<enumeration>>
        HOME
        HOTEL
    }
    class Room {
        +Int id
        +Int stayId
        +String name
        +BigDecimal price
        +Int sleeps
        +Int bedroomAmount
        +BigDecimal bathrooms
        +BigDecimal? size
    }
    class Address {
        +Int id
        +String streetAddress
        +String? extendedAddress
        +String city
        +String? stateProvince
        +String? postalCode
        +String countryCode
    }
    class Region {
        +Int id
        +String city
        +String countryCode
        +String? stateProvince
        +Int? curatedRank
    }
    class PropertyBrand {
        +Int id
        +String brandName
    }
    class View {
        +Int id
        +String viewType
    }
    class Amenity {
        +Int id
        +String name
        +String type
    }
    class Accessibility {
        +Int id
        +String accessibilityType
    }
    class MealPlan {
        +Int id
        +String mealPlanType
    }
    class PaymentType {
        +Int id
        +String paymentType
    }
    class TravelerExperience {
        +Int id
        +String travelerExperienceType
    }

    Stay "1" --> "1" Address : address
    Address "0..*" --> "1" Region : region
    Stay "0..*" --> "0..1" PropertyBrand : propertyBrand
    Stay "1" --> "0..*" Room : stayId
    Stay "0..*" --> "0..*" View : views
    Stay "0..*" --> "0..*" Amenity : amenities
    Stay "0..*" --> "0..*" Accessibility : accessibilities
    Stay "0..*" --> "0..*" MealPlan : mealPlans
    Stay "0..*" --> "0..*" PaymentType : paymentTypes
    Stay "0..*" --> "0..*" TravelerExperience : travelerExperiences
    Stay --> PropertyType
```

`Stay.hostId` is a plain `Int`, not a JPA relation — host existence is Feign-checked against identity-service, not joined. `Room.stayId` is likewise a plain column with a real intra-service `@ManyToOne`-style FK, unlike `hostId`. `Address.region` is a real `@ManyToOne` (docs/adr/0018) — the stable identifier for a (city, country_code) pair, resolved or created by `StayService.findOrCreateRegion()`.

## booking-service

```mermaid
classDiagram
    class Booking {
        +Int id
        +Int userId
        +LocalDate checkInDate
        +LocalDate checkOutDate
        +BookingStatus status
        +Int guestsCount
        +LocalDateTime createdAt
        +BigDecimal totalPrice
        +Set~Int~ roomIds
    }
    class BookingStatus {
        <<enumeration>>
        PENDING
        CONFIRMED
        CANCELLED
        COMPLETED
    }

    Booking --> BookingStatus
```

`userId` and `roomIds` are plain `Int`/`Set<Int>` — no `User` or `Room` class exists in this service at all. `roomIds` is an `@ElementCollection` backed by the `booking_room` table, not an object graph.

## review-service

```mermaid
classDiagram
    class Review {
        +Int id
        +String text
        +Int userId
        +Int stayId
        +Int rating
    }
```

Single entity, no relations — `userId`/`stayId` are plain columns.

## media-service

```mermaid
classDiagram
    class Media {
        +Int id
        +MediaOwnerType ownerType
        +Int ownerId
        +String url
        +String? caption
        +Boolean isPrimary
        +Int displayOrder
    }
    class MediaOwnerType {
        <<enumeration>>
        STAY
        ROOM
        USER
    }

    Media --> MediaOwnerType
```

One polymorphic entity for all picture ownership (`ownerType` + `ownerId`) — replaces the old monolith's separate `StayPicture`/`RoomPicture` classes (docs/adr/0003).
