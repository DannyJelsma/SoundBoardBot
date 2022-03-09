package nl.dannyjelsma.soundboardbot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class JoinCommand : Command() {
    init {
        name = "join"
        cooldown = 10
    }

    override fun execute(event: CommandEvent) {
        val member = event.member
        val voiceState = member.voiceState

        if (voiceState == null || !voiceState.inVoiceChannel()) {
            event.channel.sendMessage("You are not in a voice channel...").queue()
            return
        }

        val guild = event.guild
        val channel = voiceState.channel
        guild.audioManager.openAudioConnection(channel)
    }
}