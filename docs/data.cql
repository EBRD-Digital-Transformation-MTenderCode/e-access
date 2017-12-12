CREATE KEYSPACE IF NOT EXISTS ocds WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS  ocds.access_ein (
    cp_id text,
    date timestamp,
    json_data text,
    primary key(cp_id, date)
)WITH CLUSTERING ORDER BY (date DESC);

SELECT * FROM ocds.access_ein;
