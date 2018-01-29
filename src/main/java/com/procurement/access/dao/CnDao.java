package com.procurement.access.dao;

import com.procurement.access.model.entity.CnEntity;
import org.springframework.stereotype.Service;

@Service
public interface CnDao {

    void save(CnEntity entity);

    CnEntity getByCpId(String cpId);

    CnEntity getByCpIdAndToken(String cpId, String token);

}

