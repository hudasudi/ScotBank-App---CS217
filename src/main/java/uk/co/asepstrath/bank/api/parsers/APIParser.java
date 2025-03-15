package uk.co.asepstrath.bank.api.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import javax.sql.DataSource;
import java.sql.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class APIParser {
	final String API_URL;
	final Logger log;
	final DataSource ds;

	/** This class takes the API URL, gets the response & parses it into a usable format for other functions
	 * @param api_url The URL to the API
	 * @param ds The DataSource to write to
	 * @param log The program log
	*/
	public APIParser(String api_url, Logger log, DataSource ds) {
		this.API_URL = api_url;
		this.log = log;
		this.ds = ds;
	}

	/**
	 * Create a builder for the API GET request
	 * @return The request for the API
	*/
	protected HttpRequest createRequest() {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(API_URL));

		return builder.build();
	}

	/**
	 * Get & return the endpoint response with the API data
	 * @return Endpoint response from API
	*/
	protected HttpResponse<String> getAPIData() {
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = createRequest();

			return client.send(request, HttpResponse.BodyHandlers.ofString());
		}

		catch(IOException | InterruptedException e) {
			log.error("An error occurred whilst trying to query the API", e);
			return null;
		}
	}

	protected String getResponseString() {
		HttpResponse<String> response = this.getAPIData();

		if(response == null || response.statusCode() != 200) {
			log.error("Something went wrong when trying to query the API");
			return null;
		}

		return response.body();
	}

	/**
	 * Take the response from the API, parse the data inside it & return it as a JsonArray
	 * @return JsonArray of all the Elements inside the response
	*/
	protected abstract JsonArray parseResponse();

	/** Get the parsers SQL insert query
	 * @return The insert query
	*/
	protected abstract String getInsertQuery();

	/** Bind a given object to the statement before executing
	 * @param obj The object to bind
	 * @param stmt The statement to bind to
	 * @throws SQLException If an error occurs with the SQL statement
	*/
	protected abstract void bindDataToStatement(JsonObject obj, PreparedStatement stmt) throws SQLException;

    /**
     * Write the API JSON response to the db for use later
    */
	@SuppressWarnings("All")
	public void writeAPIInformation() {
		JsonArray response_array = this.parseResponse();

		try(Connection conn = this.ds.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(this.getInsertQuery());

			if(response_array != null) {
				for(int i = 0; i < response_array.size(); i++) {
					JsonObject obj = response_array.get(i).getAsJsonObject();

					bindDataToStatement(obj, stmt);
					stmt.addBatch();
				}

				stmt.executeBatch();
			}

			stmt.close();
		}

		catch(SQLException e) {
			log.error("An error occurred whilst trying to write API Information to the database", e);
		}
	}

	/** Remove the quotes around certain saved elements
	 * @param str The String to trim
	 * @return Then trimmed String
	*/
	protected String trimString(String str, int s, int e) {
		return str.substring(s, str.length() - e);
	}
}
