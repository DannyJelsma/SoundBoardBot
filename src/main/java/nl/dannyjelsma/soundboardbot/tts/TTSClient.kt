package nl.dannyjelsma.soundboardbot.tts

import com.fasterxml.jackson.databind.ObjectMapper
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import nl.dannyjelsma.soundboardbot.audio.SoundManager
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import java.io.IOException
import java.util.concurrent.ExecutionException

class TTSClient(private val voice: String) {
    fun getSpeechURL(message: String): String? {
        try {
            val httpClient = HttpClientBuilder.create().build()
            val request = HttpPost("https://streamlabs.com/polly/speak")
            val httpParams = StringEntity("voice=$voice&text=$message")
            request.addHeader("content-type", "application/x-www-form-urlencoded")
            request.entity = httpParams

            val response = httpClient.execute(request)
            val mapper = ObjectMapper()
            val ttsResponse = mapper.readValue(response.entity.content, TTSResponse::class.java)

            return if (!ttsResponse.success) {
                null
            } else ttsResponse.speak_url.toString()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return null
    }

    fun playSpeechFromURL(url: String) {
        try {
            SoundManager.audioPlayerManager.loadItemOrdered(SoundManager, url, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    SoundManager.scheduler.queue(track, true)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    for (track in playlist.tracks) {
                        SoundManager.scheduler.queue(track, true)
                    }
                }

                override fun noMatches() {}
                override fun loadFailed(exception: FriendlyException) {}
            }).get()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
    }
}