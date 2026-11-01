CREATE TABLE agent_runs (
                            id          VARCHAR(36) PRIMARY KEY,
                            graph       VARCHAR(50)  NOT NULL,
                            started_by  VARCHAR(150) NOT NULL,
                            member_id   BIGINT       NOT NULL,
                            status      VARCHAR(20)  NOT NULL,
                            attempts    INT          NOT NULL DEFAULT 0,
                            created_at  DATETIME(6)  NOT NULL,
                            finished_at DATETIME(6)  NULL,
                            error       VARCHAR(500) NULL,
                            INDEX idx_runs_user_day (started_by, graph, created_at)
);