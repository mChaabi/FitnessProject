CREATE TABLE face_templates (
                                id bigint NOT NULL AUTO_INCREMENT,
                                member_id bigint NOT NULL,
                                embedding blob NOT NULL,
                                consent_at datetime(6) NOT NULL,
                                recorded_by varchar(100) NOT NULL,
                                created_at datetime(6) NOT NULL,
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_face_member (member_id),
                                CONSTRAINT fk_face_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;