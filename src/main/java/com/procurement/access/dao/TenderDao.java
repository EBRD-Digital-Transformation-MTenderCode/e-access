package com.procurement.access.dao;

import com.procurement.access.model.entity.TenderEntity;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public interface TenderDao {

    void save(TenderEntity entity);

    TenderEntity getByCpId(String cpId);

    TenderEntity getByCpIdAndToken(String cpId, UUID token);

}

