-- Migration V4: Remove personal settings fields from users
ALTER TABLE users DROP COLUMN phone;
ALTER TABLE users DROP COLUMN email;
ALTER TABLE users DROP COLUMN birth_date;
ALTER TABLE users DROP COLUMN menarche_age;
ALTER TABLE users DROP COLUMN avg_cycle_days;
ALTER TABLE users DROP COLUMN avg_period_days;
