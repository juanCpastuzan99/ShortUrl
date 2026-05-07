package com.parcial.urlshortener.domain.model;

public class LinkMetadata {

    private String id;
    private String shortCode;
    private String imageUrl;
    private String description;

    public LinkMetadata() {}

    public LinkMetadata(String shortCode, String imageUrl, String description) {
        this.shortCode = shortCode;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
