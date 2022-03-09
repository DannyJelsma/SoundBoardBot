package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class StopCommand : Command() {
    init {
        name = "stop"
    }

    override fun execute(event: CommandEvent) {
        SoundManager.scheduler.stop()
        event.channel.sendMessage("Stopped and cleared the queue.").queue()
    }
}