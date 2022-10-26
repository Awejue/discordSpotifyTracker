package org.example;


import java.util.ArrayList;

@SuppressWarnings({"unused", "ConstantConditions"})
public class ArtistTemplate {
    final private String id;

    final private String name;

    final private ArrayList<String> albums;
    final private ArrayList<String> singles;


    public ArtistTemplate() {
        this.id = null;
        this.name = null;
        this.albums = null;
        this.singles = null;
    }
    public ArtistTemplate(String id, String name, ArrayList<String> albums, ArrayList<String> singles) {
        this.id = id;
        this.name = name;
        this.albums = albums;
        this.singles = singles;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getAlbums() {
        return albums;
    }

    public ArrayList<String> getSingles() {
        return singles;
    }

    public void addAlbumId(String albumId) {
        albums.add(0,albumId);
        albums.remove(3);
    }

    public void addSingleId(String singleId) {
        singles.add(0, singleId);
        singles.remove(3);
    }
}
