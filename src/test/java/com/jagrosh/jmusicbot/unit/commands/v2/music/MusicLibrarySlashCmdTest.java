package com.jagrosh.jmusicbot.unit.commands.v2.music;
import com.jagrosh.jmusicbot.commands.v2.music.MusicLibrarySlashCmd;
import com.jagrosh.jmusicbot.testutil.commands.SlashCommandTestFixture;
import com.jagrosh.jmusicbot.audio.AudioSource;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class MusicLibrarySlashCmdTest {
    @TempDir Path root;
    @Test void registersValidDiscordSubcommands() {
        var fixture = SlashCommandTestFixture.create();
        var command = new MusicLibrarySlashCmd(fixture.getBot());
        String payload = command.buildCommandData().toData().toString();
        assertTrue(payload.contains("music"));
        assertEquals("play", command.getChildren()[0].getName());
        assertEquals("local", command.getChildren()[1].getName());
        assertTrue(command.getChildren()[0].getOptions().getFirst().isRequired());
        assertFalse(command.getChildren()[1].getOptions().getFirst().isRequired());
    }
    @Test void exactLocalSelectionQueuesButAmbiguousSearchDoesNot() throws Exception {
        var fixture = SlashCommandTestFixture.create();
        fixture.withReplyQueueCallback();
        when(fixture.getConfig().getEnabledAudioSources()).thenReturn(Set.of(AudioSource.LOCAL));
        when(fixture.getConfig().getLocalMusicFolder()).thenReturn(root.toString());
        var executor = mock(ScheduledExecutorService.class);
        doAnswer(call -> { call.<Runnable>getArgument(0).run(); return null; }).when(executor).execute(any(Runnable.class));
        when(fixture.getBot().getThreadpool()).thenReturn(executor);
        when(fixture.getEvent().deferReply(anyBoolean())).thenReturn(fixture.getReplyAction());
        var option = mock(OptionMapping.class);
        when(fixture.getEvent().getOption("local")).thenReturn(option);
        Files.writeString(root.resolve("Song.mp3"), "a");
        Files.writeString(root.resolve("Song live.mp3"), "b");
        var command = new MusicLibrarySlashCmd.LocalCommand(fixture.getBot(), true);
        when(option.getAsString()).thenReturn("Song");
        command.doCommand(fixture.getEvent());
        verifyNoInteractions(fixture.getMusicService());
        when(option.getAsString()).thenReturn("Song.mp3");
        command.doCommand(fixture.getEvent());
        verify(fixture.getMusicService()).play(eq(fixture.getGuild()), eq(fixture.getMember()),
                eq(root.resolve("Song.mp3").toRealPath().toString()), eq(fixture.getTextChannel()), any());
    }
    @Test void voiceChatReceivesExplanationBeforeTextChannelConversion() {
        var fixture = SlashCommandTestFixture.create();
        when(fixture.getEvent().getChannelType()).thenReturn(ChannelType.VOICE);
        var command = new MusicLibrarySlashCmd.LocalCommand(fixture.getBot(), true) {
            public void check() { super.execute(fixture.getEvent()); }
        };
        command.check();
        verify(fixture.getEvent()).reply(contains("regular server text channel"));
        verify(fixture.getEvent(), never()).getTextChannel();
        verifyNoInteractions(fixture.getMusicService());
    }
}
