package com.ocds.tender.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class Objectives {
    public List<String> types;
    public String additionalInformation;
}
