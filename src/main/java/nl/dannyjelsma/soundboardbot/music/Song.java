package nl.dannyjelsma.soundboardbot.music;

import java.util.Objects;

public class Song {

    private String title;
    private String url;

    public Song() {
    }

    public Song(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return url.equals(song.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
