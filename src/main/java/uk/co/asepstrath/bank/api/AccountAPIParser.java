package uk.co.asepstrath.bank.api;

import com.google.gson.*;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;

public class AccountAPIParser {
    private final String API_URL;
    private final Logger log;
    private final DataSource ds;

    /** This class takes the API URL, gets the response & parses it into a usable format for other functions
     * @param api_url The URL to the API
     * @param ds The DataSource to write to
     * @param log The program log
    */
    public AccountAPIParser(Logger log, String api_url, DataSource ds) {
        this.API_URL = api_url;
        this.log = log;
        this.ds = ds;
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

    /**
     * Write the API JSON response to the db for use later
    */
    public void writeAPIInformation() {
        JsonArray response_array = this.parseJSONResponse();

        try(Connection conn = this.ds.getConnection()) {
            String insert_acc = "INSERT INTO Accounts " + "VALUES (?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(insert_acc);

            for(int i = 0; i < response_array.size(); i++) {
                JsonObject obj = response_array.get(i).getAsJsonObject();

                stmt.setString(1, removeQuotes(obj.get("id").toString()));
                stmt.setString(2, removeQuotes(obj.get("name").toString()));
                stmt.setDouble(3, obj.get("startingBalance").getAsDouble());
                stmt.setBoolean(4, obj.get("roundUpEnabled").getAsBoolean());

                stmt.addBatch();
            }

            stmt.executeBatch();

            stmt.close();
            conn.close();

            log.info("Successfully wrote API information to the database");

        } catch (SQLException e) {
			log.error("An error occurred whilst trying to write API information to the database", e);
        }
    }

    private String removeQuotes(String str) {
        return str.substring(1, str.length() - 1);
    }
}