package uk.co.asepstrath.bank.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.json.*;
import org.json.XML;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionAPIParser{

    private final String API_URL;
    private final Logger log;
    private final DataSource ds;

    public TransactionAPIParser(Logger log, String api_url, DataSource ds){
        this.log = log;
        this.API_URL = api_url;
        this.ds = ds;
    }

    public HttpResponse<String> getAPIData(int currentPage) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.API_URL + "?page=" + currentPage + "&size=1000"))
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

    public void writeAPIInformation() {
        JsonArray response_array = this.parseJSONResponse();
        JsonParser parser = new JsonParser();
        try(Connection conn = this.ds.getConnection()) {
            String insert_transac = "INSERT INTO Transactions " + "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(insert_transac);

            for(int i = 0; i < response_array.size(); i++) {
                JsonObject obj = response_array.get(i).getAsJsonObject();
                System.out.println(i);

                stmt.setString(1, removeQuotes(obj.get("timestamp").toString()));
                stmt.setDouble(2, obj.get("amount").getAsDouble());
                if(obj.get("from") != null) {
                    stmt.setString(3, removeQuotes(obj.get("from").toString()));
                }
                stmt.setString(4, removeQuotes(obj.get("id").toString()));
                if(obj.get("to") != null) {
                    stmt.setString(5, removeQuotes(obj.get("to").toString()));
                }
                stmt.setString(6, removeQuotes(obj.get("type").toString()));

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

    public JsonArray parseJSONResponse() {
        try {
            JsonArray outArray = new JsonArray();
            JsonParser parser = new JsonParser();
            boolean isLastPage = false;
            for(int i = 0; !isLastPage;i++){
                log.info("Working on api page: " + i);
                HttpResponse<String> response = this.getAPIData(i);

                if(response.statusCode() == 200) {
                    JSONObject test = XML.toJSONObject(response.body());
                    JsonObject temp = parser.parse(test.toString()).getAsJsonObject();
                    JsonObject responseJson = parser.parse(temp.get("pageResult").toString()).getAsJsonObject();
                    isLastPage = !(responseJson.get("hasNext").getAsBoolean());
                    outArray.addAll(responseJson.get("results").getAsJsonArray());
                } else {
                    throw new InterruptedException();
                }
            }
            return outArray;
        }

        catch(InterruptedException e) {
            log.error("There was an error whilst retrieving & parsing the JSON information => Response code not OK", e);
            return null;
        }
    }

    private String removeQuotes(String str) {
        return str.substring(1, str.length() - 1);
    }

}
