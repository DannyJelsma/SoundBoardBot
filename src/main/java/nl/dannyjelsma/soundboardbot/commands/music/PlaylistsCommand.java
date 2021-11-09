package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.music.PlaylistManager;

import java.io.File;
import java.util.List;

public class PlaylistsCommand extends Command {

    public PlaylistsCommand() {
        name = "playlists";
    }

    @Override
    protected void execute(CommandEvent event) {

        PlaylistManager manager = new PlaylistManager();
        List<File> playlists = manager.getPlaylists();
        StringBuilder playlistsStr = new StringBuilder("**Found " + playlists.size() + " playlists:**\n");

        for (File file : playlists) {
            playlistsStr.append(file.getName().replace(".json", "")).append("\n");
        }

        event.getChannel().sendMessage(playlistsStr.toString()).queue();
    }
}
