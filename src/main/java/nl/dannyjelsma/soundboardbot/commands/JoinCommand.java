package nl.dannyjelsma.soundboardbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class JoinCommand extends Command {

    public JoinCommand() {
        name = "join";
        cooldown = 10;
    }

    @Override
    protected void execute(CommandEvent event) {

        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();

        if (voiceState == null || !voiceState.inVoiceChannel()) {
            event.getChannel().sendMessage("You are not in a voice channel...").queue();
            return;
        }

        Guild guild = event.getGuild();
        VoiceChannel channel = voiceState.getChannel();

        guild.getAudioManager().openAudioConnection(channel);
    }
}
