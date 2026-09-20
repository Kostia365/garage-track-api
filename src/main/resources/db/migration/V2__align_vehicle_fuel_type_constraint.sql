ALTER TABLE vehicles
    DROP CONSTRAINT chk_vehicles_fuel_type;

UPDATE vehicles
SET fuel_type = 'OTHER'
WHERE fuel_type = 'PETROL';

ALTER TABLE vehicles
    ADD CONSTRAINT chk_vehicles_fuel_type
        CHECK (
            fuel_type IN (
                          'DIESEL',
                          'LPG',
                          'CNG',
                          'HYBRID',
                          'ELECTRIC',
                          'OTHER'
                )
            );
