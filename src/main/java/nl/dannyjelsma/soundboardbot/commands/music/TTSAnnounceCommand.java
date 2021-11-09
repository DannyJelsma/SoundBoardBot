package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;

public class TTSAnnounceCommand extends Command {

    public TTSAnnounceCommand() {
        name = "ttsannounce";
        help = "Enables or disabled TTS Annoucements";
    }

    @Override
    protected void execute(CommandEvent event) {

        boolean currentTTS = SoundManager.getInstance().shouldTTS();

        SoundManager.getInstance().setTTS(!currentTTS);
        event.getChannel().sendMessage("Set TTS Announce to **" + !currentTTS + "**.").queue();
    }
}
