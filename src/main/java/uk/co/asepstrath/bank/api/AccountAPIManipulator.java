package uk.co.asepstrath.bank.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import uk.co.asepstrath.bank.Account;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountAPIManipulator {
	private final String API_FILE;

	public AccountAPIManipulator(String api_file) {
		this.API_FILE = api_file;
	}

	/** Get the API information stored in the JSON file, parse it into a JsonArray & return it
	 * @return The API Information
	*/
	public JsonArray getApiInformation() {
		try {
			Gson gson = new Gson();

			return gson.fromJson(new FileReader(API_FILE), JsonArray.class);

		} catch(IOException e) {
			System.out.println("Error whilst getting api information");
			e.printStackTrace();

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

	public Map<String, Object> manip_ls() {
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

	private Map<String, String> createJsonMap(JsonObject object) {
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
