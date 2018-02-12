package com.procurement.access.dao;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.procurement.access.model.entity.TenderEntity;
import org.springframework.stereotype.Service;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

@Service
public class TenderDaoImpl implements TenderDao {

    private static final String TENDER_TABLE = "access_tender";
    private static final String CP_ID = "cp_id";
    private static final String TOKEN = "token_entity";
    private static final String OWNER = "owner";
    private static final String JSON_DATA = "json_data";

    private final Session session;

    public TenderDaoImpl(final Session session) {
        this.session = session;
    }

    @Override
    public void save(final TenderEntity entity) {
        final Insert insert = insertInto(TENDER_TABLE);
        insert.value(CP_ID, entity.getCpId())
                .value(TOKEN, entity.getToken())
                .value(OWNER, entity.getOwner())
                .value(JSON_DATA, entity.getJsonData());
        session.execute(insert);
    }

    @Override
    public TenderEntity getByCpId(final String cpId) {
        final Statement query = select()
                .all()
                .from(TENDER_TABLE)
                .where(eq(CP_ID, cpId)).limit(1);
        final Row row = session.execute(query).one();
        return new TenderEntity(
                row.getString(CP_ID),
                row.getUUID(TOKEN),
                row.getString(OWNER),
                row.getString(JSON_DATA));
    }

    @Override
    public TenderEntity getByCpIdAndToken(final String cpId, final String token) {
        final Statement query = select()
                .all()
                .from(TENDER_TABLE)
                .where(eq(CP_ID, cpId))
                .and(eq(TOKEN, token)).limit(1);
        final Row row = session.execute(query).one();
        return new TenderEntity(
                row.getString(CP_ID),
                row.getUUID(TOKEN),
                row.getString(OWNER),
                row.getString(JSON_DATA));
    }
}