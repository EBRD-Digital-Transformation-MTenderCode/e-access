package com.procurement.access.dao;

import com.procurement.access.model.entity.TenderProcessEntity;
import org.springframework.stereotype.Service;

@Service
public interface TenderProcessDao {

    void save(TenderProcessEntity entity);

    TenderProcessEntity getByCpIdAndStage(String cpId, String stage);

}

