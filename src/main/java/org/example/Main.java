package org.example;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.*;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static EmbedBuilder artistMenu;
    private static int id;
    private static Artist showArtist;

    private static InputStream image;

    public static void main(String[] args) {
        String token = "MTAxNzQ3MjYwNTE0MzQ0NTU5NA.GeGwIH.anCO13nArAe-SBhOuW__xpPNAuR7YqyS8id404";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        SlashCommand command1 = SlashCommand.with("add", "Add artist to tracking list", Arrays.asList(
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "artist", "Add artist by spotify link", Arrays.asList(
                        SlashCommandOption.createStringOption("link", "The spotify link", true)
                ))
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        SlashCommand command2 = SlashCommand.with("remove", "Remove artist from tracking list", Arrays.asList(
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "artist", "Remove artist by link or name", Arrays.asList(
                        SlashCommandOption.createStringOption("nameLink", "Name or link", true)
                ))
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        SlashCommand command3 = SlashCommand.with("show", "Show artists on tracking list", Arrays.asList(
                SlashCommandOption.createSubcommand("artists", "Show artists on tracking list")
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        SlashCommand command4 = SlashCommand.with("list", "List artists on tracker list", Arrays.asList(
                SlashCommandOption.createSubcommand("artists", "List artists on tracker list")
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        SlashCommand command5 = SlashCommand.with("set", "Set channel to send updates", Arrays.asList(
                SlashCommandOption.createSubcommand("channel", "Set channel to send updates")
        )).setDefaultEnabledForPermissions(PermissionType.VIEW_CHANNEL, PermissionType.SEND_MESSAGES).createGlobal(api).join();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(api.getServers().stream().toList().get(0).getId());
                String serverId = String.valueOf(api.getServers().stream().toList().get(0).getId());
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
        }, 0, 10000);

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            if (interaction.getFullCommandName().equals("add artist")) {
                interaction.createImmediateResponder().setContent(Spotify.addArtist(String.valueOf(interaction.getServer().get().getId()), interaction.getArguments().get(0).getStringValue().get())).respond();
                artistMenu = Spotify.createArtistsEmbed(String.valueOf(interaction.getServer().get().getId()));
            } else if (interaction.getFullCommandName().equals("remove artist")) {
                interaction.createImmediateResponder().setContent(Spotify.removeArtist(String.valueOf(interaction.getServer().get().getId()), interaction.getArguments().get(0).getStringValue().get())).respond();
                artistMenu = Spotify.createArtistsEmbed(String.valueOf(interaction.getServer().get().getId()));
            } else if (interaction.getFullCommandName().equals("show artists")) {
                interaction.createImmediateResponder().setContent("Showing artists").respond();
                try {
                    id = 0;
                    showArtist = Spotify.getArtistFromJson(String.valueOf(interaction.getServer().get().getId()), id);
                    image = new URL(showArtist.getImages()[0].getUrl()).openStream();

                    new MessageBuilder().addEmbed(new EmbedBuilder().setImage(
                                            image, "png"
                                    )
                                    .setTitle(showArtist.getName()))
                            .addComponents(ActionRow.of(
                                    Button.primary("previous", "Previous artist"),
                                    Button.primary("next", "Next artist")))
                            .send(interaction.getChannel().get());
                } catch (MalformedURLException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            else if (interaction.getFullCommandName().equals("list artists")) {
                interaction.createImmediateResponder().addEmbed(artistMenu).respond();
            }
            else if(interaction.getFullCommandName().equals("set channel")) {
                JsonReader.setChannelId(String.valueOf(interaction.getServer().get().getId()), String.valueOf(interaction.getChannel().get().getId()));
                interaction.createImmediateResponder().setContent("Channel changed").respond();
            }
        });

        api.addMessageComponentCreateListener(event -> {
            MessageComponentInteraction interaction = event.getMessageComponentInteraction();
            if (interaction.getCustomId().equals("previous")) {
                id--;
                if ((showArtist = Spotify.getArtistFromJson(String.valueOf(interaction.getServer().get().getId()), id)) != null) {
                    try {
                        image.close();
                        image = new URL(showArtist.getImages()[0].getUrl()).openStream();
                        interaction.getMessage().delete();
                        new MessageBuilder()
                                .setEmbed(new EmbedBuilder().setImage(
                                        image, "png")
                                        .setTitle(showArtist.getName()))
                                .addComponents(ActionRow.of(
                                        Button.primary("previous", "Previous artist"),
                                        Button.primary("next", "Next artist")))
                                .send(interaction.getChannel().get());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else id++;
            } else if (interaction.getCustomId().equals("next")) {
                id++;
                if ((showArtist = Spotify.getArtistFromJson(String.valueOf(interaction.getServer().get().getId()), id)) != null) {
                    try {
                        image.close();
                        image = new URL(showArtist.getImages()[0].getUrl()).openStream();
                        interaction.getMessage().delete();
                        new MessageBuilder()
                                .addEmbed(new EmbedBuilder().setImage(
                                        image,  "png")
                                        .setTitle(showArtist.getName()))
                                .addComponents(ActionRow.of(
                                        Button.primary("previous", "Previous artist"),
                                        Button.primary("next", "Next artist")))
                                .send(interaction.getChannel().get());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else id--;
            }
        });
    }
}