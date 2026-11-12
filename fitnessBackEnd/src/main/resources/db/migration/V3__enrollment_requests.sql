CREATE TABLE enrollment_requests (
                                     id bigint NOT NULL AUTO_INCREMENT,
                                     member_id bigint NOT NULL,
                                     plan_id bigint NULL,
                                     coach_id bigint NULL,
                                     status varchar(10) NOT NULL,
                                     created_at datetime(6) NOT NULL,
                                     PRIMARY KEY (id),
                                     KEY idx_enroll_member_status (member_id, status),
                                     CONSTRAINT fk_enroll_member FOREIGN KEY (member_id) REFERENCES members (id),
                                     CONSTRAINT fk_enroll_plan FOREIGN KEY (plan_id) REFERENCES membership_plans (id),
                                     CONSTRAINT fk_enroll_coach FOREIGN KEY (coach_id) REFERENCES coaches (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;