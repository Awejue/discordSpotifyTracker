package org.example;

import org.javacord.api.entity.message.component.SelectMenuOption;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsAlbumsRequest;

import java.awt.image.BufferedImage;
import java.io.IOException;
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

    public static List<AlbumSimplified> checkArtists_Async() {
        initialize();
        if (!artists.isEmpty()) {
            try {
                List<AlbumSimplified> changes = new ArrayList<>();

                setAccessToken();

                for (ArtistTemplate artist : artists) {
                    final GetArtistsAlbumsRequest artistsAlbumsRequest = spotifyApi.getArtistsAlbums(artist.getId()).limit(3).album_type("album").build();
                    final CompletableFuture<Paging<AlbumSimplified>> albumsFuture = artistsAlbumsRequest.executeAsync();
                    final AlbumSimplified[] albums = albumsFuture.join().getItems();

                    final GetArtistsAlbumsRequest artistsSinglesRequest = spotifyApi.getArtistsAlbums(artist.getId()).limit(3).album_type("single").build();
                    final CompletableFuture<Paging<AlbumSimplified>> singlesFuture = artistsSinglesRequest.executeAsync();
                    final AlbumSimplified[] singles = singlesFuture.join().getItems();

                    for (int i = 0; i < albums.length; i++) {
                        if (albums[i].getId().equals(artist.getAlbums().get(i))) {
                        } else {
                            changes.add(albums[i]);
                            artist.addAlbumId(albums[i].getId());
                        }
                    }

                    for (int i = 0; i < singles.length; i++) {
                        if (singles[i].getId().equals(artist.getSingles().get(i))) {
                        } else {
                            changes.add(singles[i]);
                            artist.addSingleId(singles[i].getId());
                        }
                    }
                }
                JsonReader.save(artists);
                return changes;
            } catch (CompletionException e) {
                System.out.println("Error: " + e.getCause().getMessage());
                return null;
            } catch (CancellationException e) {
                System.out.println("Async operation cancelled.");
                return null;
            }
        }
        return null;
    }

    public static String addArtist(String spotifyLink) {
        initialize();
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

            for (int i = 0;i<albums.length;i++){
                albumsIds.add(albums[i].getId());
            }
            for (int i = 0;i<singles.length;i++) {
                singlesIds.add(singles[i].getId());
            }

            artists.add(new ArtistTemplate(id, artist.getName(), albumsIds, singlesIds));

            JsonReader.save(artists);

            return "Added";
        } catch (CompletionException e) {
            return ("Error: " + e.getCause().getMessage());
        } catch (CancellationException e) {
            return ("Async operation cancelled.");
        }
    }


    public static Artist getArtistFromJson(int id) {
        initialize();
        setAccessToken();
        final GetArtistRequest artistRequest = spotifyApi.getArtist(artists.get(id).getId()).build();
        return artistRequest.executeAsync().join();
    }

    public static Artist getArtist(String id) {
        final GetArtistRequest artistRequest = spotifyApi.getArtist(id).build();
        return artistRequest.executeAsync().join();
    }

    public static String removeArtist(String name) {
        initialize();
        String finalName = getIdFromUrl(name);
        if (!finalName.equals("Wrong link")) {
            if (artists.removeIf(artistTemplate -> (finalName.equals(artistTemplate.getName()) || finalName.equals(artistTemplate.getId())))) {
                JsonReader.save(artists);
                return "Artist deleted";
            }
            else return "None removed";
        }
        else {
            if (artists.removeIf(artistTemplate -> (name.equals(artistTemplate.getName()) || name.equals(artistTemplate.getId())))) {
                JsonReader.save(artists);
                return "Artist deleted";
            }
            else return "None removed";
        }
    }

    private static String getIdFromUrl(String spotifyLink) {
        Pattern pattern = Pattern.compile("https://open\\.spotify\\.com/artist/[a-zA-Z0-9]{22}");

        if (pattern.matcher(spotifyLink).find()) {
            URL url = null;
            try {
                url = new URL(spotifyLink);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return url.getPath().split("/")[2];
        }
        return "Wrong link";
    }

    private static void initialize() {
        artists = JsonReader.getJSONArtists();
    }

    private static void setAccessToken() {
        final CompletableFuture<ClientCredentials> clientCredentialsFuture = clientCredentialsRequest.executeAsync();
        final ClientCredentials clientCredentials = clientCredentialsFuture.join();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
    }

    public static List<SelectMenuOption> createArtistMenu() {
        initialize();
        List<SelectMenuOption> artistMenu = new ArrayList<>();
        for (ArtistTemplate artist:
             artists) {
            artistMenu.add(SelectMenuOption.create(artist.getName(),artist.getName(), "Show "+artist.getName()));
        }
        return artistMenu;
    }
}
