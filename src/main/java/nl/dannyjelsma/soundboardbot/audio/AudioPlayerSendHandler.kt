package nl.dannyjelsma.soundboardbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.Buffer
import java.nio.ByteBuffer

class AudioPlayerSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
    private val buffer: ByteBuffer = ByteBuffer.allocate(1024)
    private val frame: MutableAudioFrame = MutableAudioFrame()

    /**
     * @param audioPlayer Audio player to wrap.
     */
    init {
        frame.setBuffer(buffer)
    }

    override fun canProvide(): Boolean {
        // returns true if audio was provided
        return audioPlayer.provide(frame)
    }

    override fun provide20MsAudio(): ByteBuffer {
        // flip to make it a read buffer
        (buffer as Buffer).flip()
        return buffer
    }

    override fun isOpus(): Boolean {
        return true
    }
}