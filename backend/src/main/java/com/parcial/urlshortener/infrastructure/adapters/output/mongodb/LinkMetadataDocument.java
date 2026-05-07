package com.parcial.urlshortener.infrastructure.adapters.output.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "link_metadata")
public class LinkMetadataDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("short_code")
    private String shortCode;

    @Field("image_url")
    private String imageUrl;

    @Field("description")
    private String description;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
