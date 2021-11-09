package nl.dannyjelsma.soundboardbot.result;

import java.io.File;

public class PlaylistCreationResult extends PlaylistResult {

    private final File playlist;

    public PlaylistCreationResult(boolean success, String errorMessage, File playList) {
        super(success, errorMessage);
        this.playlist = playList;
    }

    public File getPlaylist() {
        return playlist;
    }
}
