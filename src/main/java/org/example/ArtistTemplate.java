package org.example;

import org.json.JSONArray;

public class ArtistTemplate {
    private String id;
    private JSONArray albums;
    private JSONArray singles;

    public ArtistTemplate(String id, JSONArray albums, JSONArray singles) {
        this.id = id;
        this.albums = albums;
        this.singles = singles;
    }

    public String getId() {
        return id;
    }

    public JSONArray getAlbums() {
        return albums;
    }

    public JSONArray getSingles() {
        return singles;
    }
}
