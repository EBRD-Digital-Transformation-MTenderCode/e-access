package com.ocds.etender.model.dto.tender;

import java.util.List;

public class Amendment {
    public String date;
    public String rationale;
    public String id;
    public String description;
    public String amendsReleaseID;
    public String releaseID;
    public List<ChangeableObject> changes;
}
