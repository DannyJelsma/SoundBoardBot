package nl.dannyjelsma.soundboardbot.commands.music

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.music.PlaylistManager

class PlaylistsCommand : Command() {
    init {
        name = "playlists"
    }

    override fun execute(event: CommandEvent) {
        val manager = PlaylistManager()
        val playlists = manager.playlists
        val playlistsStr = StringBuilder("**Found ${playlists.size} playlists:**\n")

        for (file in playlists) {
            playlistsStr.append(file.name.replace(".json", "")).append("\n")
        }

        event.channel.sendMessage(playlistsStr.toString()).queue()
    }
}