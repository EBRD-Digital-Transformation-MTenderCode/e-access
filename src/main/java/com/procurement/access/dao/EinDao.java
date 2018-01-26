package com.procurement.access.dao;

import com.procurement.access.model.entity.EinEntity;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface EinDao {

    void save(EinEntity entity);

    EinEntity getByCpIdAndToken(String cpId, String token);
}

