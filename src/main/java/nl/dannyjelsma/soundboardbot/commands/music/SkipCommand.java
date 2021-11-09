package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;

public class SkipCommand extends Command {

    public SkipCommand() {
        name = "skip";
    }

    @Override
    protected void execute(CommandEvent event) {

        SoundManager manager = SoundManager.getInstance();

        manager.getAudioPlayer().stopTrack();
        event.getChannel().sendMessage("Skipped the current song.").queue();
    }
}
