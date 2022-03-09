package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class QueueCommand : Command() {
    init {
        name = "queue"
        help = "=queue"
    }

    override fun execute(event: CommandEvent) {
        val queue = SoundManager.scheduler.musicQueue
        var queueStr = StringBuilder()
        var queueIndex = 0
        var isFirstMsg = true

        for (track in queue) {
            val currentLength = queueStr.length
            val line = "$queueIndex. ${track.info.title} by ${track.info.author}"

            if (currentLength + line.length >= 1500) {
                var queueList = queueStr.toString()
                if (isFirstMsg) {
                    queueList = "**There are ${queue.size} songs in the queue:**$queueList"
                    isFirstMsg = false
                }
                event.channel.sendMessage(queueList).queue()
                queueStr = StringBuilder()
            } else {
                queueStr.append(line)
            }

            queueIndex += 1
        }

        if (queueStr.isNotEmpty()) {
            val songList = queueStr.toString()
            event.channel.sendMessage(songList).queue()
        }
    }
}