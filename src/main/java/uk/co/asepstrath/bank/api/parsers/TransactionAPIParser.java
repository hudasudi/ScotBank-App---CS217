package uk.co.asepstrath.bank.api.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;
import org.json.XML;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import javax.sql.DataSource;
import java.net.http.HttpResponse;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class TransactionAPIParser extends APIParser {

	public TransactionAPIParser(Logger log, String api_url, DataSource ds) {
		super(api_url, log, ds);
	}

	protected HttpRequest createRequest(int page) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(API_URL + "?page=" + page + "&size=1000"));

		return builder.build();
	}

	private String getResponseStringForPage(int page) {
		HttpResponse<String> response = this.getAPIDataForPage(page);

		if(response == null || response.statusCode() != 200) {
			log.error("Something went wrong when trying to query the API");
			return null;
		}

		return response.body();
	}

	private HttpResponse<String> getAPIDataForPage(int page) {
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = this.createRequest(page);

			return client.send(request, HttpResponse.BodyHandlers.ofString());
		}

		catch (IOException | InterruptedException e) {
			log.error("An error occurred whilst trying to query the API", e);
			return null;
		}
	}

	@Override
	protected JsonArray parseResponse() {
		JsonArray out = new JsonArray();
		boolean is_last_page = false;

		for (int i = 0; !is_last_page; i++) {
			String response = this.getResponseStringForPage(i);

			if(response != null) {
				JSONObject xml_obj = XML.toJSONObject(response);
				JsonObject obj = JsonParser.parseString(xml_obj.toString()).getAsJsonObject();
				JsonObject responseJson = JsonParser.parseString(obj.get("pageResult").toString()).getAsJsonObject();

				is_last_page = !(responseJson.get("hasNext").getAsBoolean());

				out.addAll(responseJson.get("results").getAsJsonArray());
			} else {
				log.error("response body is null, skipping page");
				break;
			}
		}

		return out;
	}

	@Override
	protected String getInsertQuery() {
		return "INSERT INTO Transactions (Timestamp, Amount, Sender, TransactionID, Recipient, Type) VALUES (?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected void bindDataToStatement(JsonObject obj, PreparedStatement stmt) throws SQLException {
		stmt.setString(1, trimString(obj.get("timestamp").toString(), 1, 1));
		stmt.setDouble(2, obj.get("amount").getAsDouble());

		if(obj.get("from") != null) {
			stmt.setString(3, trimString(obj.get("from").toString(), 1, 1));
		}

		else {
			stmt.setNull(3, Types.NULL);
		}

		stmt.setString(4, trimString(obj.get("id").toString(), 1, 1));

		if (obj.get("to") != null) {
			stmt.setString(5, trimString(obj.get("to").toString(), 1, 1));
		}

		else {
			stmt.setNull(5, Types.NULL);
		}

		stmt.setString(6, trimString(obj.get("type").toString(), 1, 1));
	}
}