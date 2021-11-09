package nl.dannyjelsma.soundboardbot.controllers;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SoundController {

    @GetMapping("/sounds")
    public List<Sound> getSounds(@RequestHeader("X-API-KEY") String apiKey) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        return SoundManager.getInstance().getSounds();
    }

    @DeleteMapping("/sound/{soundName}")
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

    @PostMapping("/sound/{soundName}")
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


    @GetMapping("/sound/{soundName}")
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

    @GetMapping("/state")
    public Map<String, Object> getSound(@RequestHeader("X-API-KEY") String apiKey) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        Map<String, Object> state = new HashMap<>();
        SoundManager manager = SoundManager.getInstance();
        SoundScheduler scheduler = manager.getScheduler();

        List<AudioTrack> musicQueue = scheduler.getMusicQueue();
        List<AudioTrack> soundboardQueue = scheduler.getMusicQueue();
        AudioTrack playingTrack = manager.getAudioPlayer().getPlayingTrack();
        int volume = manager.getAudioPlayer().getVolume();

        state.put("musicQueue", musicQueue);
        state.put("soundboardQueue", soundboardQueue);
        state.put("playingTrack", playingTrack);
        state.put("volume", volume);

        return state;
    }

    @GetMapping("/skip")
    public void skipSound(@RequestHeader("X-API-KEY") String apiKey) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        SoundManager manager = SoundManager.getInstance();
        manager.getAudioPlayer().stopTrack();
    }

    @GetMapping("/stop")
    public void stopSound(@RequestHeader("X-API-KEY") String apiKey) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        SoundManager manager = SoundManager.getInstance();
        manager.getScheduler().stop();
    }

    @PostMapping("/TTS")
    public void stopSound(@RequestHeader("X-API-KEY") String apiKey, @RequestParam(value = "voice") String voice, @RequestParam(value = "message") String message) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        TTSClient tts = new TTSClient(voice);
        String url = tts.getSpeechURL(message);

        tts.playSpeechFromURL(url);
    }

    @GetMapping("/volume")
    public int getVolume(@RequestHeader("X-API-KEY") String apiKey) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        SoundManager manager = SoundManager.getInstance();
        return manager.getAudioPlayer().getVolume();
    }

    @PostMapping("/volume")
    public void setVolume(@RequestHeader("X-API-KEY") String apiKey, @RequestParam(value = "volume") int volume) {
        if (!isKeyValid(apiKey)) {
            throw new UnauthorizedException();
        }

        SoundManager manager = SoundManager.getInstance();
        manager.getAudioPlayer().setVolume(volume);
    }

    @GetMapping("/play/{soundName}")
    public void playSound(@RequestHeader("X-API-KEY") String apiKey, @PathVariable String soundName) {
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

    private boolean isKeyValid(String key) {
        APIKeys keys = SoundBoardBot.getApiKeys();

        return keys.containsKey(key);
    }
}
