-- Migration V6: enforce end_date >= start_date at the database level

UPDATE menstrual_records
SET end_date = start_date
WHERE end_date IS NOT NULL
  AND end_date < start_date;

ALTER TABLE menstrual_records
    ADD CONSTRAINT chk_records_end_after_start
    CHECK (end_date IS NULL OR end_date >= start_date);
