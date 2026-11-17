CREATE TABLE audit_log (
                           id bigint NOT NULL AUTO_INCREMENT,
                           created_at datetime(6) NOT NULL,
                           actor varchar(150) NOT NULL,
                           actor_role varchar(20) NULL,
                           action varchar(40) NOT NULL,
                           entity_type varchar(20) NOT NULL,
                           entity_id bigint NULL,
                           summary varchar(300) NOT NULL,
                           PRIMARY KEY (id),
                           KEY idx_audit_created (created_at),
                           KEY idx_audit_entity (entity_type, entity_id, created_at),
                           KEY idx_audit_actor (actor, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;