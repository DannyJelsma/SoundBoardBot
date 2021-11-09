package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;

public class ShuffleCommand extends Command {

    public ShuffleCommand() {
        name = "shuffle";
    }

    @Override
    protected void execute(CommandEvent event) {

        boolean currentShuffle = SoundManager.getInstance().shouldShuffle();

        SoundManager.getInstance().setShuffle(!currentShuffle);
        event.getChannel().sendMessage("Set shuffle to **" + !currentShuffle + "**.").queue();
    }
}
