package nl.dannyjelsma.soundboardbot.result

import nl.dannyjelsma.soundboardbot.music.Playlist

class PlaylistLoadResult(success: Boolean, errorMessage: String?, val playlist: Playlist?) :
    PlaylistResult(success, errorMessage)