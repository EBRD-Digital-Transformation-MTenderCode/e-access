package com.ocds.etender.model.dto.tender;

import java.util.List;

public class Organization {
    public String name;
    public String id;
    public Identifier identifier;
    public List<Identifier> additionalIdentifiers;
    public Address address;
    public ContactPoint contactPoint;
    public List<String> roles;
    public Details details;
    public String buyerProfile;
}
