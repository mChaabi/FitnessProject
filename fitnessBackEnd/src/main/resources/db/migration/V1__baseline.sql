CREATE TABLE refresh_tokens (
                                id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                token_hash VARCHAR(64) NOT NULL,
                                family_id  VARCHAR(36) NOT NULL,
                                user_id    BIGINT NOT NULL,
                                expires_at DATETIME(6) NOT NULL,
                                revoked_at DATETIME(6) NULL,
                                created_at DATETIME(6) NOT NULL,
                                CONSTRAINT uk_refresh_hash UNIQUE (token_hash),
                                CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                INDEX idx_refresh_family (family_id)
);