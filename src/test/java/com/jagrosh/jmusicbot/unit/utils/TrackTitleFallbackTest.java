package com.jagrosh.jmusicbot.unit.utils;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.track.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class TrackTitleFallbackTest {
    private AudioTrack track(String title, String id) {
        AudioTrack track = mock(AudioTrack.class);
        when(track.getInfo()).thenReturn(new AudioTrackInfo(title, "artist", 100, id, false, id));
        when(track.getIdentifier()).thenReturn(id);
        return track;
    }
    @Test void preservesSourceTitlesForEveryProvider() {
        for (String source : new String[]{"youtube", "soundcloud", "bandcamp", "vimeo", "twitch", "http", "local"})
            assertEquals("Artist - Song", FormatUtil.getTrackTitle(track("Artist - Song", source)));
    }
    @Test void missingHttpTitleUsesDecodedFilenameWithoutSecrets() {
        assertEquals("My Song.mp3", FormatUtil.getTrackTitle(track(null, "https://example.com/My%20Song.mp3?token=secret")));
    }
    @Test void handlesEmptyMetadataAndLongNames() {
        assertEquals("Untitled audio", FormatUtil.getTrackTitle(track("", null)));
        assertEquals("radio.example", FormatUtil.getTrackTitle(track("Unknown title", "https://radio.example/")));
        assertEquals("Untitled audio stream", FormatUtil.getTrackTitle(track(null, "https://bad_host/")));
        assertEquals("song.mp3", FormatUtil.getTrackTitle(track("\u202e", "C:/Music/song.mp3")));
        assertEquals(100, FormatUtil.getTrackTitle(track(null, "C:/Music/" + "a".repeat(150) + ".mp3")).length());
    }
}
