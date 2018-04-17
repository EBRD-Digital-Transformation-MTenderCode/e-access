package com.procurement.access.dao;

import com.procurement.access.model.entity.TenderProcessEntity;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public interface TenderProcessDao {

    void save(TenderProcessEntity entity);

//    TenderProcessEntity getByCpId(String cpId);

    TenderProcessEntity getByCpIdAndToken(String cpId, UUID token);

    TenderProcessEntity getByCpIdAndStage(String cpId, String stage);

}

