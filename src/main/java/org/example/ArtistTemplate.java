package org.example;


import java.util.ArrayList;

@SuppressWarnings({"unused", "ConstantConditions"})
public class ArtistTemplate {
    final private String id;

    final private String name;

    final private ArrayList<String> albums;
    final private ArrayList<String> singles;

    /**
     * Default constructor
     */
    public ArtistTemplate() {
        this.id = null;
        this.name = null;
        this.albums = null;
        this.singles = null;
    }

    /**
     *
     * @param id id of an artist
     * @param name name of an artist
     * @param albums his last 3 albums in array list
     * @param singles his last 3 singles in array list
     */
    public ArtistTemplate(String id, String name, ArrayList<String> albums, ArrayList<String> singles) {
        this.id = id;
        this.name = name;
        this.albums = albums;
        this.singles = singles;
    }

    /**
     *
     * @return id of an artis
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return name of an artist
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return albums of an artist
     */
    public ArrayList<String> getAlbums() {
        return albums;
    }

    /**
     *
     * @return singles of an artist
     */
    public ArrayList<String> getSingles() {
        return singles;
    }

    /**
     * Adds new album at the begging and deletes the oldest one
     * @param albumId new album id
     */
    public void addAlbumId(String albumId) {
        albums.add(0,albumId);
        albums.remove(3);
    }

    /**
     * Adds new single at the begging and deletes the oldest one
     * @param singleId new single id
     */
    public void addSingleId(String singleId) {
        singles.add(0, singleId);
        singles.remove(3);
    }
}
