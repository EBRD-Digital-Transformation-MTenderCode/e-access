CREATE KEYSPACE IF NOT EXISTS ocds WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS  ocds.access_ein (
    oc_id text,
    ein_id UUID,
    json_data text,
    primary key(oc_id, ein_id));

SELECT * FROM ocds.access_ein;
