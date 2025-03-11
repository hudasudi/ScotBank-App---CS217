package uk.co.asepstrath.bank.view;

import com.google.gson.JsonElement;
import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jooby.annotation.Path;
import io.jooby.annotation.QueryParam;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Business;
import uk.co.asepstrath.bank.Transaction;
import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;

import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;

import javax.sql.DataSource;

@Path("/accounts")
public class AccountController {
	private AccountAPIManipulator account_manipulator;
	private BusinessAPIManipulator business_manipulator;
	private TransactionAPIManipulator transaction_manipulator;
	private final Logger log;
	private final DataSource ds;

	/** This class controls the Jooby formatting & deployment of account pages to the site
	 * @param log The program log
	 * @param ds The DataSource to pull from
	 */
	public AccountController(Logger log, DataSource ds) {
		this.log = log;
		this.account_manipulator = new AccountAPIManipulator(log, ds);
		this.business_manipulator = new BusinessAPIManipulator(log, ds);
		this.transaction_manipulator = new TransactionAPIManipulator(log, ds);
		this.ds = ds;
	}

	// FOR TESTING
	public void setAccountAPIManipulator(AccountAPIManipulator manip) {
		this.account_manipulator = manip;
	}

	private ModelAndView<Map<String, Object>> buildErrorPage(String error, String msg) {
		Map<String, Object> map = new HashMap<>();

		map.put("error", error);
		map.put("msg", msg);

		return new ModelAndView<>("error.hbs", map);
	}

	/** Get & populate the handlebars template with information for a single account from the API
	 * @param uuid The Account's UUID
	 * @return The model to build & deploy
	*/
	@GET("/dashboard")
	public ModelAndView<Map<String, Object>> getAccount(@QueryParam String uuid) {
		try {
			if(uuid == null) {
				return null;
			}

			JsonArray result = this.account_manipulator.getApiInformation();
			JsonObject account = null;

			for(JsonElement acc : result) {
				JsonObject acc_obj = acc.getAsJsonObject();

				if(uuid.equals(acc_obj.get("id").getAsString())) {
					account = acc_obj;
				}
			}

			if(account == null) {
				return this.buildErrorPage("Error - Account not Found", "The account you're looking for was not found");
			}

			String acc_name = account.get("name").getAsString();

			// Get Transaction Info
			JsonArray transactions = this.transaction_manipulator.getTransactionForAccount(uuid);

			// Calculate actual balance
			BigDecimal account_balance = account.get("startingBalance").getAsBigDecimal();

			List<Map<String, String>> in_transactions = new ArrayList<>();
			List<Map<String, String>> out_transactions = new ArrayList<>();

			int in_count = 0; // If count = 5, we stop putting transaction data into list
			int out_count = 0;

			for(int i = 0; i < transactions.size(); i++) {
				JsonObject transaction = transactions.get(i).getAsJsonObject();

				Map<String, String> transaction_map = this.transaction_manipulator.createJsonMap(transaction);

				if(transaction.get("type").getAsString().equals("PAYMENT") || transaction.get("type").getAsString().equals("WITHDRAWAL")) {
					if(account_balance.compareTo(transaction.get("amount").getAsBigDecimal()) >= 0 || transaction.get("type").getAsString().equals("PAYMENT")) {
						account_balance = account_balance.subtract(transaction.get("amount").getAsBigDecimal());
						transaction_map.put("processed", "ACCEPTED");
						if(out_count <= 5) out_transactions.add(transaction_map);
						out_count++;
					}

					else {
						transaction_map.put("processed", "DECLINED");
						if(out_count <= 5) out_transactions.add(transaction_map);
						out_count++;
					}
				}

				else if(transaction.get("type").getAsString().equals("TRANSFER")) {
					// We're sending money out
					if(transaction.get("sender").getAsString().equals(acc_name)) {
						account_balance = account_balance.subtract(transaction.get("amount").getAsBigDecimal());
						transaction_map.put("processed", "ACCEPTED");
						if(out_count <= 5) out_transactions.add(transaction_map);
						out_count++;
					}

					// We're receiving money in
					else {
						account_balance = account_balance.add(transaction.get("amount").getAsBigDecimal());
						if(in_count <= 5) in_transactions.add(this.transaction_manipulator.createJsonMap(transaction));
						in_count++;
					}
				}

				else if (transaction.get("type").getAsString().equals("DEPOSIT")) {
					account_balance = account_balance.add(transaction.get("amount").getAsBigDecimal());
					transaction_map.put("sender", "Deposit");
					if(in_count <= 5) in_transactions.add(transaction_map);
					in_count++;
				}

				// We don't know what type of transaction
				else {
					System.out.println(transaction);
				}
			}

			account.addProperty("startingBalance", account_balance);

			Map<String, Object> model = new HashMap<>();
			model.put("account", this.account_manipulator.createJsonMap(account));
			model.put("income", in_transactions);
			model.put("outgoings", out_transactions);

			return new ModelAndView<>("account/dashboard.hbs", model);
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);
		}

		return null;
	}

	@GET("/details")
	public ModelAndView<Map<String, Object>> getAccountDetails(@QueryParam String uuid) {
		try {
			if (uuid == null) {
				return null;
			}

			return this.buildErrorPage("PAGE UNDER CONSTRUCTION", "The Frontend design for this page is not yet complete");
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);
		}

		return null;
	}

	@GET("/summary")
	public ModelAndView<Map<String, Object>> getAccountSummary(@QueryParam String uuid) {
		try {
			if (uuid == null) {
				return null;
			}

			HashMap<String, BigDecimal> out = new HashMap<>();

			for(Business b : business_manipulator.jsonToBusinesses()) {
				out.put(b.getCategory(), BigDecimal.ZERO);
			}

			ArrayList<Business> businesses = this.business_manipulator.jsonToBusinesses();
			ArrayList<Transaction> transactions = this.transaction_manipulator.jsonToTransactions();

			for(Transaction t : transactions) {
				if(t.getSender().equals(uuid)) {
					for(Business b : businesses) {
						if(out.get(b.getCategory()) != null) {
							out.put(b.getCategory(), out.get(b.getCategory()).add(t.getAmount()));
						}
					}
				}
			}

			JsonObject account_obj = null;

			JsonArray arr = this.account_manipulator.getApiInformation();

			for(JsonElement a : arr) {
				JsonObject obj = a.getAsJsonObject();

				if(obj.get("id").getAsString().equals(uuid)) {
					account_obj = obj;
					break;
				}
			}

			if(account_obj == null) {
				return this.buildErrorPage("Error - 404 Account Not Found", "Account with UUID " + uuid + " not found");
			}

			List<Map.Entry<String, BigDecimal>> category_list = new ArrayList<>(out.entrySet());

			Map<String, Object> model = new HashMap<>();

			model.put("account", this.account_manipulator.createJsonMap(account_obj));
			model.put("categories", category_list);

			return new ModelAndView<>("account/summary.hbs", model);
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);
		}

		return null;
	}

	@GET("/transactions")
	public ModelAndView<Map<String, Object>> getAccountTransactionDetails(@QueryParam String uuid) {
		try {
			if (uuid == null) {
				return null;
			}

			JsonArray accounts = this.account_manipulator.getApiInformation();
			JsonObject account = null;

			for(JsonElement e : accounts) {
				JsonObject obj = e.getAsJsonObject();

				if(obj.get("id").getAsString().equals(uuid)) {
					account = obj;
					break;
				}
			}

			if(account == null) {
				return this.buildErrorPage("Error - 404 Account Not Found", "Account with UUID " + uuid + " not found");
			}

			JsonArray transactions = this.transaction_manipulator.getTransactionForAccount(uuid);

			List<Map<String, Object>> transaction_list = new ArrayList<>();

			BigDecimal income = BigDecimal.valueOf(0);
			BigDecimal outgoings = BigDecimal.valueOf(0);
			BigDecimal balance = account.get("startingBalance").getAsBigDecimal();

			for(JsonElement e : transactions) {
				JsonObject obj = e.getAsJsonObject();
				Map<String, Object> obj_map = new HashMap<>();

				obj_map.put("timestamp", obj.get("timestamp").getAsString());

				if(obj.get("type").getAsString().equals("WITHDRAWAL") || obj.get("type").getAsString().equals("PAYMENT")) {
					balance = balance.subtract(obj.get("amount").getAsBigDecimal());

					obj_map.put("amount", "-"+obj.get("amount").getAsString());
					obj_map.put("to-from", obj.get("recipient").getAsString());
					obj_map.put("current_balance", balance);

					transaction_list.add(obj_map);
					outgoings = outgoings.add(obj.get("amount").getAsBigDecimal());
				}

				else if(obj.get("type").getAsString().equals("DEPOSIT")) {
					balance = balance.add(obj.get("amount").getAsBigDecimal());

					obj_map.put("amount", "+"+obj.get("amount").getAsString());
					obj_map.put("to-from", "Deposit");
					obj_map.put("current_balance", balance);

					transaction_list.add(obj_map);
					income = income.add(obj.get("amount").getAsBigDecimal());
				}

				else if(obj.get("type").getAsString().equals("TRANSFER")) {
					if(obj.get("sender").getAsString().equals(uuid)) {
						balance = balance.subtract(obj.get("amount").getAsBigDecimal());

						obj_map.put("amount", "-"+obj.get("amount").getAsString());
						obj_map.put("to-from", obj.get("recipient").getAsString());
						obj_map.put("current_balance", balance);

						outgoings = outgoings.add(obj.get("amount").getAsBigDecimal());
					}

					else {
						balance = balance.add(obj.get("amount").getAsBigDecimal());

						obj_map.put("amount", "+"+obj.get("amount").getAsString());
						obj_map.put("to-from", obj.get("sender").getAsString());
						obj_map.put("current_balance", balance);

						income = income.add(obj.get("amount").getAsBigDecimal());
					}
				}

				else {
					continue;
				}
			}

			Map<String, Object> model = new HashMap<>();
			model.put("transactions", transaction_list);
			model.put("balance", balance);
			model.put("income", income);
			model.put("outgoings", outgoings);
			model.put("account", this.account_manipulator.createJsonMap(account));

			return new ModelAndView<>("account/transactions.hbs", model);
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);
		}

		return null;
	}

	/** Get an array of Account from the JSON information in the Database & return their information in String form
	 * @return The JSON information as a String
	 * @deprecated
	*/
	@Deprecated
	@GET("/account-objects")
	public String accountsObjects() {
		ArrayList<Account> array = this.account_manipulator.jsonToAccounts();

		StringBuilder out = new StringBuilder();

		for(Account a : array) {
			out.append(a.toString()).append("\n\n");
		}

		return out.toString();
	}

	/** Get specific account information from a user query in the URL
	 * @param pos The Account JSON to get
 	 * @return The Account JSON information as a String
	 * @deprecated Use getAccount instead
	 */
	@Deprecated
	@GET("/account-object")
	public String accountsObject(@QueryParam int pos) {
		ArrayList<Account> array = this.account_manipulator.jsonToAccounts();

		return array.get(pos).toString();
	}
}