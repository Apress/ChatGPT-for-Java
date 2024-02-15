import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Chat {
    @JsonProperty("model")
    private String model;

    @JsonProperty("messages")
    private List<Message> messages;

    @JsonProperty("temperature")
    private float temperature;

    @JsonProperty("max_tokens")
    private int max_tokens;

    @JsonProperty("top_p")
    private float top_p;

    @JsonProperty("frequency_penalty")
    private int frequency_penalty;

    @JsonProperty("presence_penalty")
    private int presence_penalty;

    private Chat(ChatBuilder builder) {
        this.model = builder.model;
        this.messages = builder.messages;
        this.temperature = builder.temperature;
        this.max_tokens = builder.max_tokens;
        this.top_p = builder.top_p;
        this.frequency_penalty = builder.frequency_penalty;
        this.presence_penalty = builder.presence_penalty;
    }

    public static ChatBuilder builder() {

        // we need a default message here to avoid 400 errors from the API
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", "You are a helpful assistant"));
        messages.add(new Message("user", "hello"));

        return new ChatBuilder().messages(messages);
    }

    public static class ChatBuilder {
        private String model = "gpt-3.5-turbo";
        private List<Message> messages = null;
        private float temperature = 1.0f;
        private int max_tokens = 2048;
        private float top_p = 0f;
        private int frequency_penalty = 0;
        private int presence_penalty = 0;

        private ChatBuilder() {
    
        }

        public ChatBuilder model(String model) {
            this.model = model;
            return this;
        }

        public ChatBuilder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public ChatBuilder temperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        public ChatBuilder maxTokens(int max_tokens) {
            this.max_tokens = max_tokens;
            return this;
        }

        public ChatBuilder topP(float top_p) {
            this.top_p = top_p;
            return this;
        }

        public ChatBuilder frequencyPenalty(int frequency_penalty) {
            this.frequency_penalty = frequency_penalty;
            return this;
        }

        public ChatBuilder presencePenalty(int presence_penalty) {
            this.presence_penalty = presence_penalty;
            return this;
        }

        public Chat build() {
            return new Chat(this);
        }


    }
}
