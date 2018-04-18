package com.procurement.access.dao;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.procurement.access.model.entity.TenderProcessEntity;
import org.springframework.stereotype.Service;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

@Service
public class TenderProcessDaoImpl implements TenderProcessDao {

    private static final String TENDER_TABLE = "access_tender";
    private static final String CP_ID = "cp_id";
    private static final String TOKEN = "token_entity";
    private static final String STAGE = "stage";
    private static final String CREATED_DATE = "created_date";
    private static final String OWNER = "owner";
    private static final String JSON_DATA = "json_data";

    private final Session session;

    public TenderProcessDaoImpl(final Session session) {
        this.session = session;
    }

    @Override
    public void save(final TenderProcessEntity entity) {
        final Insert insert = insertInto(TENDER_TABLE);
        insert.value(CP_ID, entity.getCpId())
                .value(TOKEN, entity.getToken())
                .value(OWNER, entity.getOwner())
                .value(STAGE, entity.getStage())
                .value(CREATED_DATE, entity.getCreatedDate())
                .value(JSON_DATA, entity.getJsonData());
        session.execute(insert);
    }

    @Override
    public TenderProcessEntity getByCpIdAndStage(final String cpId, final String stage) {
        final Statement query = select()
                .all()
                .from(TENDER_TABLE)
                .where(eq(CP_ID, cpId))
                .and(eq(STAGE, stage)).limit(1);
        final Row row = session.execute(query).one();
        if (row != null)
            return new TenderProcessEntity(
                    row.getString(CP_ID),
                    row.getUUID(TOKEN),
                    row.getString(OWNER),
                    row.getString(STAGE),
                    row.getTimestamp(CREATED_DATE),
                    row.getString(JSON_DATA));
        return null;

    }
}
