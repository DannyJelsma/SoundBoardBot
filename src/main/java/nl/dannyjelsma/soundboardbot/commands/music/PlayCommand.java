package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;

import java.net.MalformedURLException;
import java.net.URL;

public class PlayCommand extends Command {

    public PlayCommand() {
        name = "play";
        help = "=play <url>";
    }

    @Override
    protected void execute(CommandEvent event) {
        
/*        if (args.length != 1) {
            event.getChannel().sendMessage("Incorrect arguments! Usage: =play <url>").queue();
            return;
        }*/

        String query = event.getArgs();

        try {
            new URL(query);
        } catch (MalformedURLException e) {
            query = "ytsearch:" + query;
        }

        SoundManager manager = SoundManager.getInstance();
        String finalQuery = query;

        if (manager.isInCache(query)) {
            manager.getScheduler().queue(manager.getFromCache(query), false);
            event.getChannel().sendMessage("Successfully added the song to the queue!").queue();
        } else {
            manager.getAudioPlayerManager().loadItemOrdered(manager, query, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    manager.getScheduler().queue(track, false);
                    manager.addToCache(finalQuery, track);
                    event.getChannel().sendMessage("Successfully added the song to the queue!").queue();
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    if (finalQuery.contains("/playlist")) {
                        for (AudioTrack track : playlist.getTracks()) {
                            manager.getScheduler().queue(track, false);
                        }

                        event.getChannel().sendMessage("Successfully added the playlist to the queue!").queue();
                    } else {
                        manager.getScheduler().queue(playlist.getTracks().get(0), false);
                        manager.addToCache(finalQuery, playlist.getTracks().get(0));
                        event.getChannel().sendMessage("Successfully added the song to the queue!").queue();
                    }
                }

                @Override
                public void noMatches() {
                    event.getChannel().sendMessage("No matches for this song!").queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    event.getChannel().sendMessage("Failed to add song to the queue: " + exception.getMessage()).queue();
                }
            });
        }
    }
}
