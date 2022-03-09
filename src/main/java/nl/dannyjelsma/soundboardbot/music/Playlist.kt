package nl.dannyjelsma.soundboardbot.music

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.File

@JsonIgnoreProperties(value = ["playlistFile"])
class Playlist {
    var songs: MutableList<Song> = ArrayList()
    var playlistFile: File? = null

    fun addSong(song: Song) {
        if (containsSong(song)) return

        songs.add(song)
    }

    fun removeSong(song: Song) {
        songs.remove(song)
    }

    fun containsSong(song: Song): Boolean {
        return songs.contains(song)
    }
}