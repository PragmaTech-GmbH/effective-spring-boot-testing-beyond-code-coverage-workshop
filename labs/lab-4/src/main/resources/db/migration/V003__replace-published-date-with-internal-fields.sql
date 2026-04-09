-- Replace published_date with library-internal fields populated on creation
ALTER TABLE books DROP COLUMN published_date;

ALTER TABLE books ADD COLUMN internal_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE books ADD COLUMN availability_date DATE NOT NULL DEFAULT CURRENT_DATE;

ALTER TABLE books ALTER COLUMN internal_name DROP DEFAULT;
ALTER TABLE books ALTER COLUMN availability_date DROP DEFAULT;
