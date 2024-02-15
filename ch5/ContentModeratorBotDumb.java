import java.io.IOException;
import java.util.EnumSet;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

// This class extends a ListenerAdapter to handle message events on Discord.
public class ContentModeratorBotDumb extends ListenerAdapter {

    // The bot's Discord token for authentication.
    static String discordToken = "YOUR_DISCORD_BOT_TOKEN";
    static String bannedWord = "puppies";


    public static void main(String[] args) throws IOException {
    
        // Set of intents declaring which types of events the bot intends to listen to.
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MEMBERS,   // to get access to the members of the Discord server
                GatewayIntent.GUILD_MODERATION, // to ban and unban members
                GatewayIntent.GUILD_MESSAGES, // For messages in guilds
                GatewayIntent.MESSAGE_CONTENT // To allow access to message content
        );

        // Initialize the bot with minimal configuration and the specified intents.
        try {
            JDA jda = JDABuilder.createLight(discordToken, intents)
                    .addEventListeners(new ContentModeratorBotDumb()) // Adding the current class as an event listener.
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

        User senderDiscordID = messageEvent.getAuthor();
        MessageChannelUnion channel = messageEvent.getChannel();
        Message message = messageEvent.getMessage();

        // Check whether the message was sent in a guild / server
        if (messageEvent.isFromGuild()){

            String content = message.getContentDisplay(); 
            // Check if the message contains the banned word 
            if (content.contains(bannedWord)){

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
