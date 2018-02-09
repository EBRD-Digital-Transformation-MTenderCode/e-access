package com.procurement.access.dao;

import com.procurement.access.model.entity.EiEntity;
import org.springframework.stereotype.Service;

@Service
public interface EiDao {

    void save(EiEntity entity);

    EiEntity getByCpIdAndToken(String cpId, String token);
}

