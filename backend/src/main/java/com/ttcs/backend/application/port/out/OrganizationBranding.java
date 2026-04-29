package com.ttcs.backend.application.port.out;

public class OrganizationBranding {

    private final String organizationName;
    private final String logoUrl;
    private final String primaryColor;
    private final String confidentialityLabel;

    public OrganizationBranding(String organizationName, String logoUrl, String primaryColor, String confidentialityLabel) {
        this.organizationName = organizationName;
        this.logoUrl = logoUrl;
        this.primaryColor = primaryColor;
        this.confidentialityLabel = confidentialityLabel;
    }

    public String organizationName() { return organizationName; }
    public String logoUrl() { return logoUrl; }
    public String primaryColor() { return primaryColor; }
    public String confidentialityLabel() { return confidentialityLabel; }

    public String getOrganizationName() { return organizationName; }
    public String getLogoUrl() { return logoUrl; }
    public String getPrimaryColor() { return primaryColor; }
    public String getConfidentialityLabel() { return confidentialityLabel; }
}
