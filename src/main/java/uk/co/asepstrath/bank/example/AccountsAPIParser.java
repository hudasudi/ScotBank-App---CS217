package uk.co.asepstrath.bank.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AccountsAPIParser {
    private final String API_URL;

    public AccountsAPIParser(String api_url) {
        this.API_URL = api_url;
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
    public JsonArray parseJSONResponse() throws IOException, InterruptedException {
        HttpResponse<String> response = this.getAPIData();

        // System.out.println("STATUS => "+ response.statusCode());

        JsonParser parser = new JsonParser();
        JsonElement element_list = parser.parse(response.body());

        return element_list.getAsJsonArray();
    }

    /**
     * Take the parse JSON from the API response & prettify it, making it easier to read
     * @return Prettified JSON output as a string
     * @throws IOException if an I/O error occurs when sending or receiving, or the client has shut down
     * @throws InterruptedException if the operation is interrupted
     */
    public String prettifyJSONOutput() throws IOException, InterruptedException {
        JsonArray elements = this.parseJSONResponse();

        StringBuilder final_output = new StringBuilder();

        for(int i = 0; i < elements.size(); i++) {
            JsonObject element = elements.get(i).getAsJsonObject();

            String elem_out = "{\n";

            elem_out += "  id: " + element.get("id").toString() + "\n";
            elem_out += "  name: " + element.get("name").toString() + "\n";
            elem_out += "  startingBalance: " + element.get("startingBalance").getAsDouble() + "\n";
            elem_out += "  roundUpEnabled: " + element.get("roundUpEnabled").getAsBoolean() + "\n";
            elem_out += (i < elements.size()-1) ? "},\n" : "};\n";

            final_output.append(elem_out);
        }

        return final_output.toString();
    }
}
