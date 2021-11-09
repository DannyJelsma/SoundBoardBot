package nl.dannyjelsma.soundboardbot.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import nl.dannyjelsma.soundboardbot.APIKeys;
import nl.dannyjelsma.soundboardbot.SoundBoardBot;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

public class GenerateCommand extends Command {

    public GenerateCommand() {
        name = "generate";
        cooldown = 10;
    }

    @Override
    protected void execute(CommandEvent event) {

        ObjectMapper mapper = new ObjectMapper();
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[16];
        File file = new File("api_keys.json");
        String key;


        random.nextBytes(randomBytes);
        key = DigestUtils.md5Hex(randomBytes);

        if (!file.exists()) {
            try {
                file.createNewFile();
                mapper.writeValue(file, new APIKeys());
            } catch (IOException e) {
                event.getChannel().sendMessage("Could not generate a key: " + e.getMessage()).queue();
                return;
            }
        }

        try {
            APIKeys keys = mapper.readValue(file, APIKeys.class);

            keys.addKey(key);
            SoundBoardBot.setApiKeys(keys);
            mapper.writeValue(file, keys);
        } catch (IOException e) {
            e.printStackTrace();
        }

        event.getChannel().sendMessage("Successfully generated a new key: **" + key + "**").queue();
    }
}
