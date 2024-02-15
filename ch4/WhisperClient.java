public class WhisperClient {

    public static void main(String[] args) throws IOException {
        // API key for OpenAI (this should be replaced with your actual API key)
        String openAIKey = "";
        // OpenAI transcription endpoint
        String endpoint = "https://api.openai.com/v1/audio/transcriptions";
        // Model used for transcription
        String model = "whisper-1";
        // Media type for the MP3 files
        MediaType MEDIA_TYPE_MP3 = MediaType.parse("audio/mpeg");
        // Folder containing the MP3 files to be transcribed
        String mp3FolderPath = "/Users/me/audio/segments";
        // Desired format for the transcription response
        String responseFormat = "text";

        // Configure the HTTP client with specified timeouts
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

        // List to store all mp3 files from the directory
        List<File> mp3Files = new ArrayList<>();

        // Try to collect all mp3 files in the directory and store them in the list
        try (Stream<Path> paths = Files.walk(Paths.get(mp3FolderPath))) {
            mp3Files = paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".mp3"))
                .map(Path::toFile)
                .sorted(Comparator.comparing(File::getName)) // Sort the files alphabetically
                .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("File reading error: " + e.getMessage());
            return; // Exit if there's an error reading the files
        }

        // Iterate over each MP3 file, transcribe it, and print the response
        for (File mp3File : mp3Files) {
            // Construct the request body for transcription
            RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    mp3File.getName(),
                    RequestBody.create(mp3File, MEDIA_TYPE_MP3))
                .addFormDataPart("model", model)
                .addFormDataPart("response_format", responseFormat)
                .build();

            // Build the HTTP request
            Request request = new Request.Builder()
                .url(endpoint)
                .header("Authorization", "Bearer " + openAIKey)
                .post(requestBody)
                .build();

            // Make the request and process the response
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                System.out.println(response.body().string());
            } catch (IOException e) {
                System.out.println("Request error for file: " + mp3File.getName() + " - " + e.getMessage());
            }
        }
    }
}