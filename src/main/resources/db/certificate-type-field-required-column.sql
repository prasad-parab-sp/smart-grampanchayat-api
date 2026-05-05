-- Optional: add required flag for dynamic form rows if your table predates this column.
ALTER TABLE certificate_type_field
    ADD COLUMN IF NOT EXISTS required BOOLEAN NOT NULL DEFAULT FALSE;
