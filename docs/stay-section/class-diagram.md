> **Superseded**: this describes the pre-migration monolithic schema. The database
> is now split one-per-service; see each service's own
> `src/main/resources/db/migration/` for the current schema
> (docs/adr/0011-database-per-service.md).

```mermaid
classDiagram
    direction LR

    %% --- Base Class ---
    class USER {
        +int id
        +string name
    }

    %% --- Subclass (Inheritance) ---
    class HOST {
        +float communication_rating
        +float checkin_process_rating
        +float cancellation_rate
    }

    class ADDRESS {
        +int id
        +string street_address
        +string extended_address
        +string city
        +string state_province
        +string postal_code
        +string country_code
    }

    class STAY {
        +int id
        +string name
        +string about
        +property_type property_type
        +boolean is_refundable
        +float star_rating
        +int days_from_booking_cancellation_deadline
        +string policies_text
        +string important_information
        +geography location
    }

    class ROOM {
        +int id
        +string name
        +float price
        +int sleeps
        +int bedroom_amount
        +int bathrooms
        +float size
    }

    class BOOKING {
        +int id
        +date check_in_date
        +date check_out_date
        +booking_status status
        +int guests_count
        +datetime created_at
        +decimal total_price
    }

    class STAY_PICTURE {
        +int id
        +string url
        +string caption
        +boolean is_primary
        +int display_order
        %% url is a server-assigned path, e.g. /uploads/stays/1/uuid.jpg
        %% pictures are uploaded as files; at most one is_primary per stay
    }

    class REVIEW {
        +int id
        +string text
        +int rating
    }

    class LANGUAGE {
        +int id
        +string language_name
    }

    class VIEW {
        +int id
        +string view_type
    }

    class AMENITY {
        +int id
        +string name
        +amenity_type type
    }

    class ACCESSIBILITY {
        +int id
        +string accessibility_type
    }

    class MEAL_PLAN {
        +int id
        +string meal_plan_type
    }

    class PAYMENT_TYPE {
        +int id
        +string payment_type
    }

    class PROPERTY_BRAND {
        +int id
        +string brand_name
    }

    class TRAVELER_EXPERIENCE {
        +int id
        +string traveler_experience_type
    }

    %% --- Inheritance Relationship ---
    USER <|-- HOST : "is a"

    %% --- Core Operational Connections ---
    HOST "1" --> "0..*" STAY : hosts
    STAY "1" --> "1..*" ROOM : contains
    USER "1" --> "0..*" BOOKING : makes
    BOOKING "*" o-- "*" ROOM : includes
    USER "1" --> "0..*" REVIEW : writes
    STAY "1" --> "0..*" REVIEW : receives
    STAY "1" --> "1" ADDRESS : has
    STAY "1" --> "0..*" STAY_PICTURE : has

    %% --- Many-to-Many Connections ---
    USER "*" o-- "*" STAY : favorites
    HOST "*" o-- "*" LANGUAGE : speaks
    STAY "*" o-- "*" VIEW : features
    STAY "*" o-- "*" AMENITY : includes
    STAY "*" o-- "*" ACCESSIBILITY : supports
    STAY "*" o-- "*" MEAL_PLAN : offers
    STAY "*" o-- "*" PAYMENT_TYPE : accepts
    STAY "*" o-- "*" PROPERTY_BRAND : branded_as
    STAY "*" o-- "*" TRAVELER_EXPERIENCE : offers
```
