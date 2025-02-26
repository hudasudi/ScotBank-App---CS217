package uk.co.asepstrath.bank.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountAPIManipulator {
	private final Logger log;
	private final DataSource ds;

	/** This class manipulates API information to format it into varied forms
	 * @param log The program log
	 * @param ds The DataSource to pull info from
	*/
	public AccountAPIManipulator(Logger log, DataSource ds) {
		this.log = log;
		this.ds = ds;
	}

	/** Get the API information stored in db, parse it into a JsonArray & return it
	 * @return The API Information
	*/
	public JsonArray getApiInformation() {
		JsonArray arr = new JsonArray();

		try(Connection conn = this.ds.getConnection()) {
			Statement stmt = conn.createStatement();

			ResultSet set = stmt.executeQuery("SELECT * FROM Accounts");

			while(set.next()) {
				JsonObject obj_to_insert = new JsonObject();

				obj_to_insert.addProperty("id", set.getString("UUID"));
				obj_to_insert.addProperty("name", set.getString("Name"));
				obj_to_insert.addProperty("startingBalance", set.getDouble("Balance"));
				obj_to_insert.addProperty("roundUpEnabled", set.getBoolean("roundUpEnabled"));

				arr.add(obj_to_insert);
			}

			stmt.close();
			conn.close();

			return arr;
		} catch(SQLException e) {
			log.error("An error occurred whilst trying to retrieve API information from the database", e);
			return null;
		}
	}

	/** Take the response JSON from the API, create a new Account from each element in the JSON array & return an ArrayList of created Accounts
	 * @return ArrayList<Account> of all accounts created from each JSON element
	*/
	public ArrayList<Account> jsonToAccounts() {
		JsonArray elements = this.getApiInformation();

		ArrayList<Account> accounts_list = new ArrayList<>();

		for(int i = 0; i < elements.size(); i++) {
			JsonObject element = elements.get(i).getAsJsonObject();

			accounts_list.add(new Account(
					element.get("id").toString(),
					element.get("name").toString(),
					element.get("startingBalance").getAsBigDecimal(),
					element.get("roundUpEnabled").getAsBoolean()
			));
		}

		return accounts_list;
	}

	/** Take the response JSON from the API & format it so that Handlebars can utilise the information
	 * @return Formatted (Handlebars compliant) map for the HTML template
	*/
	public Map<String, Object> createHandleBarsJSONMap() {
		// Get API information from file
		JsonArray json = this.getApiInformation();

		// Our List of JsonElements
		List<Map<String, String>> accounts_list = new ArrayList<>();

		// For every JsonElement
		for(int i = 0; i < json.size(); i++) {
			// Get the next element in the list (as an Object, so we can take information from it)
			JsonObject obj = json.get(i).getAsJsonObject();

			// Add the new map to our list
			accounts_list.add(createJsonMap(obj));
		}

		// This will take our big list of maps & chunk the elements into amounts of 25 (4 columns of 25 accounts each) so we can parse it in handlebars
		List<List<Map<String, String>>> chunked_accounts = chunkList(accounts_list, 25);

		// Make a map that'll be inserted through to Handlebars
		Map<String, Object> model = new HashMap<>();
		model.put("account_chunks", chunked_accounts);

		return model;
	}

	/** Take a JsonObject & Convert it into a Map of Key-Value Pairs
	 * @param object The JsonObject to convert
	 * @return A map of all the JsonObject's Key-Value Pairs
	*/
	public Map<String, String> createJsonMap(JsonObject object) {
		// The account map for the JsonObject
		Map<String, String> map = new HashMap<>();

		// Remove Quotes round the string
		String uuid = object.get("id").toString();
		uuid = uuid.substring(1, uuid.length()-1);

		String name = object.get("name").toString();
		name = name.substring(1, name.length()-1);

		// Put the JSON information into the account map
		map.put("uuid", uuid);
		map.put("name", name);
		map.put("bal", object.get("startingBalance").getAsString());
		map.put("round", object.get("roundUpEnabled").getAsBoolean() ? "Yes" : "No");

		return map;
	}

	/** Takes a list of all accounts, chunks them into sections of chunk_size then returns a new list in chunks of chunk_size
	 * @param list_to_chunk The list to chunk into sections
	 * @param chunk_size The number of elements per chunk in the list
	 * @return The newly chunked list
	 */
	private List<List<Map<String, String>>> chunkList(List<Map<String, String>> list_to_chunk, int chunk_size) {
		List<List<Map<String, String>>> chunks = new ArrayList<>();

		for(int i = 0; i < list_to_chunk.size(); i += chunk_size) {
			chunks.add(list_to_chunk.subList(i, Math.min(i + chunk_size, list_to_chunk.size())));
		}

		return chunks;
	}
}
