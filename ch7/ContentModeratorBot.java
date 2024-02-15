import java.io.IOException;
import java.util.EnumSet;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

// This class extends a ListenerAdapter to handle message events on Discord.
public class ContentModeratorBot extends ListenerAdapter {

    // The bot's Discord token for authentication.
    static String discordToken = "";
    
    // the system message
    // This is a Java 13+ Multiline String notation. At the end of the day, it's still a String
    static String systemMessage = """
        You are the automated moderator assistant for a Discord server. 
        Review each message for the following rule violations:
        1. Sensitive information
        2. Abuse
        3. Inappropriate comments
        4. Spam, for example; a message in all capital letters, the same phrase or word being repeated over and over, more than 3 exclamation marks or question marks.
        5. Advertisement
        6. External links
        7. Political messages or debate
        8. Religious messages or debate
        
        If any of these violations are detected, respond with "FLAG" (in uppercase without quotation marks). If the message adheres to the rules, respond with "SAFE" (in uppercase without quotation marks).
        """;

    static String instructionsToChatGPT = "Analyze the following message for rule violations:";
    
    // this is our Chat Endpoint client
    static ChatGPTClientForQAandModeration chatGPTClient = null;
    // this is our Moderations Endpoint client
    static ModerationClient moderationClient = null;

    public static void main(String[] args) throws IOException {
    
        // Set of intents declaring which types of events the bot intends to listen to.
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MEMBERS,   // to get access to the members of the Discord server
                GatewayIntent.GUILD_MODERATION, // to ban and unban members
                GatewayIntent.GUILD_MESSAGES, // For messages in guilds
                GatewayIntent.MESSAGE_CONTENT // To allow access to message content
        );

        // create a new ChatGPTClientForQAandModeration
        chatGPTClient = new ChatGPTClientForQAandModeration(systemMessage, instructionsToChatGPT);

        // create a new ModerationClient
        moderationClient = new ModerationClient();

        // Initialize the bot with minimal configuration and the specified intents.
        try {
            JDA jda = JDABuilder.createLight(discordToken, intents)
                    .addEventListeners(new ContentModeratorBot()) // Adding the current class as an event listener.
                    .setActivity(Activity.customStatus("Helping to keep a friendly Discord server")) // Set the bot's custom status.
                    .build();

            // Asynchronously get REST ping from Discord API and print it.
            jda.getRestPing().queue(ping -> System.out.println("Logged in with ping: " + ping));

            // Block the main thread until JDA is fully loaded.
            jda.awaitReady();

            // Print the number of guilds the bot is connected to.
            System.out.println("Guilds: " + jda.getGuildCache().size());
            // Print the Discord userID of the bot
            System.out.println("Bot's ID: " + jda.getSelfUser());
        } catch (InterruptedException e) {
            // Handle exceptions if the thread is interrupted during the awaitReady process.
            e.printStackTrace();
        }
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent messageEvent){

        String chatGPTResponse = "";
        ModerationClient.ModerationResponse moderationResponse = null;
        User senderDiscordID = messageEvent.getAuthor();

        // The Discord channel where the message was posted
        MessageChannelUnion channel = messageEvent.getChannel();
        net.dv8tion.jda.api.entities.Message message = messageEvent.getMessage();

        // Ignore messages sent by the bot to prevent self-responses.
        if (senderDiscordID.equals(messageEvent.getJDA().getSelfUser())) {
            return;
        } 

        // this line takes the message from the Discord user and invokes the Moderation Endpoint
        moderationResponse = moderationClient.checkForObjectionalContent(message.getContentDisplay());

        // this line takes the message from the Discord user and invokes the Chat Endpoint
        chatGPTResponse = chatGPTClient.sendMessageFromDiscordUser(message.getContentDisplay());

        // Check whether the message was sent in a guild / server
        if (messageEvent.isFromGuild()){

            // Check both the Chat Endpoint and Moderation Endpoint to see if the message is flagged

            if (chatGPTResponse.equals("FLAG") || moderationResponse.isFlagged ){

                // Delete the message
                message.delete().queue();

                // Mention the user who sent the inappropriate message
                String authorMention = senderDiscordID.getAsMention();

                // Send a message mentioning the user and explaining why it was inappropriate
                channel.sendMessage(authorMention + " This comment was deemed inappropriate for this channel. " +
                        "If you believe this to be in error, please contact one of the human server moderators.").queue();
            }

        }

    }


}
