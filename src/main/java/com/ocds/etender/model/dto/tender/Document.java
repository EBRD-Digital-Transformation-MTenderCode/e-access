package com.ocds.etender.model.dto.tender;

import java.util.List;

/**
 * Author: user
 * Created by: ModelGenerator on 10/5/17
 */
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