package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class SkipCommand : Command() {
    init {
        name = "skip"
    }

    override fun execute(event: CommandEvent) {
        SoundManager.audioPlayer.stopTrack()
        event.channel.sendMessage("Skipped the current song.").queue()
    }
}