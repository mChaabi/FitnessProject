CREATE TABLE videos (
                        id bigint NOT NULL AUTO_INCREMENT,
                        title varchar(120) NOT NULL,
                        description varchar(255) NULL,
                        category varchar(20) NOT NULL,
                        type varchar(10) NOT NULL,
                        source varchar(255) NOT NULL,
                        duration_min int NULL,
                        active bit(1) NOT NULL,
                        created_at datetime(6) NOT NULL,
                        PRIMARY KEY (id),
                        KEY idx_videos_cat (category, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;