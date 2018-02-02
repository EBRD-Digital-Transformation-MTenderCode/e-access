package com.procurement.access.dao;

import com.procurement.access.model.entity.FsEntity;
import org.springframework.stereotype.Service;

@Service
public interface FsDao {

    void save(FsEntity entity);

    FsEntity getByCpIdAndToken(String cpId, String token);
}

