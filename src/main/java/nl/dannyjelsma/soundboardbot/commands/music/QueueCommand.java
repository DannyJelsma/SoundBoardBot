package nl.dannyjelsma.soundboardbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import nl.dannyjelsma.soundboardbot.audio.SoundManager;
import nl.dannyjelsma.soundboardbot.audio.SoundScheduler;

import java.util.List;

public class QueueCommand extends Command {

    public QueueCommand() {
        name = "queue";
        help = "=queue";
    }

    @Override
    protected void execute(CommandEvent event) {

        SoundScheduler scheduler = SoundManager.getInstance().getScheduler();
        List<AudioTrack> queue = scheduler.getMusicQueue();
        StringBuilder queueStr = new StringBuilder();
        int queueIndex = 0;
        boolean isFirstMsg = true;

        for (AudioTrack track : queue) {
            int currentLength = queueStr.length();
            String line = queueIndex + ". " + track.getInfo().title + " by " + track.getInfo().author + "\n";

            if (currentLength + line.length() >= 1500) {
                String queueList = queueStr.toString();

                if (isFirstMsg) {
                    queueList = "**There are " + queue.size() + " songs in the queue:**\n" + queueList;
                    isFirstMsg = false;
                }

                event.getChannel().sendMessage(queueList).queue();
                queueStr = new StringBuilder();
            } else {
                queueStr.append(line);
            }

            queueIndex += 1;
        }

        if (queueStr.length() > 0) {
            String songList = queueStr.toString();
            event.getChannel().sendMessage(songList).queue();
        }
    }
}
