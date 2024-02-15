import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
