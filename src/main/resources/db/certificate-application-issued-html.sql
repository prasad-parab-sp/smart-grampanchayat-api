-- District shard: cached rendered certificate HTML (generated on first view after approval).
ALTER TABLE certificate_application ADD COLUMN IF NOT EXISTS issued_document_html TEXT;

COMMENT ON COLUMN certificate_application.issued_document_html IS 'Frozen printable HTML after first successful generation; null until first GET issued-document.';
