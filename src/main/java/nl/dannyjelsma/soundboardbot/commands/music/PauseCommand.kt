package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class PauseCommand : Command() {
    init {
        name = "pause"
    }

    override fun execute(event: CommandEvent) {
        SoundManager.audioPlayer.isPaused = !SoundManager.audioPlayer.isPaused
        event.channel.sendMessage("Set paused to **" + !SoundManager.audioPlayer.isPaused + "**.").queue()
    }
}