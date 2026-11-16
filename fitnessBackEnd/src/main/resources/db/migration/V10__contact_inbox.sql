ALTER TABLE contacts
    ADD COLUMN sujet varchar(30) NULL,
  ADD COLUMN status varchar(10) NOT NULL DEFAULT 'NEW',
  ADD COLUMN created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ADD COLUMN handled_by varchar(100) NULL,
  ADD COLUMN handled_at datetime(6) NULL,
  ADD KEY idx_contacts_status (status, created_at);