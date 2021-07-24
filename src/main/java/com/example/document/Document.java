package com.example.document;

import java.net.URL;

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

    public static Document withURL(URL url) {
        return Document.withURL(url.toString());
    }
}
