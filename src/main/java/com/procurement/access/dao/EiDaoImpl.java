package com.procurement.access.dao;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.procurement.access.model.entity.EiEntity;
import org.springframework.stereotype.Service;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

@Service
public class EiDaoImpl implements EiDao {

    private static final String EI_TABLE = "access_ein";
    private static final String CP_ID = "cp_id";
    private static final String TOKEN = "token_entity";
    private static final String OWNER = "owner";
    private static final String JSON_DATA = "json_data";

    private final Session session;

    public EiDaoImpl(final Session session) {
        this.session = session;
    }

    @Override
    public void save(final EiEntity entity) {
        final Insert insert = insertInto(EI_TABLE);
        insert.value(CP_ID, entity.getCpId())
                .value(TOKEN, entity.getToken())
                .value(OWNER, entity.getOwner())
                .value(JSON_DATA, entity.getJsonData());
        session.execute(insert);
    }

    @Override
    public EiEntity getByCpIdAndToken(final String cpId, final String token) {
        final Statement query = select()
                .all()
                .from(EI_TABLE)
                .where(eq(CP_ID, cpId))
                .and(eq(TOKEN, token)).limit(1);
        final Row row = session.execute(query).one();
        return new EiEntity(
                row.getString(CP_ID),
                row.getString(TOKEN),
                row.getString(OWNER),
                row.getString(JSON_DATA));
    }
}
