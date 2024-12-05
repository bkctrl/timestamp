-- Table for users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    pfp VARCHAR(255) NOT NULL,
    travel_mode VARCHAR(10) NOT NULL CHECK (travel_mode IN ('Car', 'Foot', 'Bike')),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table for events
CREATE TABLE IF NOT EXISTS events (
    id SERIAL PRIMARY KEY,  -- Auto-incremented ID for the event
    creator VARCHAR(255) NOT NULL,  -- FK to users table
    name VARCHAR(255) NOT NULL,
    description TEXT,  -- Description of the event
    address VARCHAR(255) NOT NULL,  -- Event location address
    latitude DOUBLE PRECISION NOT NULL,  -- Event location latitude
    longitude DOUBLE PRECISION NOT NULL, -- Event location longitude
    arrival TIMESTAMP WITH TIME ZONE NOT NULL,  -- Time when the event starts
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Auto-timestamp when created
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  -- Auto-timestamp when updated
);

-- Table for the many-to-many relationship between users and events
CREATE TABLE IF NOT EXISTS user_events (
     user_id VARCHAR(255),  -- FK to users table
     event_id BIGINT NOT NULL,  -- FK to events table
     time_est BIGINT DEFAULT NULL,
     distance DOUBLE PRECISION DEFAULT NULL,
     arrived BOOLEAN NOT NULL DEFAULT FALSE,  -- Whether the user has arrived at the event
     time_arrived TIMESTAMP WITH TIME ZONE DEFAULT NULL,
     created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
     PRIMARY KEY (user_id, event_id),
     CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
     CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

-- Table for the many-to-one relationship between Arrivals and Events
CREATE TABLE IF NOT EXISTS arrivals (
    id SERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

-- Table for the many-to-one relationship between EventLinks and Events
CREATE TABLE IF NOT EXISTS event_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);