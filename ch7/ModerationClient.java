
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


public class ModerationClient {

    // OpenAI parameters that we already know how to use
    String openAIKey = "";
    String endpoint = "https://api.openai.com/v1/moderations";
    String model = "text-moderation-latest";

    // The constructor
    public ModerationClient() {
    }

    public ModerationResponse checkForObjectionalContent(String discordMessageText) {

        ModerationResponse moderationResponse = null;

        String jsonInput = null;
        try {
            ObjectMapper mapper = new ObjectMapper();

            ModRequest modRequest = new ModRequest(discordMessageText, model);

            jsonInput = mapper.writeValueAsString(modRequest);
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
                //System.out.println(response.toString());
                // Extract the answer from JSON
                moderationResponse = getModerationResponsefromJSON(response.toString());
                String answerFromChatGPT = moderationResponse.toString();
                System.out.println(answerFromChatGPT);
            } else {
                System.out.println("Error: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return moderationResponse;
    }

    public ModerationResponse getModerationResponsefromJSON(String jsonResponse) {
        ModerationResponse response = new ModerationResponse();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode resultsNode = rootNode.path("results");
            if (!resultsNode.isMissingNode() && resultsNode.isArray() && resultsNode.size() > 0) {
                JsonNode resultNode = resultsNode.get(0);
                response.isFlagged = resultNode.path("flagged").asBoolean(false);
                JsonNode categoriesNode = resultNode.path("categories");
                if (!categoriesNode.isMissingNode()) {
                    categoriesNode.fields().forEachRemaining(entry -> {
                        if (entry.getValue().asBoolean(false)) {
                            response.offendingCategories.add(entry.getKey());
                        }
                    });
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response;
    }
    

    class ModerationResponse {
        boolean isFlagged = false;
        ArrayList<String> offendingCategories = new ArrayList<>();

        @Override
        public String toString() {
            return "ModerationResponse{" +
                    "isFlagged=" + isFlagged +
                    ", offendingCategories=" + offendingCategories +
                    '}';
        }
    }


}

