import java.io.IOException;

import okhttp3.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;


public class DALLEClient {

    public static void main(String[] args) {

        String openAIKey = "";
        String endpoint = "https://api.openai.com/v1/images/generations";
        String contentType = "application/json";
        String prompt = "a 35mm macro photo of 3 cute rottweiler puppies with no collars laying down in a field";
        int numberOfImages = 2;
        String size = "1024x1024";
      

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.get(contentType);
        
        // Create the Create Image JSON object
        CreateImage createImage = new CreateImage(prompt, numberOfImages, size);
        
        // Use Jackson ObjectMapper to convert the object to JSON string
        String json = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(createImage);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.Companion.create(json, mediaType);
        Request request = new Request.Builder()
                .url(endpoint)
                .method("POST", body)
                .addHeader("Content-Type", contentType)
                .addHeader("Authorization", "Bearer " + openAIKey)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                System.out.println(response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inner class for the CreateImage JSON Object
    public static class CreateImage {

        @JsonProperty("prompt")
        private String prompt;

        @JsonProperty("n")
        private int n;

        @JsonProperty("size")
        private String size;
    
        public CreateImage(String prompt, int n, String size) {
            this.prompt = prompt;
            this.n = n;
            this.size = size;
        }
    
    }
    
}

