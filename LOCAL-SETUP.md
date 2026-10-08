# Your MusicBot fork

GitHub: https://github.com/jasser-99/MusicBot

This working copy starts at upstream commit `6116b7aec6bd9614a33a736483e4f54ae2f6b1f7` (0.7.0). Apache 2.0 license and existing attribution are retained.

## Run on this Windows PC

1. Create or select a bot application at https://discord.com/developers/applications. Enable **Message Content Intent** on its Bot page.
2. Run **Setup Bot.bat**. Enter the bot token in the hidden local prompt, your Discord user ID, and a music directory. The token is saved only in ignored `config.txt`, with access restricted to your Windows user. Do not upload this file or its backups.
3. Invite the application to your server with the `bot` and `applications.commands` scopes. Give it View Channels, Send Messages, Read Message History, Embed Links, Connect, and Speak. Add Attach Files, Add Reactions, and Use External Emojis for the corresponding optional features. Administrator is unnecessary. Manage Messages is only needed for the optional channel-clear feature.
4. Run **Start Bot.bat**. Keep this window open; press Ctrl+C to stop. The bot runs on this PC and stops when the PC sleeps or shuts down.
5. Use `/settc` in a regular server music text channel. Join a voice channel and try `/play query:<song title or URL>`.

Java 25 and the compiled JAR are included locally. The Maven tool is also included for **Build Bot.bat**, which runs the full test suite before replacing `JMusicBot.jar`. Runtime/tool binaries are excluded from Git. For another PC, obtain Java 25 from Eclipse Adoptium and Maven from Apache or use the upstream Docker setup, then build this fork. The upstream prebuilt Docker image does not contain these changes.

## Added behavior

- `/music play local:<song name or relative path>` searches the configured folder and subfolders. Autocomplete shows up to 25 audio files. An exact relative path selects one track; ambiguous name searches show choices instead of playing an arbitrary file.
- `/music local` browses files; `/music local local:<search>` filters them. Results are relative paths and stay inside `paths.localMusicFolder`. Scans are limited to 20 directory levels and 10,000 entries; very large libraries should be split into smaller folders. Autocomplete values longer than Discord's 100-character limit are omitted; use the relative path directly instead.
- Local library search supports MP3, FLAC, WAV, WebM, MKV, MP4, M4A, OGG, Opus, and AAC. This searches filenames and folder names, not ID3 artist/album tags. Existing exact-path `/play` behavior remains available.
- `playback.maxYouTubePlaylistPages = 20`. This is a loader limit, not a promise that every playlist exposes 2,000 songs; source availability and pagination still apply.
- `presence.songInStatus = true`: Discord shows **Listening to <title>**. This status is global to the bot and reflects the most recent track event across servers. Per-server now-playing messages remain available.
- Titles use the source metadata for YouTube, SoundCloud, Bandcamp, Vimeo, Twitch, HTTP, and local tracks. Missing metadata falls back to a filename, stream host, or `Untitled audio`. HTTP fallback strips query credentials and decodes the filename. Streams without track metadata cannot reveal the actual song playing inside them.
- Music commands in voice-channel chat or threads receive an explanation instead of attempting to convert the channel to TextChannel. Full playback from these chats is still unsupported; use a regular server text channel. This guard covers prefix/slash music commands, not every administrative command or old interaction button.

## Review and validation

The reviewed code is a bot-token-based Java/JDA application using Lavaplayer, native Opus decoding, DAVE encryption, and UDP audio transport. No obvious malicious behavior was identified in the inspected setup, playback, configuration, and command paths. This is a scoped review, not a complete dependency or supply-chain security audit.

YouTube OAuth is disabled. Enabling it also enables an upstream third-party cipher service (`cipher.kikkia.dev`) and requires separate review. Eval remains disabled. HTTP playback and legacy exact local paths are powerful host-level capabilities; keep the bot in servers you trust. The new local search is confined to its configured library.

Verified locally:

- Maven `verify`: 673 unit tests and 43 integration tests, zero failures/errors/skips.
- New regression tests cover online title preservation, missing/empty metadata, HTTP query stripping, local recursive search, ambiguous matching, path traversal rejection, command registration, selected-file queuing, and voice-chat error handling.
- Windows native DAVE loading and local 48 kHz stereo WAV-to-Opus decoding passed.
- PowerShell setup syntax and packaged default-config generation passed.

Unverified until a bot token and target server are configured: Discord login, slash registration, voice connection, permissions, end-to-end sound quality, and live playback from each online source. Changing website APIs can break individual source extractors. Audio quality depends on the original source, volume settings, and Discord voice bitrate; the bot cannot restore quality lost in the source.

## Publishing local changes

The source changes are on `feature/local-library-and-track-titles`. `origin` points to your fork; `upstream` points to arif-banai/MusicBot. After Git authentication, push the feature branch with:

```powershell
git push -u origin feature/local-library-and-track-titles
```

The portable runtime, token, music, logs, and JAR are ignored and must not be committed.
