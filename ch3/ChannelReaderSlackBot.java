
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.Message;
import com.slack.api.model.User;
import com.slack.api.model.block.LayoutBlock;

import java.time.*;
import java.util.Collections;
import java.util.List;

public class ChannelReaderSlackBot {

	private static final String SLACK_BOT_TOKEN = "YOUR_SLACK_API_TOKEN";

	public static void main(String[] args) {
    	Slack slack = Slack.getInstance();
    	MethodsClient methods = slack.methods(SLACK_BOT_TOKEN);

    	String channelId = "YOUR_CHANNEL_ID";

    	LocalDateTime startTimeUTC = LocalDateTime.of(2023, Month.AUGUST, 3, 10, 0);
    	LocalDateTime endTimeUTC = LocalDateTime.of(2023, Month.AUGUST, 12, 15, 0);

    	long startTime = startTimeUTC.atZone(ZoneOffset.UTC).toEpochSecond();
    	long endTime = endTimeUTC.atZone(ZoneOffset.UTC).toEpochSecond();

    	ConversationsHistoryRequest request = ConversationsHistoryRequest.builder()
        	.channel(channelId)
        	.oldest(String.valueOf(startTime))
        	.latest(String.valueOf(endTime))
        	.build();

    	try {
        	ConversationsHistoryResponse response = methods.conversationsHistory(request);

        	if (response != null && response.isOk()) {
            	List<Message> messages = response.getMessages();
            	Collections.reverse(messages);
            	for (Message message : messages) {
                	String userId = message.getUser();
                	String timestamp = formatTimestamp(message.getTs());

                	UsersInfoRequest userInfoRequest = UsersInfoRequest.builder()
                    	.user(userId)
                    	.build();

                	UsersInfoResponse userInfoResponse = methods.usersInfo(userInfoRequest);
                	if (userInfoResponse != null && userInfoResponse.isOk()) {
                    	User user = userInfoResponse.getUser();
                    	System.out.println("User: " + user.getName());
                    	System.out.println("Timestamp: " + timestamp);
                    	System.out.println("Message: " + message.getText());
                    	System.out.println();
                	}
            	}
        	} else {
            	System.out.println("Failed to fetch messages: " + response.getError());
        	}
    	} catch (Exception e) {
        	e.printStackTrace();
    	}
	}

	private static String formatTimestamp(String ts) {
    	double timestamp = Double.parseDouble(ts);
    	Instant instant = Instant.ofEpochSecond((long) timestamp);
    	LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    	return dateTime.toString();
	}
}
