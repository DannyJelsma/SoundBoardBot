package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class TTSAnnounceCommand : Command() {
    init {
        name = "ttsannounce"
        help = "Enables or disabled TTS Annoucements"
    }

    override fun execute(event: CommandEvent) {
        val currentTTS: Boolean = SoundManager.shouldTTS()

        SoundManager.setTTS(!currentTTS)
        event.channel.sendMessage("Set TTS Announce to **" + !currentTTS + "**.").queue()
    }
}