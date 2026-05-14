-- One issued HTML format per (tenant, certificate_type) when certificate_type_id is set.
-- Run after certificate_document_format exists. Removes older duplicates, then adds a partial unique index.

DELETE FROM certificate_document_format cdf
WHERE cdf.certificate_type_id IS NOT NULL
  AND EXISTS (
      SELECT 1
      FROM certificate_document_format cdf2
      WHERE cdf2.tenant_id = cdf.tenant_id
        AND cdf2.certificate_type_id = cdf.certificate_type_id
        AND cdf2.id <> cdf.id
        AND (cdf2.updated_at, cdf2.id) > (cdf.updated_at, cdf.id)
  );

CREATE UNIQUE INDEX IF NOT EXISTS uq_certificate_document_format_tenant_cert_type
    ON certificate_document_format (tenant_id, certificate_type_id)
    WHERE certificate_type_id IS NOT NULL;
