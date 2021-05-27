package com.horizonzy;

public class ScannedItem {

    private Integer token;

    private String literal;

    public ScannedItem() {
    }

    public ScannedItem(Integer token, String literal) {
        this.token = token;
        this.literal = literal;
    }

    public Integer getToken() {
        return token;
    }

    public String getLiteral() {
        return literal;
    }
}
