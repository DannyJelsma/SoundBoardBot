package nl.dannyjelsma.soundboardbot.music;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(value = {"playlistFile"})
public class Playlist {

    private List<Song> songs = new ArrayList<>();
    private File playlistFile;

    public void addSong(Song song) {
        if (containsSong(song)) return;

        if (songs == null) {
            songs = new ArrayList<>();
        }

        songs.add(song);
    }

    public void removeSong(Song song) {
        songs.remove(song);
    }

    public boolean containsSong(Song song) {
        return songs.contains(song);
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public File getPlaylistFile() {
        return playlistFile;
    }

    public void setPlaylistFile(File playlistFile) {
        this.playlistFile = playlistFile;
    }
}
