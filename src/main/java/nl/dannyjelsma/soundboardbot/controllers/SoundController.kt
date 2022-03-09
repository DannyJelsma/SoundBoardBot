package nl.dannyjelsma.soundboardbot.controllers

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import nl.dannyjelsma.soundboardbot.SoundBoardBot
import nl.dannyjelsma.soundboardbot.audio.Sound
import nl.dannyjelsma.soundboardbot.audio.SoundManager
import nl.dannyjelsma.soundboardbot.exceptions.InvalidURLException
import nl.dannyjelsma.soundboardbot.exceptions.SoundAlreadyExistsException
import nl.dannyjelsma.soundboardbot.exceptions.SoundNotFoundException
import nl.dannyjelsma.soundboardbot.exceptions.UnauthorizedException
import nl.dannyjelsma.soundboardbot.tts.TTSClient
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*

@RestController
class SoundController {
    @GetMapping("/soundboard/sounds")
    fun getSounds(@RequestHeader("X-API-KEY") apiKey: String): List<Sound> {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        return SoundManager.getSounds()
    }

    @GetMapping("/soundboard/testkey")
    fun testAPIKey(@RequestHeader("X-API-KEY") apiKey: String): Boolean {
        return isKeyValid(apiKey)
    }

    @DeleteMapping("/soundboard/sound/{soundName}")
    fun deleteSound(@RequestHeader("X-API-KEY") apiKey: String, @PathVariable soundName: String) {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        val file = File("." + File.separator + "sounds" + File.separator + soundName)
        if (!file.exists()) {
            throw SoundNotFoundException()
        } else {
            val files = file.listFiles()
            if (files != null && files.isNotEmpty()) {
                for (dirFile in files) {
                    dirFile.delete()
                }
            }

            SoundManager.clearSounds()
            SoundManager.loadSounds()
            file.delete()
        }
    }

    @PostMapping("/soundboard/sound/{soundName}")
    @Throws(InterruptedException::class, IOException::class)
    fun uploadSound(@RequestHeader("X-API-KEY") apiKey: String,
                    @PathVariable soundName: String,
                    @RequestBody body: MultiValueMap<String, String>) {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        try {
            URL(Objects.requireNonNull(body.getFirst("url")))
        } catch (e: Exception) {
            throw InvalidURLException()
        }

        val file =
            File("." + File.separator + "sounds" + File.separator + soundName + File.separator + soundName + ".m4a")
        if (file.exists()) {
            throw SoundAlreadyExistsException()
        } else {
            file.parentFile.mkdir()
        }

        val process = ProcessBuilder("." + File.separator + "yt-dlp",
            "-f",
            "bestaudio[ext=m4a]",
            "--extract-audio",
            "--audio-format",
            "mp3",
            "-o",
            file.absolutePath,
            body.toString()).redirectErrorStream(true).start()

        val `in` = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while (`in`.readLine().also { line = it } != null) {
            println(line)
        }

        process.waitFor()
        file.delete()
        SoundManager.clearSounds()
        SoundManager.loadSounds()
    }

    @GetMapping("/soundboard/sound/{soundName}")
    fun getSound(@RequestHeader("X-API-KEY") apiKey: String, @PathVariable soundName: String): Sound? {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        val file = File("." + File.separator + "sounds" + File.separator + soundName)
        if (!file.exists()) {
            throw SoundNotFoundException()
        }

        return SoundManager.getSound(soundName)
    }

    @GetMapping("/soundboard/state")
    fun getState(@RequestHeader("X-API-KEY") apiKey: String): Map<String, Any> {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        val state: MutableMap<String, Any> = HashMap()
        val musicQueue = SoundManager.scheduler.musicQueue
        val musicNames: MutableList<String> = ArrayList()
        val soundboardQueue = SoundManager.scheduler.musicQueue
        val soundboardNames: MutableList<String> = ArrayList()
        val playingTrack = SoundManager.audioPlayer.playingTrack
        val playingTrackName: String = if (playingTrack != null) {
            playingTrack.info.title + " - " + playingTrack.info.author
        } else {
            "None"
        }

        val volume = SoundManager.audioPlayer.volume
        for (track in musicQueue) {
            musicNames.add(track.info.title + " - " + track.info.author)
        }

        for (track in soundboardQueue) {
            soundboardNames.add(track.info.title + " - " + track.info.author)
        }

        state["musicQueue"] = musicNames
        state["soundboardQueue"] = soundboardNames
        state["playingTrack"] = playingTrackName
        state["volume"] = volume
        return state
    }

    @GetMapping("/soundboard/skip")
    fun skipSound(@RequestHeader("X-API-KEY") apiKey: String) {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        SoundManager.audioPlayer.stopTrack()
    }

    @GetMapping("/soundboard/stop")
    fun stopSound(@RequestHeader("X-API-KEY") apiKey: String) {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        SoundManager.scheduler.stop()
    }

    @PostMapping("/soundboard/TTS")
    fun playTTS(@RequestHeader("X-API-KEY") apiKey: String, @RequestBody body: MultiValueMap<String, String>) {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        val voice: String? = body.getFirst("voice")
        val message: String? = body.getFirst("message")

        if (voice != null && message != null) {
            val tts = TTSClient(voice)
            val url = tts.getSpeechURL(message)

            if (url != null) {
                tts.playSpeechFromURL(url)
            }
        }
    }

    @GetMapping("/soundboard/volume")
    fun getVolume(@RequestHeader("X-API-KEY") apiKey: String): Int {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        return SoundManager.audioPlayer.volume
    }

    @PostMapping("/soundboard/volume")
    fun setVolume(@RequestHeader("X-API-KEY") apiKey: String, @RequestBody body: MultiValueMap<String, Int>) {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        val volume = body.getFirst("volume")

        if (volume != null) {
            SoundManager.audioPlayer.volume = volume
        }
    }

    @GetMapping("/soundboard/play/{soundName}")
    fun playSound(@RequestHeader("X-API-KEY") apiKey: String, @PathVariable soundName: String) {
        if (!isKeyValid(apiKey)) {
            throw UnauthorizedException()
        }

        if (!SoundManager.hasSound(soundName)) {
            throw SoundNotFoundException()
        } else {
            val sound = SoundManager.getSound(soundName)
            if (sound != null) {
                SoundManager.audioPlayerManager.loadItemOrdered(SoundManager, sound.file.absolutePath,
                    object : AudioLoadResultHandler {
                        override fun trackLoaded(track: AudioTrack) {
                            SoundManager.scheduler.queue(track, true)
                        }

                        override fun playlistLoaded(playlist: AudioPlaylist) {
                            for (track in playlist.tracks) {
                                SoundManager.scheduler.queue(track, true)
                            }
                        }

                        override fun noMatches() {
                            throw SoundNotFoundException()
                        }

                        override fun loadFailed(exception: FriendlyException) {
                            throw exception
                        }
                    })
            }
        }
    }

    private fun isKeyValid(key: String): Boolean {
        return SoundBoardBot.apiKeys.containsKey(key)
    }
}