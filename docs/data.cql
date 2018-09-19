CREATE KEYSPACE IF NOT EXISTS ocds WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS  ocds.access_tender (
    cp_id text,
    stage text,
    token_entity uuid,
    owner text,
    created_date timestamp,
    json_data text,
    primary key(cp_id, stage, token_entity)
);

CREATE TABLE IF NOT EXISTS  ocds.access_history (
    operation_id text,
    command text,
    operation_date timestamp,
    json_data text,
    primary key(operation_id, command)
);

SELECT * FROM ocds.access_tender;

TRUNCATE ocds.access_tender;
