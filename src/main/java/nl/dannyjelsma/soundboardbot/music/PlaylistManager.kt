package nl.dannyjelsma.soundboardbot.music

import com.fasterxml.jackson.databind.ObjectMapper
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import nl.dannyjelsma.soundboardbot.audio.SoundManager
import nl.dannyjelsma.soundboardbot.result.PlaylistCreationResult
import nl.dannyjelsma.soundboardbot.result.PlaylistLoadResult
import nl.dannyjelsma.soundboardbot.result.PlaylistResult
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files

class PlaylistManager {
    val playlists: List<File>
        get() {
            val files = File("./playlists/").listFiles()
            val playlists: MutableList<File> = ArrayList()
            if (files != null) {
                for (file in files) {
                    if (file.name.endsWith(".json")) {
                        playlists.add(file)
                    }
                }
            }
            return playlists
        }

    fun createPlaylist(name: String): PlaylistCreationResult {
        val mapper = ObjectMapper()
        val playlist = Playlist()
        val newPlaylist = File("./playlists/", "$name.json")
        if (newPlaylist.exists()) {
            return PlaylistCreationResult(false, "This playlist already exists!")
        }

        if (!newPlaylist.parentFile.exists()) {
            newPlaylist.parentFile.mkdir()
        }

        return try {
            newPlaylist.createNewFile()
            mapper.writeValue(newPlaylist, playlist)
            PlaylistCreationResult(true, null)
        } catch (e: Exception) {
            PlaylistCreationResult(false, e.message)
        }
    }

    fun deletePlaylist(name: String): PlaylistResult {
        val playlist = File("./playlists/", "$name.json")
        val dstFile = File("./playlists/", "$name.json.disabled")
        if (!playlist.exists()) {
            return PlaylistResult(false, "That playlist doesn't exist!")
        }

        try {
            Files.move(playlist.toPath(), dstFile.toPath())
        } catch (e: IOException) {
            playlist.delete()
            e.printStackTrace()
        }

        return PlaylistResult(true, null)
    }

    fun addToPlaylist(playlistName: String, url: String): PlaylistResult {
        val loadResult = loadPlaylist(playlistName)
        if (!loadResult.isSuccess) {
            return PlaylistResult(false, "Could not load playlist: " + loadResult.message)
        }

        val playlist = loadResult.playlist
        if (playlist != null) {
            var query = url

            try {
                URL(query)
            } catch (e: MalformedURLException) {
                query = "ytsearch:$query"
            }

            return try {
                val success = booleanArrayOf(false)
                val audioTrack: AudioTrack? = SoundManager.getFromCache(query)
                if (audioTrack != null) {
                    val song = Song(audioTrack.info.title, audioTrack.info.uri)

                    playlist.addSong(song)
                    savePlaylist(playlist)
                    success[0] = true
                } else {
                    SoundManager.audioPlayerManager.loadItemOrdered(SoundManager,
                        query,
                        object : AudioLoadResultHandler {
                            override fun trackLoaded(track: AudioTrack) {
                                val song = Song(track.info.title, track.info.uri)

                                SoundManager.addToCache(query, track)
                                playlist.addSong(song)
                                savePlaylist(playlist)
                                success[0] = true
                            }

                            override fun playlistLoaded(audioPlaylist: AudioPlaylist) {
                                if (query.contains("/playlist")) {
                                    for (track in audioPlaylist.tracks) {
                                        val song = Song(track.info.title, track.info.uri)
                                        playlist.addSong(song)
                                    }
                                } else {
                                    val track = audioPlaylist.tracks[0]
                                    val song = Song(track.info.title, track.info.uri)
                                    SoundManager.addToCache(query, track)
                                    playlist.addSong(song)
                                }

                                savePlaylist(playlist)
                                success[0] = true
                            }

                            override fun noMatches() {}
                            override fun loadFailed(exception: FriendlyException) {}
                        }).get()
                }

                PlaylistResult(success[0], "No matches for this song.")
            } catch (e: Exception) {
                PlaylistResult(false, e.message)
            }
        } else {
            return PlaylistResult(false, loadResult.message)
        }
    }

    fun removeFromPlaylist(playlistName: String, url: String): PlaylistResult {
        val loadResult = loadPlaylist(playlistName)
        if (!loadResult.isSuccess) {
            return PlaylistResult(false, "Could not load playlist: " + loadResult.message)
        }

        val playlist = loadResult.playlist

        if (playlist != null) {
            val songs = playlist.songs
            for (song in songs) {
                if (song.url == url) {
                    playlist.removeSong(song)
                    savePlaylist(playlist)
                    return PlaylistResult(true, null)
                }
            }
        } else {
            return PlaylistResult(false, loadResult.message)
        }

        return PlaylistResult(false, "This URL is not in this playlist.")
    }

    fun loadPlaylist(playlistName: String): PlaylistLoadResult {
        val mapper = ObjectMapper()

        return try {
            val playlistFile = File("./playlists/", "$playlistName.json")
            val playlist = mapper.readValue(playlistFile, Playlist::class.java)
            playlist.playlistFile = playlistFile
            PlaylistLoadResult(true, null, playlist)
        } catch (e: Exception) {
            PlaylistLoadResult(false, e.message, null)
        }
    }

    fun savePlaylist(playlist: Playlist): PlaylistResult {
        val mapper = ObjectMapper()

        return try {
            mapper.writeValue(playlist.playlistFile, playlist)
            PlaylistResult(true, null)
        } catch (e: Exception) {
            PlaylistResult(false, e.message)
        }
    }

    fun removeFromPlaylistByTitle(playList: String, title: String): PlaylistResult {
        val loadResult = loadPlaylist(playList)
        if (!loadResult.isSuccess) {
            return PlaylistResult(false, "Could not load playlist: " + loadResult.message)
        }

        val playlist = loadResult.playlist
        if (playlist != null) {
            val songs = playlist.songs
            for (song in songs) {
                if (song.title.equals(title, ignoreCase = true)) {
                    playlist.removeSong(song)
                    savePlaylist(playlist)
                    return PlaylistResult(true, null)
                }
            }

            return PlaylistResult(false, "This URL is not in this playlist.")
        } else {
            return PlaylistResult(false, loadResult.message)
        }
    }
}