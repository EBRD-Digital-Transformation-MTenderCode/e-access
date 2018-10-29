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

CREATE TABLE IF NOT EXISTS  ocds.access_rules (
    country text,
    pmd text,
    parameter text,
    value text,
    primary key(country, pmd, parameter)
);

INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'OT', 'goods', 'false');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'OT', 'services', 'false');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'OT', 'works', 'false');

INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'SV', 'goods', 'true');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'SV', 'services', 'true');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'SV', 'works', 'false');

INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'TEST_OT', 'goods', 'false');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'TEST_OT', 'services', 'false');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'TEST_OT', 'works', 'false');

INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'TEST_SV', 'goods', 'true');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'TEST_SV', 'services', 'true');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'TEST_SV', 'works', 'false');