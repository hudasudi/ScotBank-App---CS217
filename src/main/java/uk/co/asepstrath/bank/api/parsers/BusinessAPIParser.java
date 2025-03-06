package uk.co.asepstrath.bank.api.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BusinessAPIParser extends APIParser {

	/**
	 * This class takes the API URL, gets the response & parses it into a usable format for other functions
	 * @param api_url The URL to the API
	 * @param log     The program log
	 * @param ds      The DataSource to write to
	 */
	public BusinessAPIParser(Logger log, String api_url, DataSource ds) {
		super(api_url, log, ds);
	}

	@Override
	protected JsonArray parseResponse() {
		String response = this.getResponseString();
		String[] lines = response.split("\n");

		JsonArray out = new JsonArray();

		for(int i = 1; i < lines.length; i++) {
			String[] parts = lines[i].split(",");
			JsonObject obj = new JsonObject();

			obj.addProperty("id", parts[0]);
			obj.addProperty("name", parts[1]);
			obj.addProperty("category", parts[2]);
			obj.addProperty("sanctioned", trimString(parts[3], 0, 1));

			out.add(obj);
		}

		return out;
	}

	@Override
	protected String getInsertQuery() {
		return "INSERT INTO Businesses (ID, Name, Category, Sanctioned) VALUES (?, ?, ?, ?)";
	}

	@Override
	protected void bindDataToStatement(JsonObject obj, PreparedStatement stmt) throws SQLException {
		stmt.setString(1, trimString(obj.get("id").toString(), 1, 1));
		stmt.setString(2, trimString(obj.get("name").toString(), 1, 1));
		stmt.setString(3, trimString(obj.get("category").toString(), 1, 1));
		stmt.setBoolean(4, obj.get("sanctioned").getAsBoolean());
	}
}