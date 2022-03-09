package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class ShuffleCommand : Command() {
    init {
        name = "shuffle"
    }

    override fun execute(event: CommandEvent) {
        val currentShuffle: Boolean = SoundManager.shouldShuffle()

        SoundManager.setShuffle(!currentShuffle)
        event.channel.sendMessage("Set shuffle to **" + !currentShuffle + "**.").queue()
    }
}