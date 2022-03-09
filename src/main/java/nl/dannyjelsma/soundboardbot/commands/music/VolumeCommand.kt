package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class VolumeCommand : Command() {
    init {
        name = "volume"
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split(" ").toTypedArray()
        if (args.size != 1) {
            event.channel.sendMessage("Incorrect arguments! Usage: =play <url>").queue()
            return
        }

        try {
            val volume = args[0].toInt()
            SoundManager.audioPlayer.volume = volume
            event.channel.sendMessage("Set volume to $volume.").queue()
        } catch (ex: NumberFormatException) {
            event.channel.sendMessage("That is not a number!").queue()
        }
    }
}