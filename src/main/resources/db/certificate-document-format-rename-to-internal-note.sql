-- certificate_document_format: internal admin notes column as internal_note.
-- Run on each district shard before or with the API that maps JPA to internal_note.
-- Handles legacy column names; idempotent if internal_note already exists.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'certificate_document_format'
          AND column_name = 'internal_note'
    ) THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'certificate_document_format'
          AND column_name = 'draft_description'
    ) THEN
        ALTER TABLE certificate_document_format
            RENAME COLUMN draft_description TO internal_note;
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'certificate_document_format'
          AND column_name = 'description_text'
    ) THEN
        ALTER TABLE certificate_document_format
            RENAME COLUMN description_text TO internal_note;
    END IF;
END $$;
