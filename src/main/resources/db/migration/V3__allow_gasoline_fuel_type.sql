ALTER TABLE vehicles
    DROP CONSTRAINT chk_vehicles_fuel_type;

ALTER TABLE vehicles
    ADD CONSTRAINT chk_vehicles_fuel_type
        CHECK (
            fuel_type IN (
                'GASOLINE',
                'DIESEL',
                'LPG',
                'CNG',
                'HYBRID',
                'ELECTRIC',
                'OTHER'
            )
        );
