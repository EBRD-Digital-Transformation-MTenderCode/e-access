package com.procurement.access.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class OrganizationReference {
    public String name;
    public Integer id;
    public Identifier identifier;
    public Address address;
    public List<Identifier> additionalIdentifiers;
    public ContactPoint contactPoint;
}