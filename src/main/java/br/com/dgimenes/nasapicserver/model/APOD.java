package br.com.dgimenes.nasapicserver.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class APOD {

    @SerializedName("hdurl")
    private String url;

    @SerializedName("media_type")
    private String mediaType;

    private String explanation;

    private String title;

    public APOD(String url, String mediaType, String explanation, List<String> concepts, String title) {
        this.url = url;
        this.mediaType = mediaType;
        this.explanation = explanation;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getTitle() {
        return title;
    }
}
