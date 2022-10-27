package org.example;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchArtistsRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;

public class Spotify {
    private static final String clientId = "5893ed7df50f4010a6d7f06ba0465bdb";
    private static final String clientSecret = "daab854d59fa4a888a8b43a873c41a83";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();
    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
            .build();

    private static List<ArtistTemplate> artists;

    /**
     * Check if there are new singles/albums by checking the latest 3 of each artist(added to json) from spotify api and compare them with the 3 from json.
     * Then adds the difference to changes list.
     * @param serverId id of server method will be used on
     * @return List of AlbumSimplified object, if there are any new songs, otherwise returns empty list
     */
    public static List<AlbumSimplified> checkArtists_Async(String serverId) {
        initialize(serverId);
        if (!artists.isEmpty()) {
            List<AlbumSimplified> changes = new ArrayList<>();
            try {

                setAccessToken();

                for (ArtistTemplate artist : artists) {
                    final GetArtistsAlbumsRequest artistsAlbumsRequest = spotifyApi.getArtistsAlbums(artist.getId()).limit(3).album_type("album").build();
                    final CompletableFuture<Paging<AlbumSimplified>> albumsFuture = artistsAlbumsRequest.executeAsync();
                    final AlbumSimplified[] albums = albumsFuture.join().getItems();

                    final GetArtistsAlbumsRequest artistsSinglesRequest = spotifyApi.getArtistsAlbums(artist.getId()).limit(3).album_type("single").build();
                    final CompletableFuture<Paging<AlbumSimplified>> singlesFuture = artistsSinglesRequest.executeAsync();
                    final AlbumSimplified[] singles = singlesFuture.join().getItems();

                    for (int i = 0; i < albums.length; i++) {
                        if (!albums[i].getId().equals(artist.getAlbums().get(i))) {
                            changes.add(albums[i]);
                            artist.addAlbumId(albums[i].getId());
                        }
                    }

                    for (int i = 0; i < singles.length; i++) {
                        if (!singles[i].getId().equals(artist.getSingles().get(i))) {
                            changes.add(singles[i]);
                            artist.addSingleId(singles[i].getId());
                        }
                    }
                }
                return changes;
            } catch (CompletionException e) {
                return new ArrayList<>();
            } catch (CancellationException e) {
                System.out.println("Async operation cancelled.");
                return new ArrayList<>();
            }
        } else return new ArrayList<>();
    }

    /**
     * Add artist by providing his spotify link.
     * @param serverId id of server method will be used on
     * @param spotifyLink spotify link to the artist
     * @return String saying how this operation ended
     */
    public static String addArtist(String serverId, String spotifyLink) {
        initialize(serverId);
        try {
            setAccessToken();

            String id = getIdFromUrl(spotifyLink);

            if (id.equals("Wrong link")) return id;

            if (artists.stream().anyMatch(o -> id.equals(o.getId()))) return "Artist already added";

            final GetArtistsAlbumsRequest artistsAlbumsRequest = spotifyApi.getArtistsAlbums(id).limit(3).album_type("album").build();
            final CompletableFuture<Paging<AlbumSimplified>> albumsFuture = artistsAlbumsRequest.executeAsync();
            final AlbumSimplified[] albums = albumsFuture.join().getItems();

            final GetArtistsAlbumsRequest artistsSinglesRequest = spotifyApi.getArtistsAlbums(id).limit(3).album_type("single").build();
            final CompletableFuture<Paging<AlbumSimplified>> singlesFuture = artistsSinglesRequest.executeAsync();
            final AlbumSimplified[] singles = singlesFuture.join().getItems();

            final Artist artist = getArtist(id);

            ArrayList<String> albumsIds = new ArrayList<>();
            ArrayList<String> singlesIds = new ArrayList<>();

            for (AlbumSimplified album : albums) {
                albumsIds.add(album.getId());
            }
            for (AlbumSimplified single : singles) {
                singlesIds.add(single.getId());
            }

            artists.add(new ArtistTemplate(id, artist.getName(), albumsIds, singlesIds));

            JsonReader.save(serverId, artists);

            return "Added " + artist.getName();
        } catch (CompletionException e) {
            return ("Error: " + e.getCause().getMessage());
        } catch (CancellationException e) {
            return ("Async operation cancelled.");
        }
    }

    /**
     * Get artist object from json
     * @param serverId id of the server method will be used on
     * @param id id of the list
     * @return Spotify Artist object from list of json artists
     */
    public static Artist getArtistFromJson(String serverId, int id) {
        initialize(serverId);
        setAccessToken();
        if (id<artists.size() && id>=0) {
            final GetArtistRequest artistRequest = spotifyApi.getArtist(artists.get(id).getId()).build();
            return artistRequest.executeAsync().join();
        }
        else return null;
    }

    /**
     *
     * @param id spotify id
     * @return Spotify Artist object with given id
     */
    public static Artist getArtist(String id) {
        setAccessToken();
        final GetArtistRequest artistRequest = spotifyApi.getArtist(id).build();
        return artistRequest.executeAsync().join();
    }

    /**
     * Remove artist from json file with provided name/spotify link
     * @param serverId server id method will be used on
     * @param name name of an artist or spotify link
     * @return String saying how operation ended
     */
    public static String removeArtist(String serverId, String name) {
        initialize(serverId);
        String finalName = getIdFromUrl(name);
        if (!finalName.equals("Wrong link")) {
            if (artists.removeIf(artistTemplate -> (finalName.equalsIgnoreCase(artistTemplate.getName()) || finalName.equals(artistTemplate.getId())))) {
                JsonReader.save(serverId, artists);
                return "Artist deleted";
            }
            else return "None removed";
        }
        else {
            if (artists.removeIf(artistTemplate -> (name.equalsIgnoreCase(artistTemplate.getName()) || name.equals(artistTemplate.getId())))) {
                JsonReader.save(serverId, artists);
                return "Artist deleted";
            }
            else return "None removed";
        }
    }

    /**
     * Gets artist's id from given spotify link.
     * @param spotifyLink Spotify link to artist
     * @return id of an artist from link or 'Wrong link'
     */
    private static String getIdFromUrl(String spotifyLink) {
        Pattern pattern = Pattern.compile("https://open\\.spotify\\.com/artist/[a-zA-Z0-9]{22}");

        if (pattern.matcher(spotifyLink).find()) {
            URL url;
            try {
                url = new URL(spotifyLink);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return url.getPath().split("/")[2];
        }
        return "Wrong link";
    }

    private static void initialize(String serverId) {
        artists = JsonReader.getJSONArtists(serverId);
    }

    /**
     * Sets access token for spotify api
     */
    private static void setAccessToken() {
        final CompletableFuture<ClientCredentials> clientCredentialsFuture = clientCredentialsRequest.executeAsync();
        final ClientCredentials clientCredentials = clientCredentialsFuture.join();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
    }

    /**
     * Creates embed builder with all artists.
     * @param serverId server id method will be used on.
     * @return Embed builder containing all artist in embed fields
     */
    public static EmbedBuilder createArtistsEmbed(String serverId) {
        initialize(serverId);
        EmbedBuilder embed = new EmbedBuilder();
        for (ArtistTemplate artist: artists) {
            embed.addField(artist.getName(), "\u200b", true);
        }
        return embed;
    }

    /**
     * Gets number of artists stored in json file.
     * @param serverId server id method will be used on
     * @return number of artists stored in json file
     */
    public static int getArtistsCount(String serverId) {
        initialize(serverId);
        return artists.size();
    }

    /**
     * Search artist by name
     * @param artistsName name of an artist to search for
     * @param id page
     * @return Spotify Artist object found by spotify api
     */
    public static Artist searchArtist(String artistsName, int id) {
        setAccessToken();
        SearchArtistsRequest searchArtistsRequest = spotifyApi.searchArtists(artistsName).limit(1).offset(id).build();
        System.out.println(searchArtistsRequest.executeAsync().join().getItems()[0].getName());
        return searchArtistsRequest.executeAsync().join().getItems()[0];
    }
}