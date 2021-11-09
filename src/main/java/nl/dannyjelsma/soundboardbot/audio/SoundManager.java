package nl.dannyjelsma.soundboardbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SoundManager {

    private final AudioPlayer audioPlayer;
    private static SoundManager instance;
    private final List<Sound> sounds;
    private final AudioPlayerManager audioPlayerManager;
    private final SoundScheduler scheduler;
    private boolean shuffle = false;
    private boolean ttsAnnouncement = true;
    private final HashMap<String, AudioTrack> songCache = new HashMap<>();
    private boolean loop = false;

    private SoundManager() {
        this.sounds = new ArrayList<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);

        this.audioPlayer = audioPlayerManager.createPlayer();
        this.scheduler = new SoundScheduler(this.audioPlayer);
        this.audioPlayer.addListener(scheduler);
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }

        return instance;
    }

    public void addToCache(String query, AudioTrack track) {
        System.out.println("Added " + query + " to the cache!");
        songCache.put(query, track);
    }

    public boolean isInCache(String query) {
        return songCache.containsKey(query);
    }

    public AudioTrack getFromCache(String query) {
        return songCache.get(query).makeClone();
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public SoundScheduler getScheduler() {
        return scheduler;
    }

    public List<Sound> getSounds() {
        return sounds;
    }

    public Sound getSound(String name) {
        for (Sound sound : sounds) {
            if (sound.name().equals(name)) {
                return sound;
            }
        }

        return null;
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(this.audioPlayer);
    }

    public void addSound(Sound sound) {
        sounds.add(sound);
    }

    public boolean hasSound(String name) {
        for (Sound sound : sounds) {
            if (sound.name().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public void loadSounds() {
        File soundsFolder = new File("sounds");

        if (soundsFolder.exists()) {
            File[] sounds = soundsFolder.listFiles();

            if (sounds != null) {
                for (File soundFolder : sounds) {
                    if (soundFolder.isDirectory()) {
                        String name = soundFolder.getName();
                        File soundFile = new File(soundFolder, name + ".mp3");

                        if (soundFile.exists()) {
                            Sound sound = new Sound(name, soundFile);

                            addSound(sound);
                        }
                    }
                }
            }
        }
    }

    public boolean shouldShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public boolean shouldTTS() {
        return ttsAnnouncement;
    }

    public void setTTS(boolean ttsAnnouncement) {
        this.ttsAnnouncement = ttsAnnouncement;
    }

    public void clearSounds() {
        sounds.clear();
    }

    public boolean shouldLoop() {
        return this.loop;
    }

    public void setLoop(boolean shouldLoop) {
        this.loop = shouldLoop;
    }
}
