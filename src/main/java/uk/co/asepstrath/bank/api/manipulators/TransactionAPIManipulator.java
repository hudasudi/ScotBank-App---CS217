package uk.co.asepstrath.bank.api.manipulators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.slf4j.Logger;

import uk.co.asepstrath.bank.Transaction;

import javax.sql.DataSource;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransactionAPIManipulator extends APIManipulator {

	public TransactionAPIManipulator(Logger log, DataSource ds) {
		super(log, ds);
	}

	@Override
	protected JsonObject makeJsonObject(ResultSet set) {
		try {
			JsonObject object = new JsonObject();

			object.addProperty("timestamp", set.getString("Timestamp"));
			object.addProperty("amount", set.getDouble("Amount"));
			object.addProperty("sender", set.getString("Sender"));
			object.addProperty("id", set.getString("TransactionID"));
			object.addProperty("recipient", set.getString("Recipient"));
			object.addProperty("type", set.getString("Type"));

			return object;
		}

		catch(Exception e) {
			log.error("An error occurred whilst querying the database", e);
			return null;
		}
	}

	@Override
	protected String getTableQuery() {
		return "SELECT * FROM Transactions";
	}

	/**
	 * Take a JsonObject & Convert it into a Map of Key-Value Pairs
	 *
	 * @param object The JsonObject to convert
	 * @return A map of all the JsonObject's Key-Value Pairs
	 */
	@Override
	public Map<String, String> createJsonMap(JsonObject object) {
		Map<String, String> map = new HashMap<>();

		map.put("timestamp", object.get("timestamp").getAsString());
		map.put("amount", object.get("amount").getAsString());
		map.put("sender", object.get("sender").getAsString());
		map.put("id", object.get("id").getAsString());
		map.put("recipient", object.get("recipient").getAsString());
		map.put("type", object.get("type").getAsString());

		return map;
	}

	/**
	 * Take the response JSON from the API, create a new Transaction from each element in the JSON array & return an ArrayList of created Transactions
	 *
	 * @return ArrayList<Transaction> of all transactions created from each JSON element
	 */
	public ArrayList<Transaction> jsonToTransactions() {
		JsonArray elements = this.getApiInformation();

		ArrayList<Transaction> transactions = new ArrayList<>();

		for (int i = 0; i < elements.size(); i++) {
			JsonObject element = elements.get(i).getAsJsonObject();

			transactions.add(new Transaction(
					element.get("timestamp").getAsString(),
					element.get("amount").getAsBigDecimal(),
					element.get("sender").getAsString(),
					element.get("id").getAsString(),
					element.get("recipient").getAsString(),
					element.get("type").getAsString()
			));
		}

		return transactions;
	}
}