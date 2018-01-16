package com.procurement.access.dao;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.procurement.access.model.entity.FsEntity;
import org.springframework.stereotype.Service;

import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;

@Service
public class FsDaoImpl implements FsDao {

    private static final String FS_TABLE = "access_fs";
    private static final String CP_ID = "cp_id";
    private static final String OC_ID = "oc_id";
    private static final String TOKEN = "token_entity";
    private static final String OWNER = "owner";
    private static final String AMOUNT = "amount";
    private static final String AMOUNT_RESERVED = "amount_reserved";
    private static final String JSON_DATA = "json_data";

    private final Session session;

    public FsDaoImpl(final Session session) {
        this.session = session;
    }

    @Override
    public void save(final FsEntity entity) {
        final Insert insert = insertInto(FS_TABLE);
        insert.value(CP_ID, entity.getCpId())
                .value(OC_ID, entity.getOcId())
                .value(TOKEN, entity.getToken())
                .value(OWNER, entity.getOwner())
                .value(AMOUNT, entity.getAmount())
                .value(AMOUNT_RESERVED, entity.getAmountReserved())
                .value(JSON_DATA, entity.getJsonData());
        session.execute(insert);
    }
}
