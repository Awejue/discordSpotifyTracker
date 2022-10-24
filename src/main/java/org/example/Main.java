package org.example;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static List<SelectMenuOption> artistMenu = Spotify.createArtistMenu();
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

        SlashCommand command3 = SlashCommand.with("show", "Show artists in tracking list", Arrays.asList(
                SlashCommandOption.createSubcommand("artists", "Show artists in tracking list")
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            System.out.println(interaction.getFullCommandName());
            if (interaction.getFullCommandName().equals("add artist")) {
               interaction.createImmediateResponder().setContent(Spotify.addArtist(interaction.getArguments().get(0).getStringValue().get())).respond();
            }
            else if (interaction.getFullCommandName().equals("remove artist")) {
                interaction.createImmediateResponder().setContent(Spotify.removeArtist(interaction.getArguments().get(0).getStringValue().get())).respond();
            }
            else if (interaction.getFullCommandName().equals("show artists")) {
                interaction.createImmediateResponder().setContent("Showing artists").respond();
                try {
                    new MessageBuilder().addAttachment(new URL(Spotify.getArtistFromJson(0).getImages()[0].getUrl()).openStream(), "2115.jpg").addComponents(ActionRow.of(
                            Button.primary("previous", "Previous artist"),
                            Button.primary("next", "Next artist")
                    )).addComponents(ActionRow.of(SelectMenu.create("artistsList", "Select artist", 1,1, artistMenu))).send(interaction.getChannel().get());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}