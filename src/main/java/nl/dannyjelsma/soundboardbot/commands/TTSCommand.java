package nl.dannyjelsma.soundboardbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import nl.dannyjelsma.soundboardbot.tts.TTSClient;

public class TTSCommand extends Command {

    public TTSCommand() {
        name = "tts";
    }

    @Override
    protected void execute(CommandEvent event) {

        GuildVoiceState voiceState = event.getGuild().getSelfMember().getVoiceState();

        if (voiceState == null || !voiceState.inVoiceChannel()) {
            event.getChannel().sendMessage("I am not in a voice channel...").queue();
            return;
        }

        TTSClient tts = new TTSClient("Brian");

        if (event.getArgs().isEmpty()) {
            event.getChannel().sendMessage("You did not provide a message.").queue();
            return;
        }

        String url = tts.getSpeechURL(event.getArgs());
        tts.playSpeechFromURL(url);
    }
}
