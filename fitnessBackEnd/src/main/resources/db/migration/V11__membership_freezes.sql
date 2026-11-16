CREATE TABLE membership_freezes (
                                    id bigint NOT NULL AUTO_INCREMENT,
                                    member_id bigint NOT NULL,
                                    start_date date NOT NULL,
                                    resume_date date NOT NULL,
                                    ended_on date NULL,
                                    days_added int NOT NULL,
                                    reason varchar(255) NULL,
                                    created_by varchar(100) NOT NULL,
                                    created_at datetime(6) NOT NULL,
                                    status varchar(10) NOT NULL,
                                    PRIMARY KEY (id),
                                    KEY idx_freeze_member_status (member_id, status),
                                    KEY idx_freeze_due (status, resume_date),
                                    CONSTRAINT fk_freeze_member FOREIGN KEY (member_id) REFERENCES members (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- two new email types (the column is a MySQL enum, so every value must be listed)
ALTER TABLE email_outbox
    MODIFY type enum('WAITLIST_PROMOTED','CLASS_CANCELLED','CLASS_REMINDER','SUBSCRIPTION_EXPIRING',
    'CHURN_RISK','WELCOME','PAYMENT_CONFIRMED','PAYMENT_REMINDER',
    'MEMBERSHIP_FROZEN','MEMBERSHIP_RESUMED') NOT NULL;