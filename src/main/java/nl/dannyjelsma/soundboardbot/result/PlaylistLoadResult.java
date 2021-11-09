package nl.dannyjelsma.soundboardbot.result;

import nl.dannyjelsma.soundboardbot.music.Playlist;

public class PlaylistLoadResult extends PlaylistResult {

    private final Playlist playlist;

    public PlaylistLoadResult(boolean success, String errorMessage, Playlist playlist) {
        super(success, errorMessage);
        this.playlist = playlist;
    }

    public Playlist getPlaylist() {
        return playlist;
    }
}
