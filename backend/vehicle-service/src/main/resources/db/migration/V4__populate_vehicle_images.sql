-- Update existing vehicles with sample car images from Unsplash

-- Toyota Premio
UPDATE vehicles 
SET image_url = 'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=500&auto=format' 
WHERE make = 'Toyota' AND model = 'Premio';

-- Nissan
UPDATE vehicles 
SET image_url = 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=500&auto=format' 
WHERE make = 'NISSAN';

-- Toyota Prius
UPDATE vehicles 
SET image_url = 'https://images.unsplash.com/photo-1567818735868-e71b99932e29?w=500&auto=format' 
WHERE make = 'Toyota' AND model = 'prius';

-- For any remaining vehicles without images, set a generic car placeholder
UPDATE vehicles 
SET image_url = 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=500&auto=format' 
WHERE image_url IS NULL OR image_url = '';
