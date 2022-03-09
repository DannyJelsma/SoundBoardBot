package nl.dannyjelsma.soundboardbot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class RefreshCommand : Command() {
    init {
        name = "refresh"
        cooldown = 20
    }

    override fun execute(event: CommandEvent) {
        SoundManager.clearSounds()
        SoundManager.loadSounds()
        event.channel.sendMessage("Refreshed the sounds.").queue()
    }
}