package org.example;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;

public class Main {
    public static void main(String[] args) {
        String token = "MTAxNzQ3MjYwNTE0MzQ0NTU5NA.GeGwIH.anCO13nArAe-SBhOuW__xpPNAuR7YqyS8id404";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        String regex = "<@"+api.getClientId() + ">";

        SlashCommand command = SlashCommand.with("test", "TEst command").createGlobal(api).join();

        api.addSlashCommandCreateListener( event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            if (interaction.getCommandName().equals("test")) {
                interaction.createImmediateResponder().setContent("kox").addComponents(
                        ActionRow.of(
                                Button.primary("feed", "Nakarm")
                        )
                ).respond();
            }
        });
    }
}