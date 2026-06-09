```mermaid
erDiagram
    USER {
        int id PK
        string name
    }

    HOST {
        int id PK
        string name
        float communication_rating
        float checkin_process_rating
        float cancellation_rate
    }

    ADDRESS {
        int id PK
        string street_address
        string extended_address
        string city
        string state_province
        string postal_code
        string country_code
    }

    STAY {
        int id PK
        string name
        text about
        property_type property_type
        boolean is_refundable
        float star_rating
        int days_from_booking_cancellation_deadline
        text policies_text
        text important_information
        int host_id FK
        int address_id FK
    }

    ROOM {
        int id PK
        int stay_id FK
        string name
        float price
        int sleeps
        int bedroom_amount
        int bathrooms
        float size
    }

    BOOKING {
        int id PK
        int user_id FK
        date check_in_date
        date check_out_date
        booking_status status
        int guests_count
        datetime created_at
    }

    BOOKING_ROOM {
        int booking_id PK, FK
        int room_id PK, FK
    }

    STAY_PICTURE {
        int id PK
        int stay_id FK
        string url "server-assigned path to uploaded file"
        string caption
        boolean is_primary "unique per stay"
        int display_order
    }

    REVIEW {
        int id PK
        text text
        int user_id FK
        int stay_id FK
    }

    LANGUAGE {
        int id PK
        string language_name
    }

    HOST_LANGUAGE {
        int host_id PK, FK
        int language_id PK, FK
    }

    VIEW {
        int id PK
        string view_type
    }

    STAY_VIEW {
        int stay_id PK, FK
        int view_id PK, FK
    }

    AMENITY {
        int id PK
        string name
        amenity_type type
    }

    STAY_AMENITY {
        int stay_id PK, FK
        int amenity_id PK, FK
    }

    ACCESSIBILITY {
        int id PK
        string accessibility_type
    }

    STAY_ACCESSIBILITY {
        int stay_id PK, FK
        int accessibility_id PK, FK
    }

    MEAL_PLAN {
        int id PK
        string meal_plan_type
    }

    PROPERTY_BRAND {
        int id PK
        string brand_name
    }

    STAY_PROPERTY_BRAND {
        int stay_id PK, FK
        int brand_id PK, FK
    }

    TRAVELER_EXPERIENCE {
        int id PK
        string traveler_experience_type
    }

    STAY_TRAVELER_EXPERIENCE {
        int stay_id PK, FK
        int traveler_experience_id PK, FK
    }

    STAY_MEAL_PLAN {
        int stay_id PK, FK
        int meal_plan_id PK, FK
    }

    PAYMENT_TYPE {
        int id PK
        string payment_type
    }

    STAY_PAYMENT_TYPE {
        int stay_id PK, FK
        int payment_type_id PK, FK
    }

    USER_FAVORITE {
        int user_id PK, FK
        int stay_id PK, FK
        datetime created_at
    }

    %% Relationships
    HOST ||--o{ STAY : "hosts"
    ADDRESS ||--|| STAY : "located_at"
    STAY ||--|{ ROOM : "contains"
    USER ||--o{ BOOKING : "makes"
    BOOKING ||--|{ BOOKING_ROOM : "includes"
    ROOM ||--o{ BOOKING_ROOM : "booked_in"
    USER ||--o{ REVIEW : "writes"
    STAY ||--o{ REVIEW : "receives"
    STAY ||--o{ STAY_PICTURE : "has"

    USER ||--o{ USER_FAVORITE : "saves"
    STAY ||--o{ USER_FAVORITE : "favored_by"

    HOST ||--o{ HOST_LANGUAGE : "speaks"
    LANGUAGE ||--o{ HOST_LANGUAGE : "spoken_by"

    STAY ||--o{ STAY_VIEW : "features"
    VIEW ||--o{ STAY_VIEW : "seen_in"

    STAY ||--o{ STAY_AMENITY : "includes"
    AMENITY ||--o{ STAY_AMENITY : "provided_in"

    STAY ||--o{ STAY_ACCESSIBILITY : "supports"
    ACCESSIBILITY ||--o{ STAY_ACCESSIBILITY : "adapted_for"

    STAY ||--o{ STAY_MEAL_PLAN : "offers"
    MEAL_PLAN ||--o{ STAY_MEAL_PLAN : "included_in"

    STAY ||--o{ STAY_PAYMENT_TYPE : "accepts"
    PAYMENT_TYPE ||--o{ STAY_PAYMENT_TYPE : "used_for"

    STAY ||--o{ STAY_PROPERTY_BRAND : "branded_as"
    PROPERTY_BRAND ||--o{ STAY_PROPERTY_BRAND : "brands"

    STAY ||--o{ STAY_TRAVELER_EXPERIENCE : "offers"
    TRAVELER_EXPERIENCE ||--o{ STAY_TRAVELER_EXPERIENCE : "included_in"
```
