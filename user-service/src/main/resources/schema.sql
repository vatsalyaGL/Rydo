CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ENUMS
CREATE TYPE user_role AS ENUM ('RIDER', 'DRIVER', 'ADMIN');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION', 'DELETED');

CREATE TYPE vehicle_type AS ENUM ('ECONOMY', 'COMFORT', 'XL', 'BLACK', 'MOTO', 'AUTO');

CREATE TYPE verification_status AS ENUM (
    'PENDING',
    'DOCS_SUBMITTED',
    'UNDER_REVIEW',
    'BACKGROUND_CHECK',
    'VERIFIED',
    'REJECTED'
);

-- USERS
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       phone_number VARCHAR(20) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE,
                       full_name VARCHAR(100) NOT NULL,

                       role user_role NOT NULL,
                       status user_status NOT NULL,

                       rating_avg DECIMAL(3,2) DEFAULT 5.00,
                       rating_count INT DEFAULT 0,

                       preferred_language VARCHAR(10) DEFAULT 'en-US',

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMP
);

-- DRIVER PROFILE
CREATE TABLE driver_profiles (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 user_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,

                                 license_number VARCHAR(50) UNIQUE NOT NULL,
                                 vehicle_make VARCHAR(50) NOT NULL,
                                 vehicle_model VARCHAR(50) NOT NULL,
                                 vehicle_year INT CHECK (vehicle_year >= 2000),
                                 vehicle_color VARCHAR(30) NOT NULL,
                                 vehicle_plate VARCHAR(20) UNIQUE,

                                 vehicle_type vehicle_type NOT NULL,
                                 is_online BOOLEAN DEFAULT FALSE,

                                 verification_status verification_status NOT NULL,
                                 rejection_reason TEXT,

                                 city_id INT NOT NULL,

                                 acceptance_rate DECIMAL(5,2) DEFAULT 100.00 CHECK (acceptance_rate BETWEEN 0 AND 100),
                                 completion_rate DECIMAL(5,2) DEFAULT 100.00 CHECK (completion_rate BETWEEN 0 AND 100)
);