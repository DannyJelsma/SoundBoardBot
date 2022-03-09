package nl.dannyjelsma.soundboardbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.*

class SoundScheduler(private val player: AudioPlayer) : AudioEventAdapter() {
    private val soundboardQueue: MutableList<AudioTrack> = Collections.synchronizedList(ArrayList())
    val musicQueue: MutableList<AudioTrack> = Collections.synchronizedList(ArrayList())

    fun queue(track: AudioTrack, isSoundBoard: Boolean) {
        if (!player.startTrack(track, true)) {
            if (isSoundBoard) {
                soundboardQueue.add(track)
            } else {
                musicQueue.add(track)
            }
        }

        if (SoundManager.shouldShuffle()) {
            musicQueue.shuffle()
            soundboardQueue.shuffle()
        }
    }

    fun queue(tracks: List<AudioTrack>) {
        musicQueue.addAll(tracks)

        if (SoundManager.shouldShuffle()) {
            musicQueue.shuffle()
        }

        val firstTrack = musicQueue[0]
        if (player.startTrack(firstTrack, true)) {
            musicQueue.removeAt(0)
        }
    }

    fun removeAtIndex(index: Int) {
        musicQueue.removeAt(index)
    }

    fun nextTrack() {
        if (soundboardQueue.isNotEmpty()) {
            player.startTrack(soundboardQueue[0], false)
            soundboardQueue.removeAt(0)
            return
        }

        if (musicQueue.isEmpty()) {
            return
        }

        if (SoundManager.shouldShuffle()) {
            musicQueue.shuffle()
        }

        /*if (SoundManager.getInstance().shouldTTS()) {
            TTSClient ttsClient = new TTSClient("Brian");
            ttsClient.playSpeechFromURL(ttsClient.getSpeechURL("Now playing: " + nextTrack.getInfo().title));

            AudioTrack ttsTrack = soundboardQueue.get(soundboardQueue.size() - 1);
            player.startTrack(ttsTrack, false);

            while (player.getPlayingTrack().equals(ttsTrack)) {
                Thread.onSpinWait();
            }
        }*/

        val nextTrack = musicQueue[0]
        musicQueue.removeAt(0)

        if (SoundManager.shouldLoop()) {
            queue(nextTrack.makeClone(), false)
        }

        player.startTrack(nextTrack, false)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        nextTrack()
    }

    fun stop() {
        soundboardQueue.clear()
        musicQueue.clear()
        player.stopTrack()
    }
}