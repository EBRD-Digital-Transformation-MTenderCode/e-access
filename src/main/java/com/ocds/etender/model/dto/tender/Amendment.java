package com.ocds.etender.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class Amendment {
    public String date;
    public String rationale;
    public String id;
    public String description;
    public String amendsReleaseID;
    public String releaseID;
    public List<ChangeableObject> changes;
}
