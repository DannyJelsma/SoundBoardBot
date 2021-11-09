package nl.dannyjelsma.soundboardbot.listeners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.dannyjelsma.soundboardbot.audio.AudioPlayerSendHandler;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;

public class NewGuildListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        event.getGuild().getAudioManager().setSendingHandler(new AudioPlayerSendHandler(SoundManager.getInstance().getAudioPlayer()));
    }
}
