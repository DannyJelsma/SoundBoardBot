package nl.dannyjelsma.soundboardbot.tts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class TTSClient {

    private final String voice;

    public TTSClient(String voice) {
        this.voice = voice;
    }

    public String getSpeechURL(String message) {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost("https://streamlabs.com/polly/speak");
            StringEntity httpParams = new StringEntity("voice=" + this.voice + "&text=" + message);

            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setEntity(httpParams);

            CloseableHttpResponse response = httpClient.execute(request);
            ObjectMapper mapper = new ObjectMapper();
            TTSResponse ttsResponse = mapper.readValue(response.getEntity().getContent(), TTSResponse.class);

            if (!ttsResponse.success) {
                return null;
            }

            return String.valueOf(ttsResponse.speak_url);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public void playSpeechFromURL(String url) {
        SoundManager manager = SoundManager.getInstance();
        try {
            manager.getAudioPlayerManager().loadItemOrdered(manager, url, new AudioLoadResultHandler() {
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
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
