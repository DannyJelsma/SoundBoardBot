package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;
import nl.dannyjelsma.soundboardbot.music.Playlist;
import nl.dannyjelsma.soundboardbot.music.PlaylistManager;
import nl.dannyjelsma.soundboardbot.music.Song;
import nl.dannyjelsma.soundboardbot.result.PlaylistCreationResult;
import nl.dannyjelsma.soundboardbot.result.PlaylistLoadResult;
import nl.dannyjelsma.soundboardbot.result.PlaylistResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlaylistCommand extends Command {

    private static final PlaylistManager playlistManager = new PlaylistManager();

    public PlaylistCommand() {
        name = "playlist";
        help = "See =playlist help";
    }

    @Override
    protected void execute(CommandEvent event) {

        String[] args = event.getArgs().split(" ");

        switch (args[0].toLowerCase()) {
            case "create" -> createPlaylist(event);
            case "add" -> addToPlaylist(event);
            case "list" -> listPlaylist(event);
            case "play" -> playPlaylist(event);
            case "remove" -> removeFromPlaylist(event);
            case "deletelist" -> deletePlaylist(event);
            default -> event.getChannel().sendMessage("""
                    Valid commands:
                    =playlist create <name>
                    =playlist add <name> <url>
                    =playlist list <name>
                    =playlist play <name>
                    =playlist remove <name> <url/title>
                    =playlist deletelist <name>""").queue();
        }
    }

    private void deletePlaylist(CommandEvent event) {
        String[] args = event.getArgs().split(" ");

        if (args.length != 2) {
            event.getChannel().sendMessage("Incorrect arguments! Usage: =playlist deletelist <name>").queue();
            return;
        }

        PlaylistResult result = playlistManager.deletePlaylist(args[1]);

        if (!result.isSuccess()) {
            event.getChannel().sendMessage("Could not delete the playlist: " + result.getMessage()).queue();
        } else {
            event.getChannel().sendMessage("Successfully deleted the playlist!").queue();
        }
    }

    private void removeFromPlaylist(CommandEvent event) {
        String[] args = event.getArgs().split(" ");

        if (args.length < 3) {
            event.getChannel().sendMessage("Incorrect arguments! Usage: =playlist remove <name> <url/title>").queue();
            return;
        }

        String playList = args[1];
        String arg = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        ;
        PlaylistResult result = playlistManager.removeFromPlaylist(playList, arg);

        if (!result.isSuccess()) {
            result = playlistManager.removeFromPlaylistByTitle(playList, arg);

            if (!result.isSuccess()) {
                event.getChannel().sendMessage("Could not remove the song from the playlist: " + result.getMessage()).queue();
            } else {
                event.getChannel().sendMessage("Successfully removed the song from the playlist!").queue();
            }
        } else {
            event.getChannel().sendMessage("Successfully removed the song from the playlist!").queue();
        }
    }

    private void playPlaylist(CommandEvent event) {
        String[] args = event.getArgs().split(" ");

        if (args.length != 2) {
            event.getChannel().sendMessage("Incorrect arguments! Usage: =playlist list <name>").queue();
            return;
        }

        String playListName = args[1];
        PlaylistLoadResult loadResult = playlistManager.loadPlaylist(playListName);

        if (!loadResult.isSuccess()) {
            event.getChannel().sendMessage("Could not load the playlist: " + loadResult.getMessage()).queue();
            return;
        }

        Playlist playlist = loadResult.getPlaylist();
        List<Song> songs = playlist.getSongs();
        SoundManager manager = SoundManager.getInstance();
        List<AudioTrack> tracks = new ArrayList<>();

        if (SoundManager.getInstance().shouldShuffle()) {
            Collections.shuffle(songs);
        }

        for (Song song : songs) {
            if (manager.isInCache(song.getUrl())) {
                tracks.add(manager.getFromCache(song.getUrl()));
            } else {
                manager.getAudioPlayerManager().loadItemOrdered(manager, song.getUrl(), new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        tracks.add(track);
                        manager.addToCache(song.getUrl(), track);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        tracks.add(playlist.getTracks().get(0));
                        manager.addToCache(song.getUrl(), playlist.getTracks().get(0));
                    }

                    @Override
                    public void noMatches() {
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                    }
                });
            }
        }

        manager.getScheduler().queue(tracks);
        event.getChannel().sendMessage("Added " + tracks.size() + " songs to the queue!").queue();
    }

    private void listPlaylist(CommandEvent event) {
        String[] args = event.getArgs().split(" ");

        if (args.length != 2) {
            event.getChannel().sendMessage("Incorrect arguments! Usage: =playlist list <name>").queue();
            return;
        }

        String playListName = args[1];
        PlaylistLoadResult loadResult = playlistManager.loadPlaylist(playListName);

        if (!loadResult.isSuccess()) {
            event.getChannel().sendMessage("Could not load the playlist: " + loadResult.getMessage()).queue();
            return;
        }

        Playlist playlist = loadResult.getPlaylist();
        List<Song> songs = playlist.getSongs();
        StringBuilder songListBuilder = new StringBuilder();
        boolean isFirstMsg = true;

        for (Song song : songs) {
            int currentLength = songListBuilder.length();
            String songLine = song.getTitle() + " - `" + song.getUrl() + "`\n";

            if (currentLength + songLine.length() >= 1500) {
                String songList = songListBuilder.toString();

                if (isFirstMsg) {
                    songList = "**" + playListName + " contains " + songs.size() + " songs:" + "**\n" + songList;
                    isFirstMsg = false;
                }

                event.getChannel().sendMessage(songList).queue();
                songListBuilder = new StringBuilder();
            } else {
                songListBuilder.append(songLine);
            }
        }

        if (songListBuilder.length() > 0) {
            String songList = songListBuilder.toString();
            event.getChannel().sendMessage(songList).queue();
        }
    }

    private void addToPlaylist(CommandEvent event) {
        String[] args = event.getArgs().split(" ");

        if (args.length < 3) {
            event.getChannel().sendMessage("Incorrect arguments! Usage: =playlist add <name> <url/search query>").queue();
            return;
        }

        String playList = args[1];
        String query = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        PlaylistResult result = playlistManager.addToPlaylist(playList, query);

        if (!result.isSuccess()) {
            event.getChannel().sendMessage("Could not add the song to the playlist: " + result.getMessage()).queue();
        } else {
            event.getChannel().sendMessage("Successfully added the song to the playlist!").queue();
        }
    }

    private void createPlaylist(CommandEvent event) {
        String[] args = event.getArgs().split(" ");

        if (args.length < 2) {
            event.getChannel().sendMessage("Incorrect arguments! Usage: =playlist create <name>").queue();
            return;
        } else if (args.length > 2) {
            event.getChannel().sendMessage("The playlist name cannot contain spaces! Use underscores instead.").queue();
            return;
        }

        String name = event.getArgs().split(" ")[1];
        PlaylistCreationResult result = playlistManager.createPlaylist(name);

        if (!result.isSuccess()) {
            event.getChannel().sendMessage("Could not create a new playlist with the name `" + name + "`: " + result.getMessage()).queue();
        } else {
            event.getChannel().sendMessage("Successfully created a new playlist named `" + name + "`!").queue();
        }
    }
}
