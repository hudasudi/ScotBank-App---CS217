package uk.co.asepstrath.bank.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.Path;

import io.jooby.annotation.QueryParam;
import org.slf4j.Logger;

import uk.co.asepstrath.bank.Transaction;
import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/admin")
public class AdminController {
	private final Logger log;
	private final DataSource ds;
	private final AccountAPIManipulator account_manipulator;
	private final BusinessAPIManipulator business_manipulator;
	private final TransactionAPIManipulator	transaction_manipulator;

	/** This class controls the Jooby formatting & deployment of admin pages to the site
	 * @param log The program log
	 * @param ds The DataSource to pull from
	*/
	public AdminController(Logger log, DataSource ds) {
		this.log = log;
		this.ds = ds;

		this.account_manipulator = new AccountAPIManipulator(log, ds);
		this.business_manipulator = new BusinessAPIManipulator(log, ds);
		this.transaction_manipulator = new TransactionAPIManipulator(log, ds);
	}

	/** Get & populate the handlebars template with information from the API
	 * @return The model to build & deploy
	*/
	@GET("/dashboard")
	public ModelAndView<Map<String, Object>> getDashboard() {
		JsonArray accounts = this.account_manipulator.getApiInformation();
		List<Map<String, String>> account_list = new ArrayList<>();

		int account_count = 0;
		BigDecimal bank_value = BigDecimal.ZERO;

		for(int i = 0; i < accounts.size(); i++) {
			JsonObject account = accounts.get(i).getAsJsonObject();

			Map<String, String> account_map = this.account_manipulator.createJsonMap(account);
			bank_value = bank_value.add(BigDecimal.valueOf(account.get("startingBalance").getAsDouble()));

			account_list.add(account_map);
			account_count++;
		}

		Map<String, Object> model = new HashMap<>();
		model.put("accounts", account_list);
		model.put("holdings", bank_value);
		model.put("users", account_count);

		return new ModelAndView<>("admin/admin_dashboard.hbs", model);
	}

	private ModelAndView<Map<String, Object>> buildErrorPage(String error, String msg) {
		Map<String, Object> map = new HashMap<>();

		map.put("error", error);
		map.put("msg", msg);

		return new ModelAndView<>("error.hbs", map);
	}

	@GET("/account")
	public ModelAndView<Map<String, Object>> getSingleAccount(@QueryParam String uuid) {
		try {
			if(uuid == null) {
				return this.getDashboard();
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

			for(int i = 0; i < transactions.size(); i++) {
				JsonObject transaction = transactions.get(i).getAsJsonObject();

				Map<String, String> transaction_map = this.transaction_manipulator.createJsonMap(transaction);

				if(transaction.get("type").getAsString().equals("PAYMENT") || transaction.get("type").getAsString().equals("WITHDRAWAL")) {
					if(account_balance.compareTo(transaction.get("amount").getAsBigDecimal()) >= 0 || transaction.get("type").getAsString().equals("PAYMENT")) {
						account_balance = account_balance.subtract(transaction.get("amount").getAsBigDecimal());
						transaction_map.put("processed", "ACCEPTED");
						out_transactions.add(transaction_map);
					}

					else {
						transaction_map.put("processed", "DECLINED");
						out_transactions.add(transaction_map);
					}
				}

				else if(transaction.get("type").getAsString().equals("TRANSFER")) {
					// We're sending money out
					if(transaction.get("sender").getAsString().equals(acc_name)) {
						account_balance = account_balance.subtract(transaction.get("amount").getAsBigDecimal());
						transaction_map.put("processed", "ACCEPTED");
						out_transactions.add(transaction_map);
					}

					// We're receiving money in
					else {
						account_balance = account_balance.add(transaction.get("amount").getAsBigDecimal());
						in_transactions.add(this.transaction_manipulator.createJsonMap(transaction));
					}
				}

				else if (transaction.get("type").getAsString().equals("DEPOSIT")) {
					account_balance = account_balance.add(transaction.get("amount").getAsBigDecimal());
					transaction_map.put("sender", "Deposit");
					in_transactions.add(transaction_map);
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

			return new ModelAndView<>("admin/admin_account.hbs", model);
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);
		}

		return null;
	}
}