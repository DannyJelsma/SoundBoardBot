package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class RemoveCommand : Command() {
    init {
        name = "remove"
        help = "=remove <queue index>"
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split(" ").toTypedArray()
        if (args.size != 1) {
            event.channel.sendMessage("Invalid usage: =remove <queue index>").queue()
            return
        }

        val indexStr = args[0]
        try {
            val index = indexStr.toInt()
            SoundManager.scheduler.removeAtIndex(index)
            event.channel.sendMessage("Successfully removed the song from the queue!").queue()
        } catch (ex: NumberFormatException) {
            event.channel.sendMessage("$indexStr is not a number!").queue()
        }
    }
}