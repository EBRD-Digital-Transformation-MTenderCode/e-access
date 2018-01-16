package com.procurement.access.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.procurement.access.model.entity.EinEntity;
import java.util.Optional;
import org.springframework.stereotype.Service;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Service
public class EinDaoImpl implements EinDao {

    private static final String EIN_TABLE = "access_ein";
    private static final String CP_ID = "cp_id";
    private static final String TOKEN = "token_entity";
    private static final String OWNER = "owner";
    private static final String JSON_DATA = "json_data";

    private final Session session;

    public EinDaoImpl(final Session session) {
        this.session = session;
    }

    @Override
    public void save(final EinEntity entity) {
        final Insert insert = insertInto(EIN_TABLE);
        insert.value(CP_ID, entity.getCpId())
                .value(TOKEN, entity.getToken())
                .value(OWNER, entity.getOwner())
                .value(JSON_DATA, entity.getJsonData());
        session.execute(insert);
    }

    @Override
    public Optional<EinEntity> getByCpId(final String cpId) {
        final Statement query = select().all().from(EIN_TABLE).where(eq(CP_ID, cpId)).limit(1);
        final ResultSet rows = session.execute(query);
        return Optional.ofNullable(rows.one())
                .map(row -> {
                    final String cpid = row.getString(CP_ID);
                    final String token = row.getString(TOKEN);
                    final String owner = row.getString(OWNER);
                    final String jsonData = row.getString(JSON_DATA);
                    return new EinEntity(cpid, token, owner, jsonData);
                });
    }
}
