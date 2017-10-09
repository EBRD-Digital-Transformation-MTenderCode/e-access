package com.ocds.etender.model.dto.tender;

import java.util.List;

/**
 * Author: user
 * Created by: ModelGenerator on 10/5/17
 */
public class OrganizationReference {
    public String name;
    public Integer id;
    public Identifier identifier;
    public Address address;
    public List<Identifier> additionalIdentifiers;
    public ContactPoint contactPoint;
}