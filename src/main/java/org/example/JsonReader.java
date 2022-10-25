package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonReader {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static List<ArtistTemplate> map;

    private static File json;

    private static File channelJson;

    public static void setJson(String serverId) {
        json = new File("config/json/"+serverId+".json");
        try {
            json.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void setChannelJson(String serverId) {
        channelJson = new File("config/json/"+serverId+"Channel.json");
        try {
            channelJson.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ArtistTemplate> getJSONArtists(String serverId) {
        try {
            setJson(serverId);
            if (!(json.length()==0 || json.length()==2)){
                map = mapper.readValue(json, new TypeReference<List<ArtistTemplate>>() {});
            }
            else map = new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static String getChannelId(String serverId) {
        setChannelJson(serverId);
        try {
            if (!(channelJson.length()==0 || channelJson.length()==2)) {
                return mapper.readValue(channelJson, java.util.Map.class).get("id").toString();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void setChannelId(String serverId, String id) {
        try {
            setChannelJson(serverId);
            Map<String, Object> channelId = new HashMap<>();
            channelId.put("id", id);
            mapper.writeValue(channelJson, channelId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void save(String serverId, List<ArtistTemplate> artists) {
        try {
            setJson(serverId);
            mapper.writeValue(json, artists);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
