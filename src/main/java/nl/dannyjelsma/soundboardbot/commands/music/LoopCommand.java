package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;

public class LoopCommand extends Command {

    public LoopCommand() {
        name = "loop";
    }

    @Override
    protected void execute(CommandEvent event) {

        boolean currentLoop = SoundManager.getInstance().shouldLoop();

        SoundManager.getInstance().setLoop(!currentLoop);
        event.getChannel().sendMessage("Set loop to **" + !currentLoop + "**.").queue();
    }
}
