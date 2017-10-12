CREATE KEYSPACE IF NOT EXISTS ocds WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS  ocds.tender (
    oc_id text,
    added_date timestamp,
    json_data text,
    event_type text,
    id UUID,
    primary key(oc_id, added_date)
)WITH CLUSTERING ORDER BY (added_date DESC);

CREATE TABLE IF NOT EXISTS  ocds.budget (
    oc_id text,
    added_date timestamp,
    json_data text,
    event_type text,
    id UUID,
    primary key(oc_id, added_date)
)WITH CLUSTERING ORDER BY (added_date DESC);

CREATE TABLE IF NOT EXISTS  ocds.related_notice (
    oc_id text,
    added_date timestamp,
    json_data text,
    event_type text,
    id UUID,
    primary key(oc_id, added_date)
)WITH CLUSTERING ORDER BY (added_date DESC);

CREATE TABLE IF NOT EXISTS  ocds.event_log (
    oc_id text,
    added_date timestamp,
    event_type text,
    id UUID,
    primary key(oc_id, added_date)
)WITH CLUSTERING ORDER BY (added_date DESC);