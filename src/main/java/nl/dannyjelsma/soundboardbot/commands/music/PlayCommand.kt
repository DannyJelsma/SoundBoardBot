package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import nl.dannyjelsma.soundboardbot.audio.SoundManager
import java.net.MalformedURLException
import java.net.URL

class PlayCommand : Command() {
    init {
        name = "play"
        help = "=play <url>"
    }

    override fun execute(event: CommandEvent) {
        var query = event.args

        try {
            URL(query)
        } catch (e: MalformedURLException) {
            query = "ytsearch:$query"
        }

        val audioTrack: AudioTrack? = SoundManager.getFromCache(query)

        if (audioTrack != null) {
            SoundManager.scheduler.queue(audioTrack, false)
            event.channel.sendMessage("Successfully added the song to the queue!").queue()
        } else {
            SoundManager.audioPlayerManager.loadItemOrdered(SoundManager, query, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    SoundManager.scheduler.queue(track, false)
                    SoundManager.addToCache(query, track)
                    event.channel.sendMessage("Successfully added the song to the queue!").queue()
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    if (query.contains("/playlist")) {
                        for (track in playlist.tracks) {
                            SoundManager.scheduler.queue(track, false)
                        }
                        event.channel.sendMessage("Successfully added the playlist to the queue!").queue()
                    } else {
                        SoundManager.scheduler.queue(playlist.tracks[0], false)
                        SoundManager.addToCache(query, playlist.tracks[0])
                        event.channel.sendMessage("Successfully added the song to the queue!").queue()
                    }
                }

                override fun noMatches() {
                    event.channel.sendMessage("No matches for this song!").queue()
                }

                override fun loadFailed(exception: FriendlyException) {
                    event.channel.sendMessage("Failed to add song to the queue: " + exception.message).queue()
                }
            })
        }
    }
}