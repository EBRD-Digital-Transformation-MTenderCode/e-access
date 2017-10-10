package com.ocds.etender.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class ContactPoint {
    public String name;
    public String email;
    public String telephone;
    public String faxNumber;
    public String url;
    public List<String> languages;
}