package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.*;

public class JsonReader {
    private static JSONObject obj = new JSONObject("../../../config/json/server_id.json");
    private static final JSONArray JSONartists = obj.getJSONArray("artists");

    public static ArtistTemplate getJSONArtist(int id) {
        JSONObject artist = JSONartists.getJSONObject(id);
        return new ArtistTemplate(artist.getString("id"), artist.getJSONObject("releases").getJSONArray("albums"), artist.getJSONObject("releases").getJSONArray("singles"));
    }

    public static ArtistTemplate[] getJSONArtists() {
        ArtistTemplate[] artists = new ArtistTemplate[JSONartists.length()];
        for (int i = 0;i<JSONartists.length();i++) {
            JSONObject artist = JSONartists.getJSONObject(i);
            artists[i] = new ArtistTemplate(artist.getString("id"), artist.getJSONArray("albums"), artist.getJSONArray("singles"));
        }
        return artists;
    }
}
