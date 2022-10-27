package org.example;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.*;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "unused", "ConstantConditions"})
public class Main {
    private static EmbedBuilder artistMenu;
    private static int id;

    private static Artist showArtist;

    public static void main(String[] args) {
        String token = "MTAzNTE0MTQzMTY5OTk4MDI4OA.GPRlgg.j3Gxp4G3Qq9j27rjjUEU8O2aI8fyX-VN0i2M5s";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        SlashCommand command1 = SlashCommand.with("add", "Add artist to tracking list", Collections.singletonList(
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "artist", "Add artist by spotify link", Collections.singletonList(
                        SlashCommandOption.createStringOption("link", "The spotify link", true)
                ))
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        SlashCommand command2 = SlashCommand.with("remove", "Remove artist from tracking list", Collections.singletonList(
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "artist", "Remove artist by link or name", Collections.singletonList(
                        SlashCommandOption.createStringOption("nameLink", "Name or link", true)
                ))
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        SlashCommand command3 = SlashCommand.with("show", "Show artists on tracking list", Collections.singletonList(
                SlashCommandOption.createSubcommand("artists", "Show artists on tracking list")
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        SlashCommand command4 = SlashCommand.with("list", "List artists on tracker list", Collections.singletonList(
                SlashCommandOption.createSubcommand("artists", "List artists on tracker list")
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        SlashCommand command5 = SlashCommand.with("set", "Set channel to send updates", Collections.singletonList(
                SlashCommandOption.createSubcommand("channel", "Set channel to send updates")
        )).setDefaultEnabledForPermissions(PermissionType.VIEW_CHANNEL, PermissionType.SEND_MESSAGES).createGlobal(api).join();

        SlashCommand command6 = SlashCommand.with("search", "Search for an artist", Arrays.asList(
                SlashCommandOption.createStringOption("artistName", "Artist's name", true),
                SlashCommandOption.createDecimalOption("page", "Select page", false)
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Server server:api.getServers().stream().collect(Collectors.toList())) {
                    String serverId = String.valueOf(server.getId());
                    String textChannelId = JsonReader.getChannelId(serverId);
                    List<AlbumSimplified> changes = Spotify.checkArtists_Async(serverId);
                    if (!changes.isEmpty() && textChannelId != null) {
                        for (AlbumSimplified album: changes) {
                            StringBuilder artists= new StringBuilder();
                            for (ArtistSimplified artist : album.getArtists()) {
                                artists.append(artist.getName()).append(" | ");
                            }
                            new MessageBuilder().addEmbed(new EmbedBuilder()
                                            .setAuthor(
                                                    artists.toString(),
                                                    album.getArtists()[0].getExternalUrls().get("spotify"),
                                                    Spotify.getArtist(album.getArtists()[0].getId()).getImages()[0].getUrl())
                                            .setTitle(album.getName())
                                            .setDescription(String.valueOf(album.getAlbumType()))
                                            .setUrl(album.getExternalUrls().get("spotify"))
                                            .setImage(album.getImages()[0].getUrl()))
                                    .send(api.getTextChannelById(textChannelId).get());
                        }
                    }
                }
            }
        }, 0, 14400000);

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            String serverId = String.valueOf(interaction.getServer().get().getId());
            String image;
            if (interaction.getFullCommandName().equals("add artist")) {
                interaction.createImmediateResponder().setContent(
                        Spotify.addArtist(
                                serverId, interaction.getArguments().get(0).getStringValue().get()))
                        .respond();
                artistMenu = Spotify.createArtistsEmbed(serverId);
            } else if (interaction.getFullCommandName().equals("remove artist")) {
                interaction.createImmediateResponder().setContent(
                        Spotify.removeArtist(
                                serverId, interaction.getArguments().get(0).getStringValue().get()))
                        .respond();
                artistMenu = Spotify.createArtistsEmbed(serverId);
            } else if (interaction.getFullCommandName().equals("show artists")) {
                if (Spotify.getArtistsCount(serverId)==0) {
                    interaction.createImmediateResponder().setContent("No artists on the list").respond();
                    return;
                }
                interaction.createImmediateResponder().setContent("Showing artists").respond();
                showArtist = Spotify.getArtistFromJson(serverId, 0);
                image = showArtist.getImages().length != 0 ? showArtist.getImages()[0].getUrl() : null;
                boolean onlyOne;
                onlyOne = Spotify.getArtistsCount(serverId) == 1;

                new MessageBuilder().addEmbed(new EmbedBuilder()
                                .setImage(image)
                                .setTitle(showArtist.getName()))
                        .addComponents(ActionRow.of(
                                Button.primary("previous", "Previous artist", true),
                                Button.primary("next", "Next artist", onlyOne)))
                        .send(interaction.getChannel().get());
                id = 0;
            }
            else if (interaction.getFullCommandName().equals("list artists")) {
                artistMenu = Spotify.createArtistsEmbed(serverId);
                interaction.createImmediateResponder().addEmbed(artistMenu).respond();
            }
            else if(interaction.getFullCommandName().equals("set channel")) {
                JsonReader.setChannelId(serverId, String.valueOf(interaction.getChannel().get().getId()));
                interaction.createImmediateResponder().setContent("Channel changed").respond();
            }
            else if (interaction.getFullCommandName().equals("search")) {
                String searchName = interaction.getArguments().get(0).getStringValue().get();
                int id = interaction.getArguments().size() == 2 ? interaction.getArguments().get(1).getDecimalValue().get().intValue() : 0;
                Artist artist = Spotify.searchArtist(searchName, id);
                new MessageBuilder()
                        .addEmbed(new EmbedBuilder().
                                setTitle(artist.getName()).
                                setUrl(artist.getExternalUrls().get("spotify")).
                                setImage(artist.getImages().length!=0 ? artist.getImages()[0].getUrl() : null)
                                .addField("Followers", String.valueOf(artist.getFollowers().getTotal())))
                        .addComponents(ActionRow.of(Collections.singletonList(
                                Button.primary("add", "Add to tracking list")
                        )))
                        .send(interaction.getChannel().get());
                interaction.createImmediateResponder().setContent("\u200b").respond();
            }
        });

        api.addMessageComponentCreateListener(event -> {
            MessageComponentInteraction interaction = event.getMessageComponentInteraction();
            String serverId = String.valueOf(interaction.getServer().get().getId());
            boolean first;
            boolean last;
            String image;
            if (interaction.getCustomId().equals("previous")) {
                id--;
                showArtist = Spotify.getArtistFromJson(serverId, id);
                first = 0 == id;
                image = showArtist.getImages().length != 0 ? showArtist.getImages()[0].getUrl() : null;
                interaction.getMessage().delete();
                new MessageBuilder()
                        .setEmbed(new EmbedBuilder().setImage(
                                image)
                                .setTitle(showArtist.getName()))
                        .addComponents(ActionRow.of(
                                Button.primary("previous", "Previous artist", first),
                                Button.primary("next", "Next artist")))
                        .send(interaction.getChannel().get());
            } else if (interaction.getCustomId().equals("next")) {
                id++;
                showArtist = Spotify.getArtistFromJson(serverId, id);
                last = id == Spotify.getArtistsCount(serverId) - 1;
                image = showArtist.getImages().length != 0 ? showArtist.getImages()[0].getUrl() : null;
                interaction.getMessage().delete();
                new MessageBuilder()
                        .addEmbed(new EmbedBuilder().setImage(
                                        image)
                                .setTitle(showArtist.getName()))
                        .addComponents(ActionRow.of(
                                Button.primary("previous", "Previous artist"),
                                Button.primary("next", "Next artist", last)))
                        .send(interaction.getChannel().get());
            }
            else if (interaction.getCustomId().equals("add")) {
                String spotifyLink = interaction.getMessage().getEmbeds().get(0).getUrl().get().toString();
                interaction.createImmediateResponder().setContent(Spotify.addArtist(serverId, spotifyLink)).respond();
            }
        });
    }
}