package org.example;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String token = "MTAxNzQ3MjYwNTE0MzQ0NTU5NA.GeGwIH.anCO13nArAe-SBhOuW__xpPNAuR7YqyS8id404";
        String spotifyClientID = "5893ed7df50f4010a6d7f06ba0465bdb";
        String spotifySecretID = "daab854d59fa4a888a8b43a873c41a83";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        SlashCommand command1 = SlashCommand.with("add", "Add artist to tracking list", Arrays.asList(
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "artist", "Add artist by spotify link", Arrays.asList(
                        SlashCommandOption.createStringOption("link", "The spotify link", true)
                ))
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        SlashCommand command2 = SlashCommand.with("remove", "Remove artist from tracking list", Arrays.asList(
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "artist", "Remove artist by id or name", Arrays.asList(
                        SlashCommandOption.createStringOption("name", "Name or id", true)
                ))
        )).setDefaultEnabledForPermissions(PermissionType.SEND_MESSAGES).createGlobal(api).join();

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            if (interaction.getFullCommandName().equals("add artist")) {
               interaction.createImmediateResponder().setContent(Spotify.addArtist(interaction.getArguments().get(0).getStringValue().get())).respond();
            }
            else if (interaction.getFullCommandName().equals("remove artist")) {
                interaction.createImmediateResponder().setContent(Spotify.removeArtist(interaction.getArguments().get(0).getStringValue().get())).respond();
            }
        });
    }
}