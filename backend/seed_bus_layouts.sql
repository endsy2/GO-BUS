-- ============================================================================
-- Bus Layout Seed Data
-- ============================================================================
-- This script creates sample bus layouts for different bus types
-- Schema: bus_service
-- ============================================================================

-- Standard 40-Seat Bus Layout (2-2 configuration)
INSERT INTO bus_service."BusLayout" (name, layout, description, "createdAt")
VALUES (
    'Standard 40-Seat (2-2)',
    '[
        {"row": 1, "seats": ["1A", "1B", null, "1C", "1D"]},
        {"row": 2, "seats": ["2A", "2B", null, "2C", "2D"]},
        {"row": 3, "seats": ["3A", "3B", null, "3C", "3D"]},
        {"row": 4, "seats": ["4A", "4B", null, "4C", "4D"]},
        {"row": 5, "seats": ["5A", "5B", null, "5C", "5D"]},
        {"row": 6, "seats": ["6A", "6B", null, "6C", "6D"]},
        {"row": 7, "seats": ["7A", "7B", null, "7C", "7D"]},
        {"row": 8, "seats": ["8A", "8B", null, "8C", "8D"]},
        {"row": 9, "seats": ["9A", "9B", null, "9C", "9D"]},
        {"row": 10, "seats": ["10A", "10B", null, "10C", "10D"]}
    ]',
    'Standard bus layout with 40 seats in 2-2 configuration. Ideal for regular intercity routes with comfortable seating.',
    NOW()
);

-- VIP 36-Seat Bus Layout (2-1 configuration)
INSERT INTO bus_service."BusLayout" (name, layout, description, "createdAt")
VALUES (
    'VIP 36-Seat (2-1)',
    '[
        {"row": 1, "seats": ["1A", "1B", null, "1C"]},
        {"row": 2, "seats": ["2A", "2B", null, "2C"]},
        {"row": 3, "seats": ["3A", "3B", null, "3C"]},
        {"row": 4, "seats": ["4A", "4B", null, "4C"]},
        {"row": 5, "seats": ["5A", "5B", null, "5C"]},
        {"row": 6, "seats": ["6A", "6B", null, "6C"]},
        {"row": 7, "seats": ["7A", "7B", null, "7C"]},
        {"row": 8, "seats": ["8A", "8B", null, "8C"]},
        {"row": 9, "seats": ["9A", "9B", null, "9C"]},
        {"row": 10, "seats": ["10A", "10B", null, "10C"]},
        {"row": 11, "seats": ["11A", "11B", null, "11C"]},
        {"row": 12, "seats": ["12A", "12B", null, "12C"]}
    ]',
    'VIP bus layout with 36 seats in 2-1 configuration. Extra legroom and wider seats for premium comfort.',
    NOW()
);

-- Sleeper 24-Bed Bus Layout (1-1 configuration)
INSERT INTO bus_service."BusLayout" (name, layout, description, "createdAt")
VALUES (
    'Sleeper 24-Bed (1-1)',
    '[
        {"row": 1, "seats": ["1L", null, "1R"]},
        {"row": 2, "seats": ["2L", null, "2R"]},
        {"row": 3, "seats": ["3L", null, "3R"]},
        {"row": 4, "seats": ["4L", null, "4R"]},
        {"row": 5, "seats": ["5L", null, "5R"]},
        {"row": 6, "seats": ["6L", null, "6R"]},
        {"row": 7, "seats": ["7L", null, "7R"]},
        {"row": 8, "seats": ["8L", null, "8R"]},
        {"row": 9, "seats": ["9L", null, "9R"]},
        {"row": 10, "seats": ["10L", null, "10R"]},
        {"row": 11, "seats": ["11L", null, "11R"]},
        {"row": 12, "seats": ["12L", null, "12R"]}
    ]',
    'Sleeper bus with 24 beds in 1-1 configuration. Perfect for overnight long-distance journeys with full reclining beds.',
    NOW()
);

-- Luxury 28-Seat Bus Layout (2-1 configuration with extra space)
INSERT INTO bus_service."BusLayout" (name, layout, description, "createdAt")
VALUES (
    'Luxury 28-Seat (2-1)',
    '[
        {"row": 1, "seats": ["1A", "1B", null, "1C"]},
        {"row": 2, "seats": ["2A", "2B", null, "2C"]},
        {"row": 3, "seats": ["3A", "3B", null, "3C"]},
        {"row": 4, "seats": ["4A", "4B", null, "4C"]},
        {"row": 5, "seats": ["5A", "5B", null, "5C"]},
        {"row": 6, "seats": ["6A", "6B", null, "6C"]},
        {"row": 7, "seats": ["7A", "7B", null, "7C"]},
        {"row": 8, "seats": ["8A", "8B", null, "8C"]},
        {"row": 9, "seats": ["9A", "9B", null, "9C"]},
        {"row": 10, "seats": ["10A", null, null, "10C"]}
    ]',
    'Luxury bus with 28 spacious seats in 2-1 configuration. Premium amenities with maximum comfort and extra legroom.',
    NOW()
);

-- Mini Bus 16-Seat Layout (2-2 configuration)
INSERT INTO bus_service."BusLayout" (name, layout, description, "createdAt")
VALUES (
    'Mini Bus 16-Seat (2-2)',
    '[
        {"row": 1, "seats": ["1A", "1B", null, "1C", "1D"]},
        {"row": 2, "seats": ["2A", "2B", null, "2C", "2D"]},
        {"row": 3, "seats": ["3A", "3B", null, "3C", "3D"]},
        {"row": 4, "seats": ["4A", "4B", null, "4C", "4D"]}
    ]',
    'Compact mini bus with 16 seats in 2-2 configuration. Ideal for short routes and small group transportation.',
    NOW()
);

-- Double Decker 60-Seat Layout (2-3 configuration)
INSERT INTO bus_service."BusLayout" (name, layout, description, "createdAt")
VALUES (
    'Double Decker 60-Seat (2-3)',
    '[
        {"row": 1, "seats": ["1A", "1B", null, "1C", "1D", "1E"]},
        {"row": 2, "seats": ["2A", "2B", null, "2C", "2D", "2E"]},
        {"row": 3, "seats": ["3A", "3B", null, "3C", "3D", "3E"]},
        {"row": 4, "seats": ["4A", "4B", null, "4C", "4D", "4E"]},
        {"row": 5, "seats": ["5A", "5B", null, "5C", "5D", "5E"]},
        {"row": 6, "seats": ["6A", "6B", null, "6C", "6D", "6E"]},
        {"row": 7, "seats": ["7A", "7B", null, "7C", "7D", "7E"]},
        {"row": 8, "seats": ["8A", "8B", null, "8C", "8D", "8E"]},
        {"row": 9, "seats": ["9A", "9B", null, "9C", "9D", "9E"]},
        {"row": 10, "seats": ["10A", "10B", null, "10C", "10D", "10E"]}
    ]',
    'Double decker bus with 60 seats in 2-3 configuration. High capacity for busy routes with panoramic upper deck views.',
    NOW()
);

-- Semi-Sleeper 32-Seat Layout (2-2 configuration with recline)
INSERT INTO bus_service."BusLayout" (name, layout, description, "createdAt")
VALUES (
    'Semi-Sleeper 32-Seat (2-2)',
    '[
        {"row": 1, "seats": ["1A", "1B", null, "1C", "1D"]},
        {"row": 2, "seats": ["2A", "2B", null, "2C", "2D"]},
        {"row": 3, "seats": ["3A", "3B", null, "3C", "3D"]},
        {"row": 4, "seats": ["4A", "4B", null, "4C", "4D"]},
        {"row": 5, "seats": ["5A", "5B", null, "5C", "5D"]},
        {"row": 6, "seats": ["6A", "6B", null, "6C", "6D"]},
        {"row": 7, "seats": ["7A", "7B", null, "7C", "7D"]},
        {"row": 8, "seats": ["8A", "8B", null, "8C", "8D"]}
    ]',
    'Semi-sleeper bus with 32 reclining seats in 2-2 configuration. Comfortable for medium to long-distance travel.',
    NOW()
);

-- Executive 20-Seat Layout (1-2 configuration)
INSERT INTO bus_service."BusLayout" (name, layout, description, "createdAt")
VALUES (
    'Executive 20-Seat (1-2)',
    '[
        {"row": 1, "seats": ["1A", null, "1B", "1C"]},
        {"row": 2, "seats": ["2A", null, "2B", "2C"]},
        {"row": 3, "seats": ["3A", null, "3B", "3C"]},
        {"row": 4, "seats": ["4A", null, "4B", "4C"]},
        {"row": 5, "seats": ["5A", null, "5B", "5C"]},
        {"row": 6, "seats": ["6A", null, "6B", "6C"]},
        {"row": 7, "seats": ["7A", null, "7B", "7C"]},
        {"row": 8, "seats": ["8A", null, "8B", "8C"]},
        {"row": 9, "seats": ["9A", null, "9B", "9C"]},
        {"row": 10, "seats": ["10A", null, "10B", "10C"]}
    ]',
    'Executive class bus with 20 premium seats in 1-2 configuration. Business class comfort with luxury amenities.',
    NOW()
);

-- ============================================================================
-- Verification Query
-- ============================================================================
-- SELECT id, name, "createdAt" FROM bus_service."BusLayout" ORDER BY id;
