package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"ResultOfMethodCallIgnored", "Convert2Diamond"})
public class JsonReader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static File json;

    private static File channelJson;

    /**
     * Sets json file to use
     * @param serverId id of the server
     */
    public static void setJson(String serverId) {
        json = new File("config/json/"+serverId+".json");
        try {
            json.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets json file with channel id
     * @param serverId id of the server
     */
    public static void setChannelJson(String serverId) {
        channelJson = new File("config/json/"+serverId+"Channel.json");
        try {
            channelJson.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets artists from the json file
     * @param serverId id of the server
     * @return List of ArtistTemplate from json file
     */
    public static List<ArtistTemplate> getJSONArtists(String serverId) {
        List<ArtistTemplate> map;
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

    /**
     * Gets the channel id
     * @param serverId id of the server
     * @return channel id
     */
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

    /**
     * Sets the channel id
     * @param serverId id of the server
     * @param id id of the new channel
     */
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

    /**
     * Saves new values to json file
     * @param serverId id of the server
     * @param artists list of artists in artist template
     */
    public static void save(String serverId, List<ArtistTemplate> artists) {
        try {
            setJson(serverId);
            mapper.writeValue(json, artists);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
