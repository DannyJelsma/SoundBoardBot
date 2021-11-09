package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;

public class VolumeCommand extends Command {

    public VolumeCommand() {
        name = "volume";
    }

    @Override
    protected void execute(CommandEvent event) {

        String[] args = event.getArgs().split(" ");
        SoundManager manager = SoundManager.getInstance();

        if (args.length != 1) {
            event.getChannel().sendMessage("Incorrect arguments! Usage: =play <url>").queue();
            return;
        }

        try {
            int volume = Integer.parseInt(args[0]);

            manager.getAudioPlayer().setVolume(volume);
            event.getChannel().sendMessage("Set volume to " + volume + ".").queue();
        } catch (NumberFormatException ex) {
            event.getChannel().sendMessage("That is not a number!").queue();
        }
    }
}
