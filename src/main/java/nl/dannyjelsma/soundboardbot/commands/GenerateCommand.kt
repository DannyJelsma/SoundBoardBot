package nl.dannyjelsma.soundboardbot.commands

import com.fasterxml.jackson.databind.ObjectMapper
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import nl.dannyjelsma.soundboardbot.APIKeys
import nl.dannyjelsma.soundboardbot.SoundBoardBot
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.security.SecureRandom

class GenerateCommand : Command() {
    init {
        name = "generate"
        cooldown = 10
    }

    override fun execute(event: CommandEvent) {
        val mapper = ObjectMapper()
        val random = SecureRandom()
        val randomBytes = ByteArray(16)
        val file = File("api_keys.json")
        random.nextBytes(randomBytes)
        val key: String = DigestUtils.md5Hex(randomBytes)

        if (!file.exists()) {
            try {
                file.createNewFile()
                mapper.writeValue(file, APIKeys())
            } catch (e: Exception) {
                e.printStackTrace()
                event.channel.sendMessage("Could not generate a key: " + e.message).queue()
                return
            }
        }

        try {
            val keys = mapper.readValue(file, APIKeys::class.java)

            keys.addKey(key)
            SoundBoardBot.apiKeys = keys
            mapper.writeValue(file, keys)
        } catch (e: Exception) {
            e.printStackTrace()
            event.channel.sendMessage("Could not generate a key: " + e.message).queue()
            return
        }

        event.channel.sendMessage("Successfully generated a new key: **$key**").queue()
    }
}