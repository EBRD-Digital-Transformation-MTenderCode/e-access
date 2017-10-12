package com.ocds.tender.service;

import com.ocds.tender.model.dto.DataDto;
import org.springframework.stereotype.Service;

@Service
public interface MainService {

    void insertData(DataDto data);

    void updateData(DataDto data);
}
