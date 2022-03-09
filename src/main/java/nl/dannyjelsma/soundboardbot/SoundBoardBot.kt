package nl.dannyjelsma.soundboardbot

import com.fasterxml.jackson.databind.ObjectMapper
import com.jagrosh.jdautilities.command.CommandClientBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import nl.dannyjelsma.soundboardbot.audio.AudioPlayerSendHandler
import nl.dannyjelsma.soundboardbot.audio.SoundManager
import nl.dannyjelsma.soundboardbot.commands.GenerateCommand
import nl.dannyjelsma.soundboardbot.commands.JoinCommand
import nl.dannyjelsma.soundboardbot.commands.RefreshCommand
import nl.dannyjelsma.soundboardbot.commands.TTSCommand
import nl.dannyjelsma.soundboardbot.commands.music.*
import nl.dannyjelsma.soundboardbot.listeners.NewGuildListener
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.channels.Channels
import kotlin.system.exitProcess

@SpringBootApplication
class SoundBoardBotApplication

// TODO: add proper error handling in all the null checks (messages to channel etc.)
// TODO: rework Result classes to use generics instead (like Rust)
object SoundBoardBot {
    lateinit var jda: JDA
    var apiKeys = APIKeys()

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            println("Please provide your bot token!")
            exitProcess(1)
        }

        runApplication<SoundBoardBotApplication>(*args) {
            val properties: MutableMap<String, Any> = HashMap()

            properties["logging.level.root"] = "INFO"
            properties["server.port"] = "32944"
            setDefaultProperties(properties)
        }

        val ytdlp = File("yt-dlp")
        if (!ytdlp.exists()) {
            try {
                println("Downloading yt-dlp...")
                val url = URL("https://github.com/yt-dlp/yt-dlp/releases/download/2021.10.22/yt-dlp")
                Channels.newChannel(url.openStream()).use { rbc ->
                    FileOutputStream(ytdlp).use { fos ->
                        fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                exitProcess(1)
            }
        }

        println("Connecting to discord...")
        val cmdBuilder = CommandClientBuilder()
        val builder = JDABuilder.createDefault(args[0])
        cmdBuilder.setOwnerId("160687007369134080")
        cmdBuilder.setPrefix("=")
        cmdBuilder.addCommands(JoinCommand(), GenerateCommand(), RefreshCommand(), TTSCommand(),
            PlaylistCommand(), ShuffleCommand(), PlayCommand(), TTSAnnounceCommand(), VolumeCommand(),
            StopCommand(), PauseCommand(), SkipCommand(), PlaylistsCommand(), QueueCommand(),
            RemoveCommand(), LoopCommand())
        val cmdClient = cmdBuilder.build()
        builder.addEventListeners(cmdClient, NewGuildListener())
        jda = builder.build()

        val apiKeyFile = File("api_keys.json")
        val mapper = ObjectMapper()
        if (apiKeyFile.exists()) {
            println("Found keys file. Reading...")
            try {
                apiKeys = mapper.readValue(apiKeyFile, APIKeys::class.java)
            } catch (e: IOException) {
                e.printStackTrace()
                exitProcess(1)
            }
        }

        println("Loading sounds...")
        SoundManager.loadSounds()

        val soundsFolder = File("sounds")
        if (!soundsFolder.exists()) {
            soundsFolder.mkdir()
        }

        for (guild in jda.guilds) {
            guild.audioManager.sendingHandler = AudioPlayerSendHandler(SoundManager.audioPlayer)
        }

        println("Waiting for JDA to fully connect...")
        jda.awaitReady()
        println("Done!")
    }
}