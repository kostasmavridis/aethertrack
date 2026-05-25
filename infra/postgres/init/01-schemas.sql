-- Create schemas used by each service.
-- Flyway within each service creates its own tables inside the relevant schema.

CREATE SCHEMA IF NOT EXISTS supplement;
CREATE SCHEMA IF NOT EXISTS scheduling;
CREATE SCHEMA IF NOT EXISTS fhir;
CREATE SCHEMA IF NOT EXISTS intake;

-- Grant full access to the application user (same credentials for all services in dev).
GRANT ALL PRIVILEGES ON SCHEMA supplement  TO aethertrack;
GRANT ALL PRIVILEGES ON SCHEMA scheduling  TO aethertrack;
GRANT ALL PRIVILEGES ON SCHEMA fhir        TO aethertrack;
GRANT ALL PRIVILEGES ON SCHEMA intake      TO aethertrack;
