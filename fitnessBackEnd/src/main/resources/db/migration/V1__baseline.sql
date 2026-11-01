CREATE TABLE users (
                       id bigint NOT NULL AUTO_INCREMENT,
                       email varchar(255) NOT NULL,
                       mot_de_passe varchar(255) NOT NULL,
                       nom varchar(255) NOT NULL,
                       prenom varchar(255) NOT NULL,
                       role enum('ADMIN','COACH','MEMBER','RECEPTION') NOT NULL,
                       enabled bit(1) NOT NULL,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE coaches (
                         id bigint NOT NULL AUTO_INCREMENT,
                         active bit(1) NOT NULL,
                         bio varchar(1000) DEFAULT NULL,
                         specialties varchar(150) DEFAULT NULL,
                         user_id bigint NOT NULL,
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_coaches_user (user_id),
                         CONSTRAINT fk_coaches_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE members (
                         id bigint NOT NULL AUTO_INCREMENT,
                         birth_date date DEFAULT NULL,
                         email varchar(100) NOT NULL,
                         first_name varchar(50) NOT NULL,
                         goal varchar(255) DEFAULT NULL,
                         joined_at date NOT NULL,
                         last_name varchar(50) NOT NULL,
                         phone varchar(20) DEFAULT NULL,
                         qr_code varchar(36) NOT NULL,
                         sex enum('FEMALE','MALE','OTHER') DEFAULT NULL,
                         status enum('ACTIVE','EXPIRED','FROZEN','INACTIVE') NOT NULL,
                         user_id bigint DEFAULT NULL,
                         coach_id bigint DEFAULT NULL,
                         email_opt_out bit(1) NOT NULL,
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_members_email (email),
                         UNIQUE KEY uk_members_qr (qr_code),
                         UNIQUE KEY uk_members_user (user_id),
                         KEY idx_members_last_name (last_name),
                         KEY fk_members_coach_idx (coach_id),
                         CONSTRAINT fk_members_coach FOREIGN KEY (coach_id) REFERENCES coaches (id),
                         CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE membership_plans (
                                  id bigint NOT NULL AUTO_INCREMENT,
                                  active bit(1) NOT NULL,
                                  description varchar(255) DEFAULT NULL,
                                  duration_days int NOT NULL,
                                  name varchar(80) NOT NULL,
                                  price decimal(10,2) NOT NULL,
                                  PRIMARY KEY (id),
                                  UNIQUE KEY uk_plans_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE subscriptions (
                               id bigint NOT NULL AUTO_INCREMENT,
                               end_date date NOT NULL,
                               start_date date NOT NULL,
                               status enum('ACTIVE','CANCELLED','EXPIRED') NOT NULL,
                               member_id bigint NOT NULL,
                               plan_id bigint NOT NULL,
                               PRIMARY KEY (id),
                               KEY idx_subs_member_status (member_id, status),
                               KEY fk_subs_plan_idx (plan_id),
                               CONSTRAINT fk_subs_member FOREIGN KEY (member_id) REFERENCES members (id),
                               CONSTRAINT fk_subs_plan FOREIGN KEY (plan_id) REFERENCES membership_plans (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payments (
                          id bigint NOT NULL AUTO_INCREMENT,
                          amount decimal(10,2) NOT NULL,
                          method enum('CARD','CASH','CHECK','TRANSFER') NOT NULL,
                          paid_at datetime(6) NOT NULL,
                          receipt_number varchar(30) DEFAULT NULL,
                          received_by varchar(100) DEFAULT NULL,
                          subscription_id bigint NOT NULL,
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_payments_subscription (subscription_id),
                          UNIQUE KEY uk_payments_receipt (receipt_number),
                          CONSTRAINT fk_payments_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE check_ins (
                           id bigint NOT NULL AUTO_INCREMENT,
                           checked_at datetime(6) NOT NULL,
                           outcome enum('ALLOWED','DENIED','DUPLICATE') NOT NULL,
                           performed_by varchar(100) DEFAULT NULL,
                           reason enum('EXPIRED','FROZEN','INACTIVE','NOT_FOUND','NO_SUBSCRIPTION') DEFAULT NULL,
                           member_id bigint NOT NULL,
                           PRIMARY KEY (id),
                           KEY idx_checkins_member_date (member_id, checked_at),
                           CONSTRAINT fk_checkins_member FOREIGN KEY (member_id) REFERENCES members (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE class_templates (
                                 id bigint NOT NULL AUTO_INCREMENT,
                                 active bit(1) NOT NULL,
                                 capacity int NOT NULL,
                                 day_of_week int NOT NULL,
                                 description varchar(255) DEFAULT NULL,
                                 duration_minutes int NOT NULL,
                                 name varchar(100) NOT NULL,
                                 room varchar(50) DEFAULT NULL,
                                 start_time time NOT NULL,
                                 coach_id bigint NOT NULL,
                                 PRIMARY KEY (id),
                                 KEY fk_tpl_coach_idx (coach_id),
                                 CONSTRAINT fk_tpl_coach FOREIGN KEY (coach_id) REFERENCES coaches (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE class_sessions (
                                id bigint NOT NULL AUTO_INCREMENT,
                                capacity int NOT NULL,
                                description varchar(255) DEFAULT NULL,
                                end_time time NOT NULL,
                                name varchar(100) NOT NULL,
                                room varchar(50) DEFAULT NULL,
                                session_date date NOT NULL,
                                start_time time NOT NULL,
                                status enum('CANCELLED','SCHEDULED') NOT NULL,
                                coach_id bigint NOT NULL,
                                template_id bigint DEFAULT NULL,
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_sessions_tpl_date (template_id, session_date),
                                KEY idx_sessions_date (session_date),
                                KEY fk_sessions_coach_idx (coach_id),
                                CONSTRAINT fk_sessions_tpl FOREIGN KEY (template_id) REFERENCES class_templates (id),
                                CONSTRAINT fk_sessions_coach FOREIGN KEY (coach_id) REFERENCES coaches (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE class_bookings (
                                id bigint NOT NULL AUTO_INCREMENT,
                                queued_at datetime(6) NOT NULL,
                                status enum('CANCELLED','CONFIRMED','WAITLIST') NOT NULL,
                                member_id bigint NOT NULL,
                                session_id bigint NOT NULL,
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_bookings_session_member (session_id, member_id),
                                KEY fk_bookings_member_idx (member_id),
                                CONSTRAINT fk_bookings_member FOREIGN KEY (member_id) REFERENCES members (id),
                                CONSTRAINT fk_bookings_session FOREIGN KEY (session_id) REFERENCES class_sessions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exercices (
                           id bigint NOT NULL AUTO_INCREMENT,
                           categorie varchar(50) DEFAULT NULL,
                           description varchar(255) DEFAULT NULL,
                           duree int DEFAULT NULL,
                           image varchar(255) DEFAULT NULL,
                           nom varchar(255) DEFAULT NULL,
                           PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE workout_programs (
                                  id bigint NOT NULL AUTO_INCREMENT,
                                  active bit(1) NOT NULL,
                                  goal varchar(255) DEFAULT NULL,
                                  name varchar(100) NOT NULL,
                                  start_date date NOT NULL,
                                  coach_id bigint NOT NULL,
                                  member_id bigint NOT NULL,
                                  PRIMARY KEY (id),
                                  KEY idx_prog_member (member_id),
                                  KEY idx_prog_coach (coach_id),
                                  CONSTRAINT fk_prog_coach FOREIGN KEY (coach_id) REFERENCES coaches (id),
                                  CONSTRAINT fk_prog_member FOREIGN KEY (member_id) REFERENCES members (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE workout_items (
                               id bigint NOT NULL AUTO_INCREMENT,
                               day_of_week int NOT NULL,
                               notes varchar(255) DEFAULT NULL,
                               position int NOT NULL,
                               reps int NOT NULL,
                               rest_seconds int NOT NULL,
                               sets int NOT NULL,
                               exercice_id bigint NOT NULL,
                               program_id bigint NOT NULL,
                               PRIMARY KEY (id),
                               KEY fk_items_exercice_idx (exercice_id),
                               KEY fk_items_program_idx (program_id),
                               CONSTRAINT fk_items_exercice FOREIGN KEY (exercice_id) REFERENCES exercices (id),
                               CONSTRAINT fk_items_program FOREIGN KEY (program_id) REFERENCES workout_programs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE body_measurements (
                                   id bigint NOT NULL AUTO_INCREMENT,
                                   arm_cm double DEFAULT NULL,
                                   body_fat_percent double DEFAULT NULL,
                                   chest_cm double DEFAULT NULL,
                                   measured_on date NOT NULL,
                                   notes varchar(255) DEFAULT NULL,
                                   recorded_by varchar(100) DEFAULT NULL,
                                   waist_cm double DEFAULT NULL,
                                   weight_kg double DEFAULT NULL,
                                   member_id bigint NOT NULL,
                                   PRIMARY KEY (id),
                                   UNIQUE KEY uk_meas_member_date (member_id, measured_on),
                                   CONSTRAINT fk_meas_member FOREIGN KEY (member_id) REFERENCES members (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE contacts (
                          id bigint NOT NULL AUTO_INCREMENT,
                          email varchar(255) NOT NULL,
                          message varchar(1000) NOT NULL,
                          nom varchar(50) NOT NULL,
                          PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE email_outbox (
                              id bigint NOT NULL AUTO_INCREMENT,
                              ai_generated bit(1) DEFAULT NULL,
                              attempts int NOT NULL,
                              claimed_at datetime(6) DEFAULT NULL,
                              created_at datetime(6) NOT NULL,
                              dedup_key varchar(120) DEFAULT NULL,
                              facts_text text,
                              last_error varchar(500) DEFAULT NULL,
                              member_id bigint NOT NULL,
                              next_attempt_at datetime(6) NOT NULL,
                              recipient varchar(150) NOT NULL,
                              recipient_name varchar(100) DEFAULT NULL,
                              sent_at datetime(6) DEFAULT NULL,
                              status enum('FAILED','PENDING','SENDING','SENT','SKIPPED') NOT NULL,
                              type enum('WAITLIST_PROMOTED','CLASS_CANCELLED','CLASS_REMINDER','SUBSCRIPTION_EXPIRING','CHURN_RISK') NOT NULL,
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_outbox_dedup (dedup_key),
                              KEY idx_outbox_status_next (status, next_attempt_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_tokens (
                                id bigint NOT NULL AUTO_INCREMENT,
                                token_hash varchar(64) NOT NULL,
                                family_id varchar(36) NOT NULL,
                                user_id bigint NOT NULL,
                                expires_at datetime(6) NOT NULL,
                                revoked_at datetime(6) DEFAULT NULL,
                                created_at datetime(6) NOT NULL,
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_refresh_hash (token_hash),
                                KEY idx_refresh_family (family_id),
                                CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;