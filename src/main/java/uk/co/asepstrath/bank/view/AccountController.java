package uk.co.asepstrath.bank.view;

import io.jooby.ModelAndView;
import io.jooby.annotation.GET;

import io.jooby.annotation.Path;
import io.jooby.annotation.QueryParam;

import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Business;
import uk.co.asepstrath.bank.Transaction;

import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;

import javax.sql.DataSource;

@Path("/accounts")
public class AccountController extends Controller {

	/** This class controls the Jooby formatting & deployment of account pages to the site
	 * @param log The program log
	 * @param ds The DataSource to pull from
	 */
	public AccountController(Logger log, DataSource ds) {
		super(log, ds);
	}

	/** Get & populate the handlebars template with information for a single account from the API
	 * @param uuid The Account's UUID
	 * @return The model to build & deploy
	*/
	@GET("/dashboard")
	public ModelAndView<Map<String, Object>> getAccount(@QueryParam String uuid) {
		try {
			if(uuid == null) {
				return this.buildErrorPage("Error 400 - Bad Request", "No UUID provided!");
			}

			Account account = this.account_manipulator.getAccountByUUID(uuid);

			if(account == null) {
				return this.buildErrorPage("Error 404 - Account Not Found", "The account you're looking for was not found");
			}

			// Get Transaction Info
			ArrayList<Transaction> transactions = this.transaction_manipulator.getTransactionForAccount(uuid);

			List<Map<String, Object>> in_transactions = new ArrayList<>();
			List<Map<String, Object>> out_transactions = new ArrayList<>();

			// If count = 5, we stop putting transaction data into lists
			int in_count = 0;
			int out_count = 0;

			for(Transaction transaction : transactions) {

				// Payments allow overdraft
				if(transaction.getType().equals("PAYMENT")) {
					if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
						account.withdraw(transaction.getAmount(), true);
						transaction.setProcessed(true);
					}

					if(out_count <= 5) out_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
					out_count++;
				}

				// Withdrawals do not allow overdraft
				else if(transaction.getType().equals("WITHDRAWAL")) {
					if(account.getBalance().compareTo(transaction.getAmount()) >= 0) {
						if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
							account.withdraw(transaction.getAmount(), false);
							transaction.setProcessed(true);
						}
					}

					if(out_count <= 5) out_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
					out_count++;
				}

				else if(transaction.getType().equals("DEPOSIT")) {
					if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
						account.deposit(transaction.getAmount());
						transaction.setProcessed(true);
					}

					if(in_count <= 5) in_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
					in_count++;
				}

				// Transfers between accounts (Does not allow overdraft)
				else if(transaction.getType().equals("TRANSFER")) {

					// We're sending money out
					if(transaction.getSender().equals(account.getName())) {
						if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
							account.withdraw(transaction.getAmount(), false);
							transaction.setProcessed(true);
						}

						if(out_count <= 5) out_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
						out_count++;
					}

					// We're getting money in
					else {
						if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
							account.deposit(transaction.getAmount());
							transaction.setProcessed(true);
						}

						if(in_count <= 5) in_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
						in_count++;
					}
				}

				// Unknown Transaction Type
				else {
					System.out.println(transaction);
				}
			}

			Map<String, Object> model = new HashMap<>();
			model.put("account", this.account_manipulator.createAccountMap(account));
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
			if(uuid == null) {
				return this.buildErrorPage("Error 400 - Bad Request", "No UUID provided!");
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
			if(uuid == null) {
				return this.buildErrorPage("Error 400 - Bad Request", "No UUID provided!");
			}

			Account account = this.account_manipulator.getAccountByUUID(uuid);

			if(account == null) {
				return this.buildErrorPage("Error 404 - Account Not Found", "Account with UUID " + uuid + " not found");
			}

			HashMap<String, BigDecimal> out = new HashMap<>();

			for(Business b : business_manipulator.jsonToBusinesses()) {
				out.put(b.getCategory(), BigDecimal.ZERO);
			}

			ArrayList<Business> businesses = this.business_manipulator.jsonToBusinesses();
			ArrayList<Transaction> transactions = this.transaction_manipulator.jsonToTransactions();

			for(Transaction t : transactions) {

				// Only look at payments
				if(t.getType().equals("PAYMENT")) {

					// If payment sender is our account
					if(t.getSender().equals(uuid)) {

						for(Business b : businesses) {
							// Check if a business is a recipient
							if(t.getRecipient().equals(b.getId())) {
								// Add the amount to the total for the category & break
								out.put(b.getCategory(), out.get(b.getCategory()).add(t.getAmount()));
								break;
							}
						}
					}
				}
			}

			List<Map.Entry<String, BigDecimal>> category_list = new ArrayList<>(out.entrySet());

			Map<String, Object> model = new HashMap<>();

			model.put("account", this.account_manipulator.createAccountMap(account));
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
			if(uuid == null) {
				return this.buildErrorPage("Error 400 - Bad Request", "No UUID provided!");
			}

			Account account = this.account_manipulator.getAccountByUUID(uuid);

			if(account == null) {
				return this.buildErrorPage("Error 404 - Account Not Found", "Account with UUID " + uuid + " not found");
			}

			ArrayList<Transaction> transactions = this.transaction_manipulator.getTransactionForAccount(uuid);

			List<Map<String, Object>> transaction_list = new ArrayList<>();

			BigDecimal income = BigDecimal.valueOf(0);
			BigDecimal outgoings = BigDecimal.valueOf(0);

			for(Transaction transaction : transactions) {
				Map<String, Object> transaction_map = new HashMap<>();

				transaction_map.put("timestamp", transaction.getTimestamp());

				if(transaction.getType().equals("PAYMENT")) {
					if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
						account.withdraw(transaction.getAmount(), true);

						transaction.setProcessed(true);

						outgoings = outgoings.add(transaction.getAmount());
					}

					transaction_map.put("amount", "-" + transaction.getAmount().toString());
				}

				else if(transaction.getType().equals("WITHDRAWAL")) {
					if(account.getBalance().compareTo(transaction.getAmount()) > 0) {
						account.withdraw(transaction.getAmount(), false);
						transaction.setProcessed(true);

						outgoings = outgoings.add(transaction.getAmount());
					}

					transaction_map.put("amount", "-" + transaction.getAmount().toString());
				}

				else if(transaction.getType().equals("DEPOSIT")) {
					if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
						account.deposit(transaction.getAmount());
						transaction.setProcessed(true);

						income = income.add(transaction.getAmount());
					}

					transaction_map.put("amount", "+" + transaction.getAmount().toString());
				}

				else if(transaction.getType().equals("TRANSFER")) {
					// We're sending money out
					if(transaction.getSender().equals(uuid)) {
						if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
							if(account.getBalance().compareTo(transaction.getAmount()) > 0) {
								account.withdraw(transaction.getAmount(), false);
								transaction.setProcessed(true);

								outgoings = outgoings.add(transaction.getAmount());
							}
						}

						transaction_map.put("amount", "-" + transaction.getAmount().toString());
					}

					// We're getting money in
					else {
						if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
							account.deposit(transaction.getAmount());
							transaction.setProcessed(true);

							income = income.add(transaction.getAmount());
						}

						transaction_map.put("amount", "+" + transaction.getAmount().toString());
					}
				}

				// We don't know the transaction type
				else {
					System.out.println(transaction);
				}

				transaction_map.put("to-from", transaction.getRecipient());
				transaction_map.put("current_balance", account.getBalance());
				transaction_map.put("processed", transaction.isProcessed() ? "PROCESSED" : "DECLINED");

				transaction_list.add(transaction_map);
			}

			Map<String, Object> model = new HashMap<>();

			model.put("transactions", transaction_list);
			model.put("income", income);
			model.put("outgoings", outgoings);
			model.put("account", this.account_manipulator.createAccountMap(account));

			return new ModelAndView<>("account/transactions.hbs", model);
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);
		}

		return null;
	}
}