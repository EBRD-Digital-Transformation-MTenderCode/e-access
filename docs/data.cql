CREATE KEYSPACE IF NOT EXISTS ocds WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS  ocds.access_tender (
    cp_id text,
    token_entity uuid,
    owner text,
    json_data text,
    primary key(cp_id, token_entity)
);

SELECT * FROM ocds.access_tender;

TRUNCATE ocds.access_tender;
