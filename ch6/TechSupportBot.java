import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

// This class extends a ListenerAdapter to handle message events on Discord.
public class TechSupportBot extends ListenerAdapter {

    // The bot's Discord token for authentication.
    static String discordToken = "";
    // The name of the channel the bot should monitor and interact with.
    static String channelToWatch = "q-and-a";
    // Variable to store FAQ contents
    static String contentsFromFAQ = "";
    static String pathToFAQFile = "/Users/Desktop/FAQ.txt";
    // the system message
    static String systemMessage = "You are a virtual assistant that provides support for the Crooks Bank banking app. ".”;
    // our ChatGPT client
    static ChatGPTClientForQAandModeration chatGPTClient = null;

    public static void main(String[] args) throws IOException {

        // Set of intents declaring which types of events the bot intends to listen to.
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES, // For messages in guilds.
                GatewayIntent.DIRECT_MESSAGES, // For private direct messages.
                GatewayIntent.MESSAGE_CONTENT // To allow access to message content.
        );

        
        // Read the contents of an external text file into FAQContents variable
        contentsFromFAQ = readFileContents(pathToFAQFile);

        // create a new ChatGPTClientForQAandModeration
        chatGPTClient = new ChatGPTClientForQAandModeration(contentsFromFAQ, systemMessage);

        // Initialize the bot with minimal configuration and the specified intents.
        try {
            JDA jda = JDABuilder.createLight(discordToken, intents)
                    .addEventListeners(new TechSupportBot()) // Adding the current class as an event listener.
                    .setActivity(Activity.customStatus("Ready to answer questions")) // Set the bot's custom status.
                    .build();

            // Asynchronously get REST ping from Discord API and print it.
            jda.getRestPing().queue(ping -> System.out.println("Logged in with ping: " + ping));

            // Block the main thread until JDA is fully loaded.
            jda.awaitReady();

            // Print the number of guilds the bot is connected to.
            System.out.println("Guilds: " + jda.getGuildCache().size());
            System.out.println("Self user: " + jda.getSelfUser());
        } catch (InterruptedException e) {
            // Handle exceptions if the thread is interrupted during the awaitReady process.
            e.printStackTrace();
        }
    }

    // This method handles incoming messages.
    @Override
    public void onMessageReceived(MessageReceivedEvent messageEvent) {

        // The ID of the sender
        User senderDiscordID = messageEvent.getAuthor();
        // The Discord channel where the message was posted
        MessageChannelUnion channel = messageEvent.getChannel();
        net.dv8tion.jda.api.entities.Message message = messageEvent.getMessage();
        String reply = null;

        // Ignore messages sent by the bot to prevent self-responses.
        if (senderDiscordID.equals(messageEvent.getJDA().getSelfUser())) {
            return;
        } else if (messageEvent.getChannelType() == ChannelType.TEXT) {
            // Ignore messages not in the specified "q-and-a" channel.
            if (!channel.getName().equalsIgnoreCase(channelToWatch)) {
                return;
            }
        }

        // Show "typing" status while the bot is working
        channel.sendTyping().queue(); 

        // this line takes the question from the Discord users and asks ChatGPT
        reply = chatGPTClient.sendMessageFromDiscordUser(message.getContentDisplay());
        channel.sendMessage(reply).queue();
    }

    // New method to read file contents
    private static String readFileContents(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to read FAQ contents.";
        }
    }
}
