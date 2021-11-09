package nl.dannyjelsma.soundboardbot.music;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;
import nl.dannyjelsma.soundboardbot.result.PlaylistCreationResult;
import nl.dannyjelsma.soundboardbot.result.PlaylistLoadResult;
import nl.dannyjelsma.soundboardbot.result.PlaylistResult;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {

    public List<File> getPlaylists() {
        File[] files = new File("./playlists/").listFiles();
        List<File> playlists = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".json")) {
                    playlists.add(file);
                }
            }
        }

        return playlists;
    }

    public PlaylistCreationResult createPlaylist(String name) {
        ObjectMapper mapper = new ObjectMapper();
        Playlist playlist = new Playlist();
        File newPlaylist = new File("./playlists/", name + ".json");

        if (newPlaylist.exists()) {
            return new PlaylistCreationResult(false, "This playlist already exists!", newPlaylist);
        }

        if (!newPlaylist.getParentFile().exists()) {
            newPlaylist.getParentFile().mkdir();
        }

        try {
            newPlaylist.createNewFile();
            mapper.writeValue(newPlaylist, playlist);
            return new PlaylistCreationResult(true, null, newPlaylist);
        } catch (Exception e) {
            return new PlaylistCreationResult(false, e.getMessage(), newPlaylist);
        }
    }

    public PlaylistResult deletePlaylist(String name) {
        File playlist = new File("./playlists/", name + ".json");
        File dstFile = new File("./playlists/", name + ".json.disabled");

        if (!playlist.exists()) {
            return new PlaylistResult(false, "That playlist doesn't exist!");
        }

        try {
            Files.move(playlist.toPath(), dstFile.toPath());
        } catch (IOException e) {
            playlist.delete();
            e.printStackTrace();
        }

        return new PlaylistResult(true, null);
    }

    public PlaylistResult addToPlaylist(String playlistName, String url) {
        PlaylistLoadResult loadResult = loadPlaylist(playlistName);

        if (!loadResult.isSuccess()) {
            return new PlaylistResult(false, "Could not load playlist: " + loadResult.getMessage());
        }

        Playlist playlist = loadResult.getPlaylist();
        String query = url;

        try {
            new URL(query);
        } catch (MalformedURLException e) {
            query = "ytsearch:" + query;
        }

        try {
            SoundManager manager = SoundManager.getInstance();
            final boolean[] success = {false};
            String finalQuery = query;

            if (manager.isInCache(finalQuery)) {
                AudioTrack track = manager.getFromCache(finalQuery);
                Song song = new Song(track.getInfo().title, track.getInfo().uri);

                playlist.addSong(song);
                savePlaylist(playlist);
                success[0] = true;
            } else {
                manager.getAudioPlayerManager().loadItemOrdered(manager, query, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        Song song = new Song(track.getInfo().title, track.getInfo().uri);

                        manager.addToCache(finalQuery, track);
                        playlist.addSong(song);
                        savePlaylist(playlist);
                        success[0] = true;
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {
                        if (finalQuery.contains("/playlist")) {
                            for (AudioTrack track : audioPlaylist.getTracks()) {
                                Song song = new Song(track.getInfo().title, track.getInfo().uri);

                                playlist.addSong(song);
                            }
                        } else {
                            AudioTrack track = audioPlaylist.getTracks().get(0);
                            Song song = new Song(track.getInfo().title, track.getInfo().uri);

                            manager.addToCache(finalQuery, track);
                            playlist.addSong(song);
                        }

                        savePlaylist(playlist);
                        success[0] = true;
                    }

                    @Override
                    public void noMatches() {
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                    }
                }).get();
            }

            return new PlaylistResult(success[0], "No matches for this song.");
        } catch (Exception e) {
            return new PlaylistResult(false, e.getMessage());
        }
    }

    public PlaylistResult removeFromPlaylist(String playlistName, String url) {
        PlaylistLoadResult loadResult = loadPlaylist(playlistName);

        if (!loadResult.isSuccess()) {
            return new PlaylistResult(false, "Could not load playlist: " + loadResult.getMessage());
        }

        Playlist playlist = loadResult.getPlaylist();
        List<Song> songs = playlist.getSongs();

        for (Song song : songs) {
            if (song.getUrl().equals(url)) {
                playlist.removeSong(song);
                savePlaylist(playlist);
                return new PlaylistResult(true, null);
            }
        }

        return new PlaylistResult(false, "This URL is not in this playlist.");
    }

    public PlaylistLoadResult loadPlaylist(String playlistName) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            File playlistFile = new File("./playlists/", playlistName + ".json");
            Playlist playlist = mapper.readValue(playlistFile, Playlist.class);

            playlist.setPlaylistFile(playlistFile);
            return new PlaylistLoadResult(true, null, playlist);
        } catch (Exception e) {
            return new PlaylistLoadResult(false, e.getMessage(), null);
        }
    }

    public PlaylistResult savePlaylist(Playlist playlist) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writeValue(playlist.getPlaylistFile(), playlist);

            return new PlaylistResult(true, null);
        } catch (Exception e) {
            return new PlaylistResult(false, e.getMessage());
        }
    }

    public PlaylistResult removeFromPlaylistByTitle(String playList, String title) {
        PlaylistLoadResult loadResult = loadPlaylist(playList);

        if (!loadResult.isSuccess()) {
            return new PlaylistResult(false, "Could not load playlist: " + loadResult.getMessage());
        }

        Playlist playlist = loadResult.getPlaylist();
        List<Song> songs = playlist.getSongs();

        for (Song song : songs) {
            if (song.getTitle().equalsIgnoreCase(title)) {
                playlist.removeSong(song);
                savePlaylist(playlist);
                return new PlaylistResult(true, null);
            }
        }

        return new PlaylistResult(false, "This URL is not in this playlist.");
    }
}
