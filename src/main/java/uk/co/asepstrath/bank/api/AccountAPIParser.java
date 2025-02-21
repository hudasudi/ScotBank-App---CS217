package uk.co.asepstrath.bank.api;

import com.google.gson.*;
import org.slf4j.Logger;

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
    private final Logger log;

    /** This class takes the API URL, gets the response & parses it into a usable format for other functions
     * @param api_url The URL to the API
     * @param api_file The PATH for the file to be populated with API information
     * @param log The program log
    */
    public AccountAPIParser(Logger log, String api_url, String api_file) {
        this.API_URL = api_url;
        this.API_FILE = api_file;
        this.log = log;
    }

    /**
     * Get & return the endpoint response with the account information
     * @return Endpoint response from API
    */
    private HttpResponse<String> getAPIData() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.API_URL))
                    .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        catch(IOException e) {
            log.error("An error occurred whilst trying to query the API", e);
            return null;
        }

        catch(InterruptedException e) {
            log.error("An error occurred that interrupted querying the API", e);
            return null;
        }
    }

    /**
     * Take the response from the API, parse the JSON inside it & return it as a JsonArray
     * @return JsonArray of all the JSON Elements
    */
    private JsonArray parseJSONResponse() {
        try {
            HttpResponse<String> response = this.getAPIData();

            if(response.statusCode() == 200) {
                JsonParser parser = new JsonParser();

                return parser.parse(response.body()).getAsJsonArray();
            } else {
                throw new InterruptedException();
            }
        }

        catch(InterruptedException e) {
            log.error("There was an error whilst retrieving & parsing the JSON information => Response code not OK", e);
            return null;
        }
    }

    /** Write the API JSON response to a file for use later
     */
    public void writeAPIInformation() {
        JsonArray response_array = this.parseJSONResponse();

        try {
            File file = new File(this.API_FILE);
            file.createNewFile();
        }

        catch(IOException e) {
            log.error("An error occurred whilst creating the API information file", e);
            return;
        }

        try(FileWriter writer = new FileWriter(this.API_FILE)) {
            writer.write(response_array.toString());
        }

        catch(IOException e) {
            log.error("An error occurred whilst trying to write API information to the JSON file", e);
        }
    }

    /** On stopping the program, this function will wipe the account information from the file
    */
    public void removeAPIInformation() {
        try {
            File api_info = new File(API_FILE);

            if(api_info.delete()) {
                System.out.println("API File deleted successfully");
            }

            else {
                throw new IOException();
            }
        }

        catch(IOException e) {
            log.error("An error occurred whilst trying to delete the API file", e);
        }
    }
}