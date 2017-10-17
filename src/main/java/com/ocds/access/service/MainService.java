package com.ocds.access.service;

import com.ocds.access.model.dto.DataDto;
import org.springframework.stereotype.Service;

@Service
public interface MainService {

    void insertData(DataDto data);

    void updateData(DataDto data);
}
