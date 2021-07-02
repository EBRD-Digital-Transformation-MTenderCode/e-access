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

INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'GPA', 'goods', 'false');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'GPA', 'services', 'false');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'GPA', 'works', 'false');

INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'TEST_GPA', 'goods', 'false');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'TEST_GPA', 'services', 'false');
INSERT INTO ocds.access_rules (country, pmd, parameter, value)  VALUES ('MD', 'TEST_GPA', 'works', 'false');


DROP TABLE IF EXISTS ocds.access_rules;

CREATE TABLE IF NOT EXISTS ocds.access_rules
(
    country        text,
    pmd            text,
    operation_type text,
    parameter      text,
    value          text,
    primary key (country, pmd, operation_type, parameter)
);

INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'OT', 'all', 'goods', 'false');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'OT', 'all', 'services', 'false');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'OT', 'all', 'works', 'false');

INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'SV', 'all', 'goods', 'true');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'SV', 'all', 'services', 'true');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'SV', 'all', 'works', 'false');

INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_OT', 'all', 'goods', 'false');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_OT', 'all', 'services', 'false');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_OT', 'all', 'works', 'false');

INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_SV', 'all', 'goods', 'true');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_SV', 'all', 'services', 'true');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_SV', 'all', 'works', 'false');

INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'GPA', 'all', 'goods', 'false');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'GPA', 'all', 'services', 'false');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'GPA', 'all', 'works', 'false');

INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_GPA', 'all', 'goods', 'false');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_GPA', 'all', 'services', 'false');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_GPA', 'all', 'works', 'false');

INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'GPA', 'qualificationConsideration', 'validStates', '[{"status":"active","statusDetails":"qualification"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_GPA', 'qualificationConsideration', 'validStates', '[{"status":"active","statusDetails":"qualification"}]');

INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'OF', 'outsourcingPN', 'validStates', '[{"status":"planning","statusDetails":"planning"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_OF', 'outsourcingPN', 'validStates', '[{"status":"planning","statusDetails":"planning"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'CF', 'outsourcingPN', 'validStates', '[{"status":"planning","statusDetails":"planning"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_CF', 'outsourcingPN', 'validStates', '[{"status":"planning","statusDetails":"planning"}]');


INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'DCO', 'relationAP', 'validStates', '[{"status":"planning","statusDetails":"outsourcing"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_DCO', 'relationAP', 'validStates', '[{"status":"planning","statusDetails":"outsourcing"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'RFQ', 'relationAP', 'validStates', '[{"status":"planning","statusDetails":"outsourcing"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_RFQ', 'relationAP', 'validStates', '[{"status":"planning","statusDetails":"outsourcing"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'MC', 'relationAP', 'validStates', '[{"status":"planning","statusDetails":"outsourcing"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_MC', 'relationAP', 'validStates', '[{"status":"planning","statusDetails":"outsourcing"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'CF', 'relationAP', 'validStates', '[{"status":"planning","statusDetails":"aggregation"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_CF', 'relationAP', 'validStates', '[{"status":"planning","statusDetails":"aggregation"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'OF', 'relationAP', 'validStates', '[{"status":"planning","statusDetails":"aggregation"}]');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'TEST_OF', 'relationAP', 'validStates', '[{"status":"planning","statusDetails":"aggregation"}]');

INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'OF', 'all', 'maxDurationOfFA', '126144000');
INSERT INTO ocds.access_rules (country, pmd, operation_type, parameter, value)
VALUES ('MD', 'CF', 'all', 'maxDurationOfFA', '126144000');

DROP TABLE IF EXISTS ocds.access_tender;

CREATE TABLE IF NOT EXISTS  ocds.access_tender (
    cpid text,
    ocid text,
    token_entity uuid,
    owner text,
    created_date timestamp,
    json_data text,
    primary key(cpid, ocid, token_entity)
);

/**
  New Keyspace
 */

CREATE KEYSPACE IF NOT EXISTS access WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

CREATE TABLE IF NOT EXISTS access.tenders (
    cpid         TEXT,
    ocid         TEXT,
    token_entity TEXT,
    owner        TEXT,
    created_date TIMESTAMP,
    json_data    TEXT,
    primary key (cpid, ocid, token_entity)
);

CREATE TABLE IF NOT EXISTS access.history (
    operation_id   TEXT,
    command        TEXT,
    operation_date TIMESTAMP,
    json_data      TEXT,
    primary key (operation_id, command)
);

CREATE TABLE IF NOT EXISTS access.rules (
    country        TEXT,
    pmd            TEXT,
    operation_type TEXT,
    parameter      TEXT,
    value          TEXT,
    primary key (country, pmd, operation_type, parameter)
);