package uk.co.asepstrath.bank.view;

import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.Path;
import io.jooby.annotation.QueryParam;

import org.slf4j.Logger;

import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Transaction;

import javax.sql.DataSource;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/admin")
public class AdminController extends Controller {

	/** This class controls the Jooby formatting & deployment of admin pages to the site
	 * @param log The program log
	 * @param ds The DataSource to pull from
	 */
	public AdminController(Logger log, DataSource ds) {
		super(log, ds);
	}

	/** Get all account information for this bank
	 * @return The model to build & deploy
	 */
	@GET("/dashboard")
	public ModelAndView<Map<String, Object>> getDashboard() {
		ArrayList<Account> accounts = this.account_manipulator.jsonToAccounts();
		List<Map<String, Object>> account_list = new ArrayList<>();

		int account_count = 0;
		BigDecimal bank_value = BigDecimal.ZERO;

		for(Account account : accounts) {
			// Get actual bank value
			Map<String, Object> actual_value = this.transaction_manipulator.getBalanceForAccount(account);
			account = (Account) actual_value.get("account");

			bank_value = bank_value.add(account.getBalance());

			Map<String, Object> account_map = this.account_manipulator.createAccountMap(account);

			account_list.add(account_map);
			account_count++;
		}

		Map<String, Object> model = new HashMap<>();
		model.put("accounts", account_list);
		model.put("holdings", bank_value);
		model.put("users", account_count);

		return new ModelAndView<>("admin/admin_dashboard.hbs", model);
	}

	/** Get an accounts details for the admin
	 * @param uuid The accounts UUID
	 * @return The model to build & deploy
	*/
	@GET("/account")
	@SuppressWarnings("unchecked")
	public ModelAndView<Map<String, Object>> getSingleAccount(@QueryParam String uuid) {
		try {
			if(uuid == null) {
				return this.getDashboard();
			}

			Account account = this.account_manipulator.getAccountByUUID(uuid);

			if(account == null) {
				return this.buildErrorPage("Error 404 - Account Not Found", "The account you're looking for was not found");
			}

			// Get Transaction Info
			Map<String, Object> saved_account_info = this.transaction_manipulator.getBalanceForAccount(account);

			ArrayList<Transaction> transactions = (ArrayList<Transaction>) saved_account_info.get("transactions");
			account = (Account) saved_account_info.get("account");

			List<Map<String, Object>> in_transactions = new ArrayList<>();
			List<Map<String, Object>> out_transactions = new ArrayList<>();

			// Backwards so transactions are most recent
			for(int i = transactions.size(); i > 0; i--) {
				Transaction transaction = transactions.get(i-1);

				if(transaction.getType().equals("PAYMENT") || transaction.getType().equals("WITHDRAWAL")) {
					out_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
				}

				else if(transaction.getType().equals("DEPOSIT")) {
					in_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
				}

				else if(transaction.getType().equals("TRANSFER")) {
					if(transaction.getSender().equals(account.getName())) {
						out_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
					}

					else {
						in_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
					}
				}

				else {
					continue;
				}
			}

			Map<String, Object> model = new HashMap<>();

			model.put("account", this.account_manipulator.createAccountMap(account));
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