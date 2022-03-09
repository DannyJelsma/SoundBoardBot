package nl.dannyjelsma.soundboardbot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.tts.TTSClient

class TTSCommand : Command() {
    init {
        name = "tts"
    }

    override fun execute(event: CommandEvent) {
        val voiceState = event.guild.selfMember.voiceState
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            event.channel.sendMessage("I am not in a voice channel...").queue()
            return
        }

        val tts = TTSClient("Brian")
        if (event.args.isEmpty()) {
            event.channel.sendMessage("You did not provide a message.").queue()
            return
        }

        val url = tts.getSpeechURL(event.args)

        if (url != null) {
            tts.playSpeechFromURL(url)
        }
    }
}