CREATE KEYSPACE IF NOT EXISTS ocds WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS  ocds.access_ein (
    oc_id text,
    json_data text,
    primary key(oc_id)
);

SELECT * FROM ocds.access_ein;

TRUNCATE ocds.access_ein;

CREATE TABLE IF NOT EXISTS  ocds.access_fs (
    cp_id text,
    oc_id text,
    json_data text,
    amount double,
    primary key(cp_id, oc_id)
);

SELECT * FROM ocds.access_fs;

TRUNCATE ocds.access_fs;

CREATE TABLE IF NOT EXISTS  ocds.access_cn (
    cp_id text,
    token_entity text,
    owner text,
    json_data text,
    primary key(cp_id, token_entity)
);

SELECT * FROM ocds.access_cn;

TRUNCATE ocds.access_cn;
