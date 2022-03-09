package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import nl.dannyjelsma.soundboardbot.audio.SoundManager
import nl.dannyjelsma.soundboardbot.music.PlaylistManager
import java.util.*


class PlaylistCommand : Command() {

    private val playlistManager = PlaylistManager()

    init {
        name = "playlist"
        help = "See =playlist help"
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split(" ").toTypedArray()
        when (args[0].lowercase(Locale.getDefault())) {
            "create" -> createPlaylist(event)
            "add" -> addToPlaylist(event)
            "list" -> listPlaylist(event)
            "play" -> playPlaylist(event)
            "remove" -> removeFromPlaylist(event)
            "deletelist" -> deletePlaylist(event)
            else -> event.channel.sendMessage("Valid commands:\n" +
                    "=playlist create <name>\n" +
                    "=playlist add <name> <url>\n" +
                    "=playlist list <name>\n" +
                    "=playlist play <name>\n" +
                    "=playlist remove <name> <url/title>\n" +
                    "=playlist deletelist <name>")
                .queue()
        }
    }

    private fun deletePlaylist(event: CommandEvent) {
        val args = event.args.split(" ").toTypedArray()
        if (args.size != 2) {
            event.channel.sendMessage("Incorrect arguments! Usage: =playlist deletelist <name>").queue()
            return
        }

        val result = playlistManager.deletePlaylist(args[1])
        if (!result.isSuccess) {
            event.channel.sendMessage("Could not delete the playlist: " + result.message).queue()
        } else {
            event.channel.sendMessage("Successfully deleted the playlist!").queue()
        }
    }

    private fun removeFromPlaylist(event: CommandEvent) {
        val args = event.args.split(" ").toTypedArray()
        if (args.size < 3) {
            event.channel.sendMessage("Incorrect arguments! Usage: =playlist remove <name> <url/title>").queue()
            return
        }

        val playList = args[1]
        val arg = args.copyOfRange(2, args.size).joinToString(" ")
        var result = playlistManager.removeFromPlaylist(playList, arg)
        if (!result.isSuccess) {
            result = playlistManager.removeFromPlaylistByTitle(playList, arg)
            if (!result.isSuccess) {
                event.channel.sendMessage("Could not remove the song from the playlist: " + result.message).queue()
            } else {
                event.channel.sendMessage("Successfully removed the song from the playlist!").queue()
            }
        } else {
            event.channel.sendMessage("Successfully removed the song from the playlist!").queue()
        }
    }

    private fun playPlaylist(event: CommandEvent) {
        val args = event.args.split(" ").toTypedArray()
        if (args.size != 2) {
            event.channel.sendMessage("Incorrect arguments! Usage: =playlist list <name>").queue()
            return
        }

        val playListName = args[1]
        val loadResult = playlistManager.loadPlaylist(playListName)
        if (!loadResult.isSuccess) {
            event.channel.sendMessage("Could not load the playlist: " + loadResult.message).queue()
            return
        }

        val playlist = loadResult.playlist
        if (playlist != null) {
            val songs = playlist.songs

            val tracks: MutableList<AudioTrack> = ArrayList()
            if (SoundManager.shouldShuffle()) {
                songs.shuffle()
            }

            for (song in songs) {
                val audioTrack: AudioTrack? = SoundManager.getFromCache(song.url)

                if (audioTrack != null) {
                    tracks.add(audioTrack)
                } else {
                    SoundManager.audioPlayerManager.loadItemOrdered(SoundManager, song.url,
                        object : AudioLoadResultHandler {

                            override fun trackLoaded(track: AudioTrack) {
                                tracks.add(track)
                                SoundManager.addToCache(song.url, track)
                            }

                            override fun playlistLoaded(playlist: AudioPlaylist) {
                                tracks.add(playlist.tracks[0])
                                SoundManager.addToCache(song.url, playlist.tracks[0])
                            }

                            override fun noMatches() {}
                            override fun loadFailed(exception: FriendlyException) {}
                        })
                }
            }

            SoundManager.scheduler.queue(tracks)
            event.channel.sendMessage("Added " + tracks.size + " songs to the queue!").queue()
        }
    }

    private fun listPlaylist(event: CommandEvent) {
        val args = event.args.split(" ").toTypedArray()
        if (args.size != 2) {
            event.channel.sendMessage("Incorrect arguments! Usage: =playlist list <name>").queue()
            return
        }

        val playListName = args[1]
        val loadResult = playlistManager.loadPlaylist(playListName)
        if (!loadResult.isSuccess) {
            event.channel.sendMessage("Could not load the playlist: " + loadResult.message).queue()
            return
        }

        val playlist = loadResult.playlist

        if (playlist != null) {
            val songs = playlist.songs
            var songListBuilder = StringBuilder()
            var isFirstMsg = true
            for (song in songs) {
                val currentLength = songListBuilder.length
                val songLine = "${song.title} - `${song.url}`"
                if (currentLength + songLine.length >= 1500) {
                    var songList = songListBuilder.toString()
                    if (isFirstMsg) {
                        songList = "**$playListName contains ${songs.size} songs:**\n$songList"
                        isFirstMsg = false
                    }

                    event.channel.sendMessage(songList).queue()
                    songListBuilder = StringBuilder()
                } else {
                    songListBuilder.append(songLine)
                }
            }

            if (songListBuilder.isNotEmpty()) {
                val songList = songListBuilder.toString()
                event.channel.sendMessage(songList).queue()
            }
        }
    }

    private fun addToPlaylist(event: CommandEvent) {
        val args = event.args.split(" ").toTypedArray()
        if (args.size < 3) {
            event.channel.sendMessage("Incorrect arguments! Usage: =playlist add <name> <url/search query>").queue()
            return
        }

        val playList = args[1]
        val query = args.copyOfRange(2, args.size).joinToString(" ")
        val result = playlistManager.addToPlaylist(playList, query)
        if (!result.isSuccess) {
            event.channel.sendMessage("Could not add the song to the playlist: " + result.message).queue()
        } else {
            event.channel.sendMessage("Successfully added the song to the playlist!").queue()
        }
    }

    private fun createPlaylist(event: CommandEvent) {
        val args = event.args.split(" ").toTypedArray()
        if (args.size < 2) {
            event.channel.sendMessage("Incorrect arguments! Usage: =playlist create <name>").queue()
            return
        } else if (args.size > 2) {
            event.channel.sendMessage("The playlist name cannot contain spaces! Use underscores instead.").queue()
            return
        }

        val name = event.args.split(" ").toTypedArray()[1]
        val result = playlistManager.createPlaylist(name)
        if (!result.isSuccess) {
            event.channel.sendMessage("Could not create a new playlist with the name `" + name + "`: " + result.message)
                .queue()
        } else {
            event.channel.sendMessage("Successfully created a new playlist named `$name`!").queue()
        }
    }
}