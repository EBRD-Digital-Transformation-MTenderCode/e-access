package com.procurement.access.dao;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.procurement.access.model.entity.CnEntity;
import org.springframework.stereotype.Service;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

@Service
public class CnDaoImpl implements CnDao {

    private static final String CN_TABLE = "access_cn";
    private static final String CP_ID = "cp_id";
    private static final String TOKEN = "token_entity";
    private static final String OWNER = "owner";
    private static final String JSON_DATA = "json_data";

    private final Session session;

    public CnDaoImpl(final Session session) {
        this.session = session;
    }

    @Override
    public void save(final CnEntity entity) {
        final Insert insert = insertInto(CN_TABLE);
        insert.value(CP_ID, entity.getCpId())
                .value(TOKEN, entity.getToken())
                .value(OWNER, entity.getOwner())
                .value(JSON_DATA, entity.getJsonData());
        session.execute(insert);
    }

    @Override
    public CnEntity getByCpId(final String cpId) {
        final Statement query = select()
                .all()
                .from(CN_TABLE)
                .where(eq(CP_ID, cpId)).limit(1);
        final Row row = session.execute(query).one();
        return new CnEntity(
                row.getString(CP_ID),
                row.getString(TOKEN),
                row.getString(OWNER),
                row.getString(JSON_DATA));
    }

    @Override
    public CnEntity getByCpIdAndToken(final String cpId, final String token) {
        final Statement query = select()
                .all()
                .from(CN_TABLE)
                .where(eq(CP_ID, cpId))
                .and(eq(TOKEN, token)).limit(1);
        final Row row = session.execute(query).one();
        return new CnEntity(
                row.getString(CP_ID),
                row.getString(TOKEN),
                row.getString(OWNER),
                row.getString(JSON_DATA));
    }
}
