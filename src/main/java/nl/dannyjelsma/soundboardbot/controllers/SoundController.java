package nl.dannyjelsma.soundboardbot.controllers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import nl.dannyjelsma.soundboardbot.APIKeys;
import nl.dannyjelsma.soundboardbot.SoundBoardBot;
import nl.dannyjelsma.soundboardbot.audio.Sound;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;
import nl.dannyjelsma.soundboardbot.audio.SoundScheduler;
import nl.dannyjelsma.soundboardbot.exceptions.InvalidURLException;
import nl.dannyjelsma.soundboardbot.exceptions.SoundAlreadyExistsException;
import nl.dannyjelsma.soundboardbot.exceptions.SoundNotFoundException;
import nl.dannyjelsma.soundboardbot.exceptions.UnauthorizedException;
import nl.dannyjelsma.soundboardbot.tts.TTSClient;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SoundController {

    @GetMapping("/soundboard/sounds")
    public List<Sound> getSounds(@RequestHeader("X-API-KEY") String apiKey) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        return SoundManager.getInstance().getSounds();
    }

    @GetMapping("/soundboard/testkey")
    public boolean testAPIKey(@RequestHeader("X-API-KEY") String apiKey) {
        return isKeyValid(apiKey);
    }

    @DeleteMapping("/soundboard/sound/{soundName}")
    public void deleteSound(@RequestHeader("X-API-KEY") String apiKey, @PathVariable String soundName) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        File file = new File("." + File.separator + "sounds" + File.separator + soundName);
        if (!file.exists()) {
            throw new SoundNotFoundException();
        } else {
            File[] files = file.listFiles();

            if (files != null && files.length > 0) {
                for (File dirFile : files) {
                    dirFile.delete();
                }
            }

            SoundManager manager = SoundManager.getInstance();
            manager.clearSounds();
            manager.loadSounds();
            file.delete();
        }
    }

    @PostMapping("/soundboard/sound/{soundName}")
    public void uploadSound(@RequestHeader("X-API-KEY") String apiKey, @PathVariable String soundName, @RequestParam(value = "url") String url) throws InterruptedException, IOException {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new InvalidURLException();
        }

        File file = new File("." + File.separator + "sounds" + File.separator + soundName + File.separator + soundName + ".m4a");

        if (file.exists()) {
            throw new SoundAlreadyExistsException();
        } else {
            file.getParentFile().mkdir();
        }

        Process process = new ProcessBuilder("." + File.separator + "yt-dlp", "-f", "bestaudio[ext=m4a]", "--extract-audio", "--audio-format", "mp3", "-o", file.getAbsolutePath(), url.toString()).redirectErrorStream(true).start();
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        process.waitFor();
        file.delete();

        SoundManager manager = SoundManager.getInstance();
        manager.clearSounds();
        manager.loadSounds();
    }


    @GetMapping("/soundboard/sound/{soundName}")
    public Sound getSound(@RequestHeader("X-API-KEY") String apiKey, @PathVariable String soundName) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        File file = new File("." + File.separator + "sounds" + File.separator + soundName);
        if (!file.exists()) {
            throw new SoundNotFoundException();
        }

        return SoundManager.getInstance().getSound(soundName);
    }

    @GetMapping("/soundboard/state")
    public Map<String, Object> getState(@RequestHeader("X-API-KEY") String apiKey) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        Map<String, Object> state = new HashMap<>();
        SoundManager manager = SoundManager.getInstance();
        SoundScheduler scheduler = manager.getScheduler();

        List<AudioTrack> musicQueue = scheduler.getMusicQueue();
        List<String> musicNames = new ArrayList<>();
        List<AudioTrack> soundboardQueue = scheduler.getMusicQueue();
        List<String> soundboardNames = new ArrayList<>();
        AudioTrack playingTrack = manager.getAudioPlayer().getPlayingTrack();
        String playingTrackName;

        if (playingTrack != null) {
            playingTrackName = playingTrack.getInfo().title + " - " + playingTrack.getInfo().author;
        } else {
            playingTrackName = "None";
        }

        int volume = manager.getAudioPlayer().getVolume();

        for (AudioTrack track : musicQueue) {
            musicNames.add(track.getInfo().title + " - " + track.getInfo().author);
        }

        for (AudioTrack track : soundboardQueue) {
            soundboardNames.add(track.getInfo().title + " - " + track.getInfo().author);
        }

        state.put("musicQueue", musicNames);
        state.put("soundboardQueue", soundboardNames);
        state.put("playingTrack", playingTrackName);
        state.put("volume", volume);

        return state;
    }

    @GetMapping("/soundboard/skip")
    public void skipSound(@RequestHeader("X-API-KEY") String apiKey) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        SoundManager manager = SoundManager.getInstance();
        manager.getAudioPlayer().stopTrack();
    }

    @GetMapping("/soundboard/stop")
    public void stopSound(@RequestHeader("X-API-KEY") String apiKey) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        SoundManager manager = SoundManager.getInstance();
        manager.getScheduler().stop();
    }

    @PostMapping("/soundboard/TTS")
    public void playTTS(@RequestHeader("X-API-KEY") String apiKey, @RequestParam(value = "voice") String voice, @RequestParam(value = "message") String message) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        TTSClient tts = new TTSClient(voice);
        String url = tts.getSpeechURL(message);

        tts.playSpeechFromURL(url);
    }

    @GetMapping("/soundboard/volume")
    public int getVolume(@RequestHeader("X-API-KEY") String apiKey) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        SoundManager manager = SoundManager.getInstance();
        return manager.getAudioPlayer().getVolume();
    }

    @PostMapping("/soundboard/volume")
    public void setVolume(@RequestHeader("X-API-KEY") String apiKey, @RequestParam(value = "volume") int volume) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        SoundManager manager = SoundManager.getInstance();
        manager.getAudioPlayer().setVolume(volume);
    }

    @GetMapping("/soundboard/play/{soundName}")
    public void playSound(@RequestHeader("X-API-KEY") String apiKey, @PathVariable String soundName) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        SoundManager manager = SoundManager.getInstance();
        if (!manager.hasSound(soundName)) {
            throw new SoundNotFoundException();
        } else {
            Sound sound = manager.getSound(soundName);

            manager.getAudioPlayerManager().loadItemOrdered(manager, sound.file().getAbsolutePath(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    manager.getScheduler().queue(track, true);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    for (AudioTrack track : playlist.getTracks()) {
                        manager.getScheduler().queue(track, true);
                    }
                }

                @Override
                public void noMatches() {
                    throw new SoundNotFoundException();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    throw exception;
                }
            });
        }
    }

    private boolean isKeyValid(String key) {
        APIKeys keys = SoundBoardBot.getApiKeys();

        return keys.containsKey(key);
    }
}
