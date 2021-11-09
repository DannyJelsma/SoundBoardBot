package nl.dannyjelsma.soundboardbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import nl.dannyjelsma.soundboardbot.audio.AudioPlayerSendHandler;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;
import nl.dannyjelsma.soundboardbot.commands.*;
import nl.dannyjelsma.soundboardbot.commands.music.*;
import nl.dannyjelsma.soundboardbot.listeners.NewGuildListener;
import nl.dannyjelsma.soundboardbot.music.Playlist;
import nl.dannyjelsma.soundboardbot.music.PlaylistManager;
import nl.dannyjelsma.soundboardbot.music.Song;
import nl.dannyjelsma.soundboardbot.result.PlaylistLoadResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class SoundBoardBot {

    private static JDA jda;
    private static APIKeys apiKeys = new APIKeys();
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Please provide your bot token!");
            System.exit(1);
        }

        SpringApplication app = new SpringApplication(SoundBoardBot.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "32944"));
        app.run(args);

        File youtubeDL = new File("yt-dlp");
        if (!youtubeDL.exists()) {
            try {
                System.out.println("Downloading yt-dlp...");
                URL url = new URL("https://github.com/yt-dlp/yt-dlp/releases/download/2021.10.22/yt-dlp");
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(youtubeDL);

                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                rbc.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }

        System.out.println("Connecting to discord...");
        CommandClientBuilder cmdBuilder = new CommandClientBuilder();
        JDABuilder builder = JDABuilder.createDefault(args[0]);

        cmdBuilder.setPrefix("=");
        cmdBuilder.addCommands(new JoinCommand(), new GenerateCommand(), new RefreshCommand(), new TTSCommand(),
                new PlaylistCommand(), new ShuffleCommand(), new PlayCommand(), new TTSAnnounceCommand(), new VolumeCommand(),
                new StopCommand(), new PauseCommand(), new SkipCommand(), new PlaylistsCommand(), new QueueCommand(),
                new RemoveCommand(), new LoopCommand());
        CommandClient cmdClient = cmdBuilder.build();

        builder.addEventListeners(cmdClient, new NewGuildListener());

        try {
            jda = builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        File file = new File("api_keys.json");
        ObjectMapper mapper = new ObjectMapper();

        if (file.exists()) {
            System.out.println("Found keys file. Reading...");

            try {
                apiKeys = mapper.readValue(file, APIKeys.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Loading sounds...");
        SoundManager soundManager = SoundManager.getInstance();
        soundManager.loadSounds();

        for (Guild guild : SoundBoardBot.getJDA().getGuilds()) {
            guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(soundManager.getAudioPlayer()));
        }

        System.out.println("Starting server...");
        System.out.println("Done!");

        File soundsFolder = new File("sounds");
        if (!soundsFolder.exists()) {
            soundsFolder.mkdir();
        }

        new Thread(() -> {
            System.out.println("BACKGROUND: Filling audio cache with playlist songs...");
            PlaylistManager playlistManager = new PlaylistManager();
            List<File> playlistFiles = playlistManager.getPlaylists();
            SoundManager manager = SoundManager.getInstance();

            for (File playlistFile : playlistFiles) {
                PlaylistLoadResult loadResult = playlistManager.loadPlaylist(playlistFile.getName().replace(".json", ""));

                if (loadResult.isSuccess()) {
                    Playlist playlist = loadResult.getPlaylist();

                    for (Song song : playlist.getSongs()) {
                        executor.execute(() -> {
                            try {
                                soundManager.getAudioPlayerManager().loadItemOrdered(manager, song.getUrl(), new AudioLoadResultHandler() {
                                    @Override
                                    public void trackLoaded(AudioTrack track) {
                                        soundManager.addToCache(song.getUrl(), track);
                                    }

                                    @Override
                                    public void playlistLoaded(AudioPlaylist playlist) {
                                        soundManager.addToCache(song.getUrl(), playlist.getTracks().get(0));
                                    }

                                    @Override
                                    public void noMatches() {
                                    }

                                    @Override
                                    public void loadFailed(FriendlyException exception) {
                                    }
                                }).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } else {
                    System.out.println("Couldn't load playlist " + playlistFile.getName() + ": " + loadResult.getMessage());
                }
            }
        }).start();
    }

    public static JDA getJDA() {
        return jda;
    }

    public static APIKeys getApiKeys() {
        return apiKeys;
    }

    public static void setApiKeys(APIKeys apiKeys) {
        SoundBoardBot.apiKeys = apiKeys;
    }
}
