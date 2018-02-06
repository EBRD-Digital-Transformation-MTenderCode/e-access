package com.procurement.access.dao;

import com.procurement.access.model.entity.TenderEntity;
import org.springframework.stereotype.Service;

@Service
public interface CnDao {

    void save(TenderEntity entity);

    TenderEntity getByCpId(String cpId);

    TenderEntity getByCpIdAndToken(String cpId, String token);

}

