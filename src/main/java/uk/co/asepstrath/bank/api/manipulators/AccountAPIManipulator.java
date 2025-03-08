package uk.co.asepstrath.bank.api.manipulators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;

import javax.sql.DataSource;

import java.sql.*;

import java.util.*;

public class AccountAPIManipulator extends APIManipulator {

	public AccountAPIManipulator(Logger log, DataSource ds) {
		super(log, ds);
	}

	@Override
	protected JsonObject makeJsonObject(ResultSet set) {
		try {
			JsonObject object = new JsonObject();

			object.addProperty("id", set.getString("UUID"));
			object.addProperty("name", set.getString("Name"));
			object.addProperty("startingBalance", set.getString("Balance"));
			object.addProperty("roundUpEnabled", set.getBoolean("roundUpEnabled"));

			return object;
		}

		catch(Exception e) {
			log.error("An error occurred whilst querying the database", e);
			return null;
		}
	}

	@Override
	protected String getTableQuery() {
		return "SELECT * FROM Accounts";
	}

	/** Take a JsonObject & Convert it into a Map of Key-Value Pairs
	 * @param object The JsonObject to convert
	 * @return A map of all the JsonObject's Key-Value Pairs
	*/
	@Override
	public Map<String, String> createJsonMap(JsonObject object) {
		// The account map for the JsonObject
		Map<String, String> map = new HashMap<>();

		// Put the JSON information into the account map
		map.put("uuid", object.get("id").getAsString());
		map.put("name", object.get("name").getAsString());
		map.put("bal", object.get("startingBalance").getAsString());
		map.put("round", object.get("roundUpEnabled").getAsBoolean() ? "Yes" : "No");

		return map;
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
					element.get("id").getAsString(),
					element.get("name").getAsString(),
					element.get("startingBalance").getAsBigDecimal(),
					element.get("roundUpEnabled").getAsBoolean()
			));
		}

		return accounts_list;
	}
}