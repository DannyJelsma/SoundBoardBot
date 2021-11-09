package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;

public class PauseCommand extends Command {

    public PauseCommand() {
        name = "pause";
    }

    @Override
    protected void execute(CommandEvent event) {

        SoundManager manager = SoundManager.getInstance();
        boolean paused = manager.getAudioPlayer().isPaused();

        manager.getAudioPlayer().setPaused(!paused);
        event.getChannel().sendMessage("Set paused to **" + !paused + "**.").queue();
    }
}
