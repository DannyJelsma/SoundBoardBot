package nl.dannyjelsma.soundboardbot.result;

public class PlaylistResult {

    private final boolean success;
    private final String message;

    public PlaylistResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
