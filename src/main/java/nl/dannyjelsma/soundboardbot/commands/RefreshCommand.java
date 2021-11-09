package nl.dannyjelsma.soundboardbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;

public class RefreshCommand extends Command {

    public RefreshCommand() {
        name = "refresh";
        cooldown = 20;
    }

    @Override
    protected void execute(CommandEvent event) {
        SoundManager manager = SoundManager.getInstance();


        manager.clearSounds();
        manager.loadSounds();
        event.getChannel().sendMessage("Refreshed the sounds.").queue();
    }
}
