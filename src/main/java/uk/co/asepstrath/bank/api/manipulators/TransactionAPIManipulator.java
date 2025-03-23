package uk.co.asepstrath.bank.api.manipulators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Transaction;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransactionAPIManipulator extends APIManipulator {

	/**
	 * This class manipulates API information to format it into varied forms
	 * @param log The program log
	 * @param ds  The DataSource to pull info from
	*/
	public TransactionAPIManipulator(Logger log, DataSource ds) {
		super(log, ds);
	}

	/** Make a JsonObject with a given ResultSet
	 * @param set The ResultSet to pull data from
	 * @return a JsonObject with ResultSet values
	*/
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

	/** Get the database query to use on a database
	 * @return The database query
	*/
	@Override
	protected String getTableQuery() {
		return "SELECT * FROM Transactions";
	}

	/** Make a Transaction object with a given ResultSet
	 * @param set The ResultSet to pull data from
	 * @return Transaction object with ResultSet information
	*/
	private Transaction makeTransaction(ResultSet set) {
		try {
			return new Transaction(
				set.getString("Timestamp"),
				set.getBigDecimal("Amount"),
				set.getString("Sender"),
				set.getString("TransactionID"),
				set.getString("Recipient"),
				set.getString("Type")
			);
		}

		catch(Exception e) {
			log.error("An error occurred whilst building a new transaction", e);
			return null;
		}
	}

	/** Take a JsonObject & Convert it into a Map of Key-Value Pairs
     * @param object The JsonObject to convert
     * @return A map of all the JsonObject's Key-Value Pairs
    */
	@Override
	public Map<String, Object> createJsonMap(JsonObject object) {
		if(object == null) {
			return null;
		}

		Map<String, Object> map = new HashMap<>();

		map.put("timestamp", object.get("timestamp").getAsString());
		map.put("amount", object.get("amount").getAsString());
		map.put("sender", object.get("sender").getAsString());
		map.put("id", object.get("id").getAsString());
		map.put("recipient", object.get("recipient").getAsString());
		map.put("type", object.get("type").getAsString());

		return map;
	}

	/** Create a map with information from a Transaction
	 * @param transaction The transaction to map
	 * @return A map containing information from a Transaction
	*/
	public Map<String, Object> createTransactionMap(Transaction transaction) {
		if(transaction == null) {
			return null;
		}

		Map<String, Object> map = new HashMap<>();

		map.put("timestamp", transaction.getTimestamp().split(" ")[0]);
		map.put("amount", transaction.getAmount());
		map.put("sender", transaction.getSender() == null ? "Branch Deposit" : transaction.getSender());
		map.put("id", transaction.getID());
		map.put("recipient", transaction.getRecipient() == null ? "ATM" : transaction.getRecipient());
		map.put("type", transaction.getType());
		map.put("processed", transaction.isProcessed() ? "PROCESSED" : "DECLINED");

		return map;
	}

	/** Get all transactions relating to a specific account uuid
	 * @param uuid The uuid to check for
	 * @return A list of all transactions related to the account
	*/
	public ArrayList<Transaction> getTransactionForAccount(String uuid) {
		try(Connection conn = this.ds.getConnection()) {
			ArrayList<Transaction> transactions = new ArrayList<>();

			String query = "SELECT t.Timestamp, t.Amount, t.TransactionID, " +
					"COALESCE(a.Name, b.Name, t.Sender) AS Sender, " +
					"COALESCE(c.Name, d.Name, t.Recipient) AS Recipient, " +
					"t.Type " +
					"FROM Transactions t " +
					"LEFT JOIN Accounts a ON t.Sender = a.UUID " +
					"LEFT JOIN Businesses b ON t.Sender = b.ID " +
					"LEFT JOIN Accounts c ON t.Recipient = c.UUID " +
					"LEFT JOIN Businesses d ON t.Recipient = d.ID " +
					"WHERE t.Sender = ? OR t.Recipient = ? " +
					"ORDER BY t.Timestamp ASC";

				PreparedStatement stmt = conn.prepareStatement(query);

				stmt.setString(1, uuid);
				stmt.setString(2, uuid);

				ResultSet set = stmt.executeQuery();

				while(set.next()) {
					transactions.add(this.makeTransaction(set));
				}

			return transactions;
		}

		catch(SQLException e) {
			log.error("An error occurred whilst querying the database", e);
			return null;
		}
	}

	/** Get the correct balance for an account
	 * @param account The account to get the balance of
	 * @return The balance of the account & its transactions
	*/
	public Map<String, Object> getBalanceForAccount(Account account) {
		ArrayList<Transaction> transactions = getTransactionForAccount(account.getID());

		for(Transaction transaction : transactions) {

			// Catch transactions that are less than 0
			if(transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
				continue;
			}

			// Payments allow overdraft
			else if(transaction.getType().equals("PAYMENT")) {
				account.withdraw(transaction.getAmount(), true);
				transaction.setProcessed(true);
			}

			// Withdrawals do not allow overdraft
			else if(transaction.getType().equals("WITHDRAWAL")) {
				if(account.getBalance().compareTo(transaction.getAmount()) >= 0) {
					account.withdraw(transaction.getAmount(), false);
					transaction.setProcessed(true);
				}
			}

			else if(transaction.getType().equals("DEPOSIT")) {
				account.deposit(transaction.getAmount());
				transaction.setProcessed(true);
			}

			// Transfers between accounts (Does not allow overdraft)
			else if(transaction.getType().equals("TRANSFER")) {

				// We're sending money out
				if(transaction.getSender().equals(account.getName())) {
					if(account.getBalance().compareTo(transaction.getAmount()) >= 0) {
						account.withdraw(transaction.getAmount(), false);
						transaction.setProcessed(true);
					}
				}

				// We're getting money in
				else {
					account.deposit(transaction.getAmount());
					transaction.setProcessed(true);
				}
			}

			// Unknown Transaction Type
			else {
				System.out.println(transaction);
			}
		}

		Map<String, Object> out = new HashMap<>();
		out.put("transactions", transactions);
		out.put("account", account);

		return out;
	}

	/** Take the response JSON from the API, create a new Transaction from each element in the JSON array & return an ArrayList of created Transactions
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
					element.get("sender").isJsonNull() ? null : element.get("sender").getAsString(),
					element.get("id").getAsString(),
					element.get("recipient").isJsonNull() ? null : element.get("recipient").getAsString(),
					element.get("type").getAsString()
			));
		}

		return transactions;
	}
}