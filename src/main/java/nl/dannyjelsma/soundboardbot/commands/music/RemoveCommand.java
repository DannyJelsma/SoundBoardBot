package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;
import nl.dannyjelsma.soundboardbot.audio.SoundScheduler;

public class RemoveCommand extends Command {

    public RemoveCommand() {
        name = "remove";
        help = "=remove <queue index>";
    }

    @Override
    protected void execute(CommandEvent event) {

        SoundManager manager = SoundManager.getInstance();
        SoundScheduler scheduler = manager.getScheduler();
        String[] args = event.getArgs().split(" ");

        if (args.length != 1) {
            event.getChannel().sendMessage("Invalid usage: =remove <queue index>").queue();
            return;
        }

        String indexStr = args[0];

        try {
            int index = Integer.parseInt(indexStr);

            scheduler.removeAtIndex(index);
            event.getChannel().sendMessage("Successfully removed the song from the queue!").queue();
        } catch (NumberFormatException ex) {
            event.getChannel().sendMessage(indexStr + " is not a number!").queue();
        }
    }
}
