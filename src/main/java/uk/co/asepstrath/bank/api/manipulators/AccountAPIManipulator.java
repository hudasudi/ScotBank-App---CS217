package uk.co.asepstrath.bank.api.manipulators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class AccountAPIManipulator extends APIManipulator {

	/**
	 * This class manipulates API information to format it into varied forms
	 * @param log The program log
	 * @param ds  The DataSource to pull info from
	*/
	public AccountAPIManipulator(Logger log, DataSource ds) {
		super(log, ds);
	}

	/** Make a JsonObject with a given set of information
	 * @param set The ResultSet to pull data from
	 * @return A JsonObject for a single result inside the ResultSet
	*/
	@Override
	protected JsonObject makeJsonObject(ResultSet set) {
		try {
			JsonObject object = new JsonObject();

			object.addProperty("id", set.getString("UUID"));
			object.addProperty("name", set.getString("Name"));
			object.addProperty("startingBalance", set.getBigDecimal("Balance"));
			object.addProperty("roundUpEnabled", set.getBoolean("roundUpEnabled"));

			return object;
		}

		catch(Exception e) {
			log.error("An error occurred whilst querying the database", e);
			return null;
		}
	}

	/** Create a map with an accounts information
	 * @param account The account to turn into a map
	 * @return The map with account information
	*/
	public Map<String, Object> createAccountMap(Account account) {
		// Make sure we don't crash on null
		if(account == null) {
			return null;
		}

		Map<String, Object> map = new HashMap<>();

		map.put("uuid", account.getID());
		map.put("name", account.getName());
		map.put("bal", account.getBalance().toString());
		map.put("round", account.isRoundUpEnabled() ? "Yes" : "No");

		return map;
	}

	/** The query used on the Database
	 * @return The query used in the database
	*/
	@Override
	protected String getTableQuery() {
		return "SELECT * FROM Accounts";
	}

	/**
     * Take a JsonObject & Convert it into a Map of Key-Value Pairs
     *
     * @param object The JsonObject to convert
     * @return A map of all the JsonObject's Key-Value Pairs
     */
	@Override
	public Map<String, Object> createJsonMap(JsonObject object) {
		// Make sure we don't crash on null
		if(object == null) {
			return null;
		}

		// The account map for the JsonObject
		Map<String, Object> map = new HashMap<>();

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

	/** Find an account with a specific uuid
	 * @param uuid The uuid to search for
	 * @return The account with a specified uuid
	*/
	public Account getAccountByUUID(String uuid) {
		ArrayList<Account> accounts = this.jsonToAccounts();

		for(Account a : accounts) {
			if(a.getID().equals(uuid)) {
				return a;
			}
		}

		return null;
	}
}