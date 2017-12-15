CREATE KEYSPACE IF NOT EXISTS ocds WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS  ocds.access_ein (
    oc_id text,
    date timestamp,
    json_data text,
    primary key(oc_id, date)
)WITH CLUSTERING ORDER BY (date DESC);

SELECT * FROM ocds.access_ein;

TRUNCATE ocds.access_ein;

CREATE TABLE IF NOT EXISTS  ocds.access_fs (
    cp_id text,
    oc_id text,
    date timestamp,
    json_data text,
    amount double,
    primary key(cp_id, oc_id, date)
)WITH CLUSTERING ORDER BY (oc_id DESC);

SELECT * FROM ocds.access_fs;

TRUNCATE ocds.access_fs;