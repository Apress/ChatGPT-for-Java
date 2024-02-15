import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ListModels {

	public static void main(String[] args) {
    	try {
        	// Create URL object for the API endpoint
        	URL url = new URL("https://api.openai.com/v1/models");
       	 
        	// Create HttpURLConnection object
        	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
       	 
        	// Set request method
        	conn.setRequestMethod("GET");
       	 
        	// Set Authorization header
        	conn.setRequestProperty("Authorization", "Bearer $OPENAI_API_KEY");
       	 
        	// Get response code
        	int responseCode = conn.getResponseCode();
       	 
        	// If the request was successful (status code 200)
        	if (responseCode == HttpURLConnection.HTTP_OK) {
            	// Read the response from the API
            	BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            	String inputLine;
            	StringBuilder response = new StringBuilder();
            	while ((inputLine = in.readLine()) != null) {
                	response.append(inputLine);
            	}
            	in.close();
           	 
            	// Print the response
            	System.out.println(response.toString());
        	} else {
            	System.out.println("Request failed. Response Code: " + responseCode);
        	}
       	 
        	// Close the connection
        	conn.disconnect();
    	} catch (Exception e) {
        	e.printStackTrace();
    	}
	}

}
