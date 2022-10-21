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
        String spotifyClientID = "5893ed7df50f4010a6d7f06ba0465bdb";
        String spotifySecretID = "daab854d59fa4a888a8b43a873c41a83";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

    }
}