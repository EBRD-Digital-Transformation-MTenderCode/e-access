package com.procurement.access.dao;

import com.procurement.access.model.entity.FsEntity;
import org.springframework.stereotype.Service;

@Service
public interface FsDao {

    void save(FsEntity entity);

    FsEntity getByCpIdAndIdAndToken(String cpId, String fsId, String token);
}

