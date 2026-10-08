package com.jagrosh.jmusicbot.commands.v2.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.v2.MusicSlashCommand;
import com.jagrosh.jmusicbot.commands.v2.SlashOutputAdapters.InteractionHookOutputAdapter;
import com.jagrosh.jmusicbot.utils.LocalMusicLibrary;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.nio.file.Path;
import java.util.*;

/** Local library commands alongside the existing /play command. */
public class MusicLibrarySlashCmd extends SlashCommand {
    public MusicLibrarySlashCmd(Bot bot) {
        name = "music";
        help = "Search and play your local music library";
        guildOnly = true;
        children = new SlashCommand[]{new LocalCommand(bot, true), new LocalCommand(bot, false)};
    }
    @Override protected void execute(SlashCommandEvent event) {}

    public static class LocalCommand extends MusicSlashCommand {
        private final boolean play;
        public LocalCommand(Bot bot, boolean play) {
            super(bot);
            this.play = play;
            name = play ? "play" : "local";
            help = play ? "Play a song from the local library" : "Browse or search local music";
            beListening = play;
            options = List.of(new OptionData(OptionType.STRING, "local", "Song name or relative library path", play).setAutoComplete(true));
        }
        @Override public void doCommand(SlashCommandEvent event) {
            if (!bot.getConfig().getEnabledAudioSources().contains(com.jagrosh.jmusicbot.audio.AudioSource.LOCAL)) {
                event.reply("Local playback is disabled in the bot configuration.").setEphemeral(true).queue();
                return;
            }
            String query = event.getOption("local") == null ? "" : event.getOption("local").getAsString();
            event.deferReply(!play).queue(hook -> bot.getThreadpool().execute(() -> {
                try {
                    Path root = Path.of(bot.getConfig().getLocalMusicFolder()).toRealPath();
                    List<Path> matches = LocalMusicLibrary.search(root, query, 25);
                    if (matches.isEmpty()) {
                        hook.editOriginal("No matching audio files. Add songs to the configured Music folder.").queue();
                    } else if (play && matches.size() == 1) {
                        bot.getMusicService().play(event.getGuild(), event.getMember(), matches.getFirst().toString(), event.getTextChannel(),
                                new InteractionHookOutputAdapter(hook, event.getJDA(), event.getClient().getWarning()));
                    } else {
                        StringBuilder result = new StringBuilder(play ? "Multiple matches. Choose a file with autocomplete or use its relative path:\n" : "Local music (up to 25 matches):\n");
                        for (Path file : matches) {
                            String line = root.relativize(file).toString().replace('`', '\'').replace("@", "@\u200b");
                            if (result.length() + line.length() + 4 > 1900) break;
                            result.append('`').append(line).append("`\n");
                        }
                        hook.editOriginal(result.toString()).queue();
                    }
                } catch (Exception ex) {
                    hook.editOriginal("Cannot read the local library. Check paths.localMusicFolder and choose an audio file inside it.").queue();
                }
            }));
        }
        @Override public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
            bot.getThreadpool().execute(() -> {
                try {
                    Path root = Path.of(bot.getConfig().getLocalMusicFolder()).toRealPath();
                    List<Command.Choice> choices = new ArrayList<>();
                    for (Path file : LocalMusicLibrary.search(root, event.getFocusedOption().getValue(), 25)) {
                        String relative = root.relativize(file).toString();
                        if (relative.length() <= 100) choices.add(new Command.Choice(relative, relative));
                    }
                    event.replyChoices(choices).queue();
                } catch (Exception ex) { event.replyChoices().queue(); }
            });
        }
    }
}
