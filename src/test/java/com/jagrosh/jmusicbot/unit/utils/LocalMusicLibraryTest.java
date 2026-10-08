package com.jagrosh.jmusicbot.unit.utils;
import com.jagrosh.jmusicbot.utils.LocalMusicLibrary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
class LocalMusicLibraryTest {
    @TempDir Path root;
    @Test void searchesRecursivelyAndIgnoresNonAudio() throws Exception {
        Files.createDirectories(root.resolve("Album"));
        Files.writeString(root.resolve("Album/Song.FLAC"), "audio");
        Files.writeString(root.resolve("Song.txt"), "secret");
        assertEquals(1, LocalMusicLibrary.search(root, "song", 25).size());
        assertEquals("Song.FLAC", LocalMusicLibrary.search(root, "SONG", 25).getFirst().getFileName().toString());
    }
    @Test void rejectsTraversalAndNonAudio() throws Exception {
        Files.writeString(root.resolve("secret.txt"), "secret");
        assertThrows(IOException.class, () -> LocalMusicLibrary.search(root, "../outside.mp3", 25));
        assertThrows(IOException.class, () -> LocalMusicLibrary.search(root, "secret.txt", 25));
    }
    @Test void exactSelectionDisambiguatesAndResultsAreBounded() throws Exception {
        Files.writeString(root.resolve("Song.mp3"), "a");
        Files.writeString(root.resolve("Song live.mp3"), "b");
        assertEquals(2, LocalMusicLibrary.search(root, "Song", 25).size());
        assertEquals(1, LocalMusicLibrary.search(root, "Song.mp3", 25).size());
        assertEquals(1, LocalMusicLibrary.search(root, "", 1).size());
        assertTrue(LocalMusicLibrary.search(root, "missing", 25).isEmpty());
    }
}
