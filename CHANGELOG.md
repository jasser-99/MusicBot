# Changelog

Changes in jasser-99/MusicBot relative to arif-banai/MusicBot. The application version remains 0.7.0; these changes have not been tagged as a new release.

## Unreleased — 2026-10-08

### Added

- Shared song-title handling for source metadata and safe fallbacks across supported sources, including online play autocomplete and now-playing displays.
- Local library search and autocomplete through `/music play local:<query>`, and browsing through `/music local`.
- Configurable `paths.localMusicFolder` (default `Music`), recursive scanning, ambiguous-match selection, and path/symlink escape protection with scan limits.
- Windows setup/build/start launchers and `Use-Existing-Bot.ps1` for privately validating and configuring an existing Discord bot token and owner ID.
- Safe playback-failure notices in the requesting text channel.

### Changed

- Default `playback.maxYouTubePlaylistPages` to **20**.
- Default `presence.songInStatus` to **true**, showing the playing song in Listening activity.
- Default `voice.stayInChannel` to **true** and `voice.aloneTimeUntilStopSeconds` to **600**. The bot stays after the queue ends, then stops, clears the queue and disconnects after ten minutes without human users. The five-second polling interval can add up to five seconds to the timeout.
- Count deafened human users as present and cancel the empty-room timer when a person returns.
- Pin YouTube source to upstream snapshot `2be8e542d3f6f178e048dca565892684c2e40177-SNAPSHOT`, including the TV-client reload-error fix, missing format-length recovery and cipher response cleanup.

### Fixed

- Missing or malformed track titles, including invalid URL metadata and unsafe query-string fallbacks.
- Unsupported voice-channel/thread conversion in music command entry points; these commands request a regular text channel.
- Null playback exception messages and missing audio handlers during idle cleanup.
- Concurrent empty-room timer access and stale disconnects by rechecking human presence before leaving.
- Windows token setup file protection through `icacls`, plus clearer verification errors without revealing credentials.
- OAuth tests writing dummy tokens into the live working directory; tests now use temporary files.

### Validation and limits

- Maven `verify`: **677 unit tests + 43 integration tests = 720**, with no failures, errors or skipped tests.
- Windows native DAVE and UDP audio libraries loaded; local 48 kHz stereo WAV decoding produced Opus frames.
- The patched OAuth-enabled YouTube client decoded the previously failing “Love Lockdown” track. A separate measurement selected stereo AAC in MP4 (`itag 18`); this is track-specific, not a promise of fixed bitrate or lossless quality.
- YouTube OAuth remains disabled in the distributed defaults. When enabled, the existing integration uses Google's device authorization flow and the third-party cipher service `cipher.kikkia.dev`; saved tokens must stay private.
- Bot profile icon and banner were not changed. Portable runtime, compiled JAR, local audio, configuration, credentials and logs are excluded from Git.
