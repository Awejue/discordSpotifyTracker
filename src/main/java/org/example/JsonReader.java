package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static List<ArtistTemplate> map;

    private static File json = new File("config/json/server_id.json");

    static {
        try {
            if (!json.createNewFile() && !(json.length()==0 || json.length()==2)){
                map = mapper.readValue(json, new TypeReference<List<ArtistTemplate>>() {});
            }
            else map = new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArtistTemplate getJSONArtist(int id) {
        return map.get(id);
    }

    public static List<ArtistTemplate> getJSONArtists() {
        return map;
    }

    public static void save(List<ArtistTemplate> artists) {
        try {
            mapper.writeValue(new File("config/json/server_id.json"), artists);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
