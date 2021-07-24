package com.example.document;

public class Document {

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static Document withURL(String url) {
        var document = new Document();

        document.setUrl(url);

        return document;
    }
}
