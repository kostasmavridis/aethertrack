-- ============================================================
-- V2 – Seed a handful of common supplements for dev/demo use
-- ============================================================
INSERT INTO supplement (code, name, category, description, nutrients) VALUES
('VIT-D3-1000',  'Vitamin D3 1000 IU',        'VITAMIN',    'Cholecalciferol – supports bone & immune health',
 '[{"substance": "Cholecalciferol", "amount": 25, "unit": "mcg"}]'),
('MAG-GLY-200',  'Magnesium Glycinate 200 mg', 'MINERAL',    'Chelated magnesium – promotes sleep & muscle relaxation',
 '[{"substance": "Magnesium", "amount": 200, "unit": "mg"}]'),
('IRON-CIT-50',  'Iron Citrate 50 mg',         'MINERAL',    'Gentle iron – avoid co-ingesting with calcium',
 '[{"substance": "Iron", "amount": 50, "unit": "mg"}]'),
('OMEGA3-1000',  'Omega-3 Fish Oil 1000 mg',   'FATTY_ACID', 'EPA/DHA – cardiovascular & cognitive support',
 '[{"substance": "EPA", "amount": 360, "unit": "mg"}, {"substance": "DHA", "amount": 240, "unit": "mg"}]'),
('VIT-C-500',    'Vitamin C 500 mg',            'VITAMIN',    'Ascorbic acid – antioxidant & immune support',
 '[{"substance": "Ascorbic Acid", "amount": 500, "unit": "mg"}]'),
('VIT-B12-500',  'Vitamin B12 500 mcg',         'VITAMIN',    'Methylcobalamin – energy & neurological support',
 '[{"substance": "Methylcobalamin", "amount": 500, "unit": "mcg"}]'),
('ZINC-BIS-15',  'Zinc Bisglycinate 15 mg',     'MINERAL',    'Chelated zinc – immune & skin health',
 '[{"substance": "Zinc", "amount": 15, "unit": "mg"}]'),
('CAL-CIT-500',  'Calcium Citrate 500 mg',      'MINERAL',    'Well-absorbed calcium – bone density; separate from iron',
 '[{"substance": "Calcium", "amount": 500, "unit": "mg"}]')
ON CONFLICT (code) DO NOTHING;
