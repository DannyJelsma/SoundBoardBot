package nl.dannyjelsma.soundboardbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SoundScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final List<AudioTrack> soundboardQueue;
    private final List<AudioTrack> musicQueue;

    public SoundScheduler(AudioPlayer player) {
        this.player = player;
        this.soundboardQueue = Collections.synchronizedList(new ArrayList<>());
        this.musicQueue = Collections.synchronizedList(new ArrayList<>());
    }

    public void queue(AudioTrack track, boolean isSoundBoard) {
        if (!player.startTrack(track, true)) {
            if (isSoundBoard) {
                soundboardQueue.add(track);
            } else {
                musicQueue.add(track);
            }
        }

        if (SoundManager.getInstance().shouldShuffle()) {
            Collections.shuffle(musicQueue);
            Collections.shuffle(soundboardQueue);
        }
    }

    public void queue(List<AudioTrack> tracks) {
        musicQueue.addAll(tracks);

        if (SoundManager.getInstance().shouldShuffle()) {
            Collections.shuffle(musicQueue);
        }

        AudioTrack firstTrack = musicQueue.get(0);
        if (player.startTrack(firstTrack, true)) {
            musicQueue.remove(0);
        }
    }

    public void removeAtIndex(int index) {
        musicQueue.remove(index);
    }

    public void nextTrack() {
        if (!soundboardQueue.isEmpty()) {
            player.startTrack(soundboardQueue.get(0), false);
            soundboardQueue.remove(0);
            return;
        }

        if (musicQueue.isEmpty()) {
            return;
        }

        if (SoundManager.getInstance().shouldShuffle()) {
            Collections.shuffle(musicQueue);
        }

        /*if (SoundManager.getInstance().shouldTTS()) {
            TTSClient ttsClient = new TTSClient("Brian");
            ttsClient.playSpeechFromURL(ttsClient.getSpeechURL("Now playing: " + nextTrack.getInfo().title));

            AudioTrack ttsTrack = soundboardQueue.get(soundboardQueue.size() - 1);
            player.startTrack(ttsTrack, false);

            while (player.getPlayingTrack().equals(ttsTrack)) {
                Thread.onSpinWait();
            }
        }*/

        AudioTrack nextTrack = musicQueue.get(0);
        musicQueue.remove(0);

        if (SoundManager.getInstance().shouldLoop()) {
            queue(nextTrack.makeClone(), false);
        }

        player.startTrack(nextTrack, false);
    }

    public List<AudioTrack> getMusicQueue() {
        return musicQueue;
    }

    public List<AudioTrack> getSoundboardQueue() {
        return soundboardQueue;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        nextTrack();
    }

    public void stop() {
        soundboardQueue.clear();
        musicQueue.clear();
        player.stopTrack();
    }
}