package com.jagrosh.jmusicbot.utils;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/** A bounded, read-only library. Never follows directory links outside the music root. */
public final class LocalMusicLibrary {
    private static final Set<String> EXTENSIONS = Set.of("mp3", "flac", "wav", "webm", "mkv", "mp4", "m4a", "ogg", "opus", "aac");
    private LocalMusicLibrary() {}

    public static List<Path> search(Path folder, String query, int limit) throws IOException {
        if (limit < 1) return List.of();
        Path root = folder.toRealPath();
        String input = query == null ? "" : query.trim();
        if (!input.isEmpty()) {
            Path requested = root.resolve(input).normalize();
            if (!requested.startsWith(root)) throw new IOException("Choose a file inside the music library.");
            if (Files.isRegularFile(requested)) {
                Path real = requested.toRealPath();
                if (!real.startsWith(root) || !isAudio(real)) throw new IOException("Choose an audio file inside the music library.");
                return List.of(real);
            }
        }
        String needle = input.toLowerCase(Locale.ROOT);
        try (Stream<Path> files = Files.walk(root, 20)) {
            return files.limit(10000)
                    .filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
                    .filter(LocalMusicLibrary::isAudio)
                    .filter(p -> insideRoot(p, root))
                    .filter(p -> root.relativize(p).toString().toLowerCase(Locale.ROOT).contains(needle))
                    .sorted(Comparator.comparing(p -> root.relativize(p).toString().toLowerCase(Locale.ROOT)))
                    .limit(limit).toList();
        }
    }

    private static boolean isAudio(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot >= 0 && EXTENSIONS.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    private static boolean insideRoot(Path file, Path root) {
        try { return file.toRealPath().startsWith(root); }
        catch (IOException ex) { return false; }
    }
}
