import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ChatGPTClientForQAandModeration {
    
    //
    //  OpenAI parameters that we already know how to use
    //
    String openAIKey = "";
    String endpoint = "https://api.openai.com/v1/chat/completions";
    String model = "gpt-4";
    float temperature = 1.0f;
    int max_tokens = 256;
    float top_p = 1.0f;
    int frequency_penalty = 0;
    int presence_penalty = 0;

    String systemMessage = null;
    String initialInstructionsToChatGPT = null;

    //
    // The constructor needs to be passed the contents from the FAQ.txt file
    // and the system message
    //
    public ChatGPTClientForQAandModeration(String systemMessage, String initialInstructionsToChatGPT) {
        this.systemMessage = systemMessage;
        this.initialInstructionsToChatGPT = initialInstructionsToChatGPT;
    }

    public String sendMessageFromDiscordUser(String discordMessageText) {

        String answerFromChatGPT = "";

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemMessage));
        messages.add(new Message("user", initialInstructionsToChatGPT));
        messages.add(new Message("user", discordMessageText));

        String jsonInput = null;
        try {
            ObjectMapper mapper = new ObjectMapper();

            Chat chat = Chat.builder()
                .model(model)
                .messages(messages)
                .temperature(temperature)
                .maxTokens(max_tokens)
                .topP(top_p)
                .frequencyPenalty(frequency_penalty)
                .presencePenalty(presence_penalty)
                .build();

            jsonInput = mapper.writeValueAsString(chat);
            System.out.println(jsonInput);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + openAIKey);
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonInput.getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Print the response
                answerFromChatGPT = extractAnswerFromJSON(response.toString());
                System.out.println(answerFromChatGPT);
            } else {
                System.out.println("Error: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return answerFromChatGPT;
    }

    //
    // We are only interested in the "message.content" in the JSON response
    // So here's the easy way to extract that
    //
    public String extractAnswerFromJSON(String jsonResponse) {
        String chatGPTAnswer = "";

         try {
            // Create an ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse the JSON string
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Extract the "content" parameter
            JsonNode contentNode = rootNode.at("/choices/0/message/content");
            chatGPTAnswer = contentNode.asText();

            System.out.println("Content: " + chatGPTAnswer);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chatGPTAnswer;
    }
}
