package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;

public class StopCommand extends Command {

    public StopCommand() {
        name = "stop";
    }

    @Override
    protected void execute(CommandEvent event) {

        SoundManager manager = SoundManager.getInstance();

        manager.getScheduler().stop();
        event.getChannel().sendMessage("Stopped and cleared the queue.").queue();
    }
}
