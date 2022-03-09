package nl.dannyjelsma.soundboardbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.io.File

object SoundManager {
    val audioPlayer: AudioPlayer
    private val sounds: MutableList<Sound>
    val audioPlayerManager: AudioPlayerManager
    val scheduler: SoundScheduler
    private var shuffle = false
    private var ttsAnnouncement = true
    private val songCache = HashMap<String, AudioTrack>()
    private var loop = false

    init {
        sounds = ArrayList()
        audioPlayerManager = DefaultAudioPlayerManager()
        AudioSourceManagers.registerLocalSource(audioPlayerManager)
        AudioSourceManagers.registerRemoteSources(audioPlayerManager)
        audioPlayer = audioPlayerManager.createPlayer()
        scheduler = SoundScheduler(audioPlayer)
        audioPlayer.addListener(scheduler)
    }

    fun addToCache(query: String, track: AudioTrack) {
        println("Added $query to the cache!")
        songCache[query] = track
    }

    fun getFromCache(query: String): AudioTrack? {
        return songCache[query]?.makeClone()
    }

    fun getSounds(): List<Sound> {
        return sounds
    }

    fun getSound(name: String): Sound? {
        for (sound in sounds) {
            if (sound.name == name) {
                return sound
            }
        }
        return null
    }

    private fun addSound(sound: Sound) {
        sounds.add(sound)
    }

    fun hasSound(name: String): Boolean {
        for (sound in sounds) {
            if (sound.name == name) {
                return true
            }
        }
        return false
    }

    fun loadSounds() {
        val soundsFolder = File("sounds")
        if (soundsFolder.exists()) {
            val sounds = soundsFolder.listFiles()
            if (sounds != null) {
                for (soundFolder in sounds) {
                    if (soundFolder.isDirectory) {
                        val name = soundFolder.name
                        val soundFile = File(soundFolder, "$name.mp3")
                        if (soundFile.exists()) {
                            val sound = Sound(name, soundFile)
                            addSound(sound)
                        }
                    }
                }
            }
        }
    }

    fun shouldShuffle(): Boolean {
        return shuffle
    }

    fun setShuffle(shuffle: Boolean) {
        this.shuffle = shuffle
    }

    fun shouldTTS(): Boolean {
        return ttsAnnouncement
    }

    fun setTTS(ttsAnnouncement: Boolean) {
        this.ttsAnnouncement = ttsAnnouncement
    }

    fun clearSounds() {
        sounds.clear()
    }

    fun shouldLoop(): Boolean {
        return loop
    }

    fun setLoop(shouldLoop: Boolean) {
        loop = shouldLoop
    }
}