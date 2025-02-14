package uk.co.asepstrath.bank.api;

import com.google.gson.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AccountAPIParser {
    private final String API_URL;
    private final String API_FILE;

    public AccountAPIParser(String api_url, String api_file) {
        this.API_URL = api_url;
        this.API_FILE = api_file;
    }

    // FROM => https://medium.com/@felvid/java-http-client-guide-a9b18920d2a2

    /**
     * Get & return the endpoint response with the account information
     * @return Endpoint response from API
     * @throws IOException if an I/O error occurs when sending or receiving, or the client has shut down
     * @throws InterruptedException if the operation is interrupted
    */
    private HttpResponse<String> getAPIData() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.API_URL))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Take the response from the API, parse the JSON inside of it & return it as a JsonArray
     * @return JsonArray of all the JSON Elements
     * @throws IOException if an I/O error occurs when sending or receiving, or the client has shut down
     * @throws InterruptedException if the operation is interrupted
    */
    private JsonArray parseJSONResponse() throws IOException, InterruptedException {
        try {
            HttpResponse<String> response = this.getAPIData();

            if(response.statusCode() == 200) {
                JsonParser parser = new JsonParser();

                return parser.parse(response.body()).getAsJsonArray();
            } else {
                throw new InterruptedException();
            }
        } catch(InterruptedException e) {
            System.out.println("There was an error whilst retrieving & parsing the JSON information\nResponse code not OK");
            e.printStackTrace();
        } catch(IOException e) {
            System.out.println("There was an error whilst retrieving & parsing the JSON information");
            e.printStackTrace();
        }

        HttpResponse<String> response = this.getAPIData();

        JsonParser parser = new JsonParser();
        JsonElement element_list = parser.parse(response.body());

        return element_list.getAsJsonArray();
    }

    /** Write the API JSON response to a file for use later
     * @throws IOException if an I/O error occurs when sending or receiving, or the client has shut down
     * @throws InterruptedException if the operation is interrupted
     */
    public void writeAPIInformation() throws IOException, InterruptedException {
        JsonArray response_array = this.parseJSONResponse();

        try {
            File file = new File(this.API_FILE);
            file.createNewFile();
        } catch(IOException e) {
            System.out.println("Error whilst creating api information file");
            e.printStackTrace();
        }

        try(FileWriter writer = new FileWriter(this.API_FILE)) {
            writer.write(response_array.toString());
        } catch(IOException e) {
            System.out.println("Error: Could not write API information to JSON file");
            e.printStackTrace();
        }
    }

    /** On stopping the program, this function will wipe the account information from the file
     * @throws IOException if an I/O error occurs when sending or receiving, or the client has shut down
    */
    public void removeAPIInformation() throws IOException {
        try {
            File api_info = new File("src/main/resources/assets/api.json");

            if(api_info.delete()) {
                System.out.println("API File deleted successfully");
            } else {
                throw new IOException("There was an error whilst trying to delete the API File");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}