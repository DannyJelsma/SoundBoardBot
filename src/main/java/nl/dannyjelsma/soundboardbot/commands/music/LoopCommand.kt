package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class LoopCommand : Command() {
    init {
        name = "loop"
    }

    override fun execute(event: CommandEvent) {
        val currentLoop: Boolean = SoundManager.shouldLoop()

        SoundManager.setLoop(!currentLoop)
        event.channel.sendMessage("Set loop to **" + !currentLoop + "**.").queue()
    }
}