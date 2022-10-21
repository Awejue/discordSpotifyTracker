package org.example;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import se.michaelthelin.spotify.requests.data.artists.GetSeveralArtistsRequest;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Spotify {
    private static final String clientId = "5893ed7df50f4010a6d7f06ba0465bdb";
    private static final String clientSecret = "daab854d59fa4a888a8b43a873c41a83";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();
    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
            .build();

    private static ArtistTemplate[] artists = JsonReader.getJSONArtists();

    public static void clientCredentials_Async() {
        try {
            final CompletableFuture<ClientCredentials> clientCredentialsFuture = clientCredentialsRequest.executeAsync();
            final ClientCredentials clientCredentials = clientCredentialsFuture.join();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            for (ArtistTemplate artist: artists) {
                final GetArtistsAlbumsRequest artistsAlbumsRequest = spotifyApi.getArtistsAlbums(artist.getId()).limit(3).album_type("album").build();
                final CompletableFuture<Paging<AlbumSimplified>> albumsFuture = artistsAlbumsRequest.executeAsync();
                final AlbumSimplified[] albums = albumsFuture.join().getItems();

                final GetArtistsAlbumsRequest artistsSinglesRequest = spotifyApi.getArtistsAlbums(artist.getId()).limit(3).album_type("single").build();
                final CompletableFuture<Paging<AlbumSimplified>> singlesFuture = artistsSinglesRequest.executeAsync();
                final AlbumSimplified[] singles = singlesFuture.join().getItems();

                System.out.println(singles[0].getName()+"   "+albums[0].getName());
            }


        } catch (CompletionException e) {
            System.out.println("Error: " + e.getCause().getMessage());
        } catch (CancellationException e) {
            System.out.println("Async operation cancelled.");
        }
    }

    public static void main(String[] args) {
        clientCredentials_Async();
    }
}
