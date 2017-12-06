package com.procurement.access.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class Document {
    public String id;
    public String documentType;
    public String title;
    public String description;
    public String url;
    public String datePublished;
    public String dateModified;
    public String format;
    public String language;
    public List<String> relatedLots;
}