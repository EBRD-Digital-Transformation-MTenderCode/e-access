package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "uri",
        "version",
        "extensions",
        "publishedDate",
        "releases",
        "publisher",
        "license",
        "publicationPolicy"
})
public class ReleasePackage {
    @JsonProperty("uri")
    @JsonPropertyDescription("The URI of this package that identifies it uniquely in the world. Recommended practice " +
            "is to use a dereferenceable URI, where a persistent copy of this package is available.")
    @NotNull
    private final String uri;

    @JsonProperty("version")
    @JsonPropertyDescription("The version of the OCDS schema used in this package, expressed as major.minor For " +
            "example: 1.0 or 1.1")
    @NotNull
    private final String version;

    @JsonProperty("extensions")
    @JsonPropertyDescription("An array of OCDS extensions used in this package. Each entry should be a URL to the " +
            "extension.json file for that extension.")
    @Valid
    private final List<String> extensions;

    @JsonProperty("publishedDate")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonPropertyDescription("The date that this package was published. If this package is generated 'on demand', " +
            "this date should reflect the date of the last change to the underlying contents of the package.")
    @NotNull
    private final LocalDateTime publishedDate;

    @JsonProperty("releases")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Size(min = 1)
    @Valid
    @NotNull
    private final Set<ReleaseExt> releases;

    @JsonProperty("publisher")
    @JsonPropertyDescription("Information to uniquely identify the publisher of this package.")
    @Valid
    @NotNull
    private final Publisher publisher;

    @JsonProperty("license")
    @JsonPropertyDescription("A link to the license that applies to the data in this package. A Public Domain " +
            "Dedication or [Open Definition Conformant](http://opendefinition.org/licenses/) license is strongly " +
            "recommended. The canonical URI of the license should be used. Documents linked from this file may be under " +
            "other license conditions. ")
    private final String license;

    @JsonProperty("publicationPolicy")
    @JsonPropertyDescription("A link to a document describing the publishers [publication policy](http://standard" +
            ".open-contracting.org/latest/en/implementation/publication_policy/).")
    private final String publicationPolicy;

    @JsonCreator
    public ReleasePackage(@JsonProperty("uri") final String uri,
                          @JsonProperty("version") final String version,
                          @JsonProperty("extensions") final List<String> extensions,
                          @JsonProperty("publishedDate") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime publishedDate,
                          @JsonProperty("releases") final LinkedHashSet<ReleaseExt> releases,
                          @JsonProperty("publisher") final Publisher publisher,
                          @JsonProperty("license") final String license,
                          @JsonProperty("publicationPolicy") final String publicationPolicy) {
        this.uri = uri;
        this.version = version;
        this.extensions = extensions;
        this.publishedDate = publishedDate;
        this.releases = releases;
        this.publisher = publisher;
        this.license = license;
        this.publicationPolicy = publicationPolicy;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(uri)
                .append(version)
                .append(extensions)
                .append(publishedDate)
                .append(releases)
                .append(publisher)
                .append(license)
                .append(publicationPolicy)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ReleasePackage)) {
            return false;
        }
        final ReleasePackage rhs = (ReleasePackage) other;
        return new EqualsBuilder().append(uri, rhs.uri)
                .append(version, rhs.version)
                .append(extensions, rhs.extensions)
                .append(publishedDate, rhs.publishedDate)
                .append(releases, rhs.releases)
                .append(publisher, rhs.publisher)
                .append(license, rhs.license)
                .append(publicationPolicy, rhs.publicationPolicy)
                .isEquals();
    }
}
