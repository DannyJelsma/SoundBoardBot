package nl.dannyjelsma.soundboardbot.listeners

import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import nl.dannyjelsma.soundboardbot.audio.AudioPlayerSendHandler
import nl.dannyjelsma.soundboardbot.audio.SoundManager

class NewGuildListener : ListenerAdapter() {
    override fun onGuildJoin(event: GuildJoinEvent) {
        event.guild.audioManager.sendingHandler = AudioPlayerSendHandler(SoundManager.audioPlayer)
    }
}