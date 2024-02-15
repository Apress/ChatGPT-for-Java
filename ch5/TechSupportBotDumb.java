import java.io.IOException;
import java.util.EnumSet;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

// This class extends a ListenerAdapter to handle message events on Discord.
public class TechSupportBotDumb extends ListenerAdapter {

    // The bot's Discord token for authentication.
    static String discordToken = "YOUR_DISCORD_BOT_TOKEN";
    // The name of the channel the bot should monitor and interact with.
    static String channelToWatch = "q-and-a";

    public static void main(String[] args) throws IOException {
    
        // Set of intents declaring which types of events the bot intends to listen to.
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES, // For messages in guilds.
                GatewayIntent.DIRECT_MESSAGES, // For private direct messages.
                GatewayIntent.MESSAGE_CONTENT // To allow access to message content.
        );

        // Initialize the bot with minimal configuration and the specified intents.
        try {
            JDA jda = JDABuilder.createLight(discordToken, intents)
                    .addEventListeners(new TechSupportBotDumb()) // Adding the current class as an event listener.
                    .setActivity(Activity.customStatus("Ready to answer questions")) // Set the bot's custom status.
                    .build();

            // Asynchronously get REST ping from Discord API and print it.
             jda.getRestPing().queue(ping ->   System.out.println("Logged in with ping: " + ping) );
            // Block the main thread until JDA is fully loaded.
            jda.awaitReady();

            // Print the number of guilds the bot is connected to.
            System.out.println("Guilds: " + jda.getGuildCache().size());
        } catch (InterruptedException e) {
            // Handle exceptions if the thread is interrupted during the awaitReady process.
            e.printStackTrace();
        }
    }

    // This method handles incoming messages.
    @Override
    public void onMessageReceived(MessageReceivedEvent messageEvent) {
        // The ID of the sender.
        User senderDiscordID = messageEvent.getAuthor();

        // Ignore messages sent by the bot to prevent self-responses.
        if (senderDiscordID.equals(messageEvent.getJDA().getSelfUser())) {
            return;
        } else if (messageEvent.getChannelType() == ChannelType.TEXT) {
            // Ignore messages not in the specified "q-and-a" channel.
            if (!messageEvent.getChannel().getName().equalsIgnoreCase(channelToWatch)) {
                return;
            }
        }
        // Send a greeting response to the user who sent the message.
        String reply = "hi <@" + senderDiscordID.getId() + ">, I can help you with that!";
        messageEvent.getChannel().sendMessage(reply).queue();
    }
}
