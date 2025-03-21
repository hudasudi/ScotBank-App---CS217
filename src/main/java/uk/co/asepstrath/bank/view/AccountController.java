package uk.co.asepstrath.bank.view;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.annotation.GET;

import io.jooby.annotation.Path;
import io.jooby.annotation.QueryParam;

import io.jooby.exception.MissingValueException;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Business;
import uk.co.asepstrath.bank.Transaction;

import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;

import javax.sql.DataSource;

@Path("/account")
public class AccountController extends Controller {

	/** This class controls the Jooby formatting & deployment of account pages to the site
	 * @param log The program log
	 * @param ds The DataSource to pull from
	 */
	public AccountController(Logger log, DataSource ds) {
		super(log, ds);
	}

	/** Get & populate the handlebars template with information for a single account from the API
	 * @param ctx The current context
	 * @return The model to build & deploy
	*/
	@GET("/dashboard")
	@SuppressWarnings("unchecked")
	public ModelAndView<Map<String, Object>> getAccount(Context ctx) {
		Session session = ctx.session();

		try {
			if(session.get("logged_in").booleanValue()) {
				String uuid = session.get("uuid").toString();

				Account account = this.account_manipulator.getAccountByUUID(uuid);

				if(account == null) {
					return this.buildErrorPage("Error 404 - Account Not Found", "The account you're looking for was not found");
				}

				Map<String, Object> saved_account_info = this.transaction_manipulator.getBalanceForAccount(account);
				account = (Account) saved_account_info.get("account");

				ArrayList<Transaction> transactions = (ArrayList<Transaction>) saved_account_info.get("transactions");

				List<Map<String, Object>> in_transactions = new ArrayList<>();
				List<Map<String, Object>> out_transactions = new ArrayList<>();

				int in_count = 0;
				int out_count = 0;

				// Backwards so transactions are most recent
				for(int i = transactions.size(); i > 0; i--) {
					Transaction transaction = transactions.get(i-1);

					switch (transaction.getType()) {
						case "PAYMENT", "WITHDRAWAL" -> {
							if(out_count < 5) {
								out_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
								out_count++;
							}
						}

						case "DEPOSIT" -> {
							if(in_count < 5) {
								in_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
								in_count++;
							}
						}

						case "TRANSFER" -> {
							if (transaction.getSender().equals(account.getName())) {
								if(out_count < 5) {
									out_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
									out_count++;
								}
							} else {
								if(in_count < 5) {
									in_transactions.add(this.transaction_manipulator.createTransactionMap(transaction));
									in_count++;
								}
							}
						}
					}

					if(in_count >= 5 && out_count >= 5) {
						break;
					}
				}

				Map<String, Object> model = new HashMap<>();
				model.put("account", this.account_manipulator.createAccountMap(account));
				model.put("income", in_transactions);
				model.put("outgoings", out_transactions);

				return new ModelAndView<>("account/dashboard.hbs", model);
			}

			else {
				throw new MissingValueException("Not logged in");
			}
		}

		// Tried to access page without logging in
		catch(MissingValueException e) {
			session.put("login_redirect", 2);
			session.put("logged_in", false);

			ctx.sendRedirect("/");
			return null;
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);
			return this.buildErrorPage("Error whilst finding your page", "Sorry! something unexpected happened when looking for your page");
		}
	}

	/** Get & populate the handlebars template with information for a single account's details
	 * @param ctx The current context
	 * @return The model to build & deploy
	*/
	@GET("/details")
	public ModelAndView<Map<String, Object>> getAccountDetails(Context ctx) {
		Session session = ctx.session();

		try {
			if(session.get("logged_in").booleanValue()) {
				String uuid = session.get("uuid").toString();

				return this.buildErrorPage("PAGE UNDER CONSTRUCTION", "The Frontend design for this page is not yet complete");
			}

			else {
				throw new MissingValueException("Not logged in");
			}

		}

		catch(MissingValueException e) {
			session.put("login_redirect", 2);
			session.put("logged_in", false);

			ctx.sendRedirect("/");
			return null;
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);
			return null;
		}
	}

	/** Get & populate the handlebars template with information for a single account's summary
	 * @param ctx The current context
	 * @return The model to build & deploy
	*/
	@GET("/summary")
	public ModelAndView<Map<String, Object>> getAccountSummary(Context ctx) {
		Session session = ctx.session();

		try {
			if(session.get("logged_in").booleanValue()) {
				String uuid = session.get("uuid").toString();

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

				Iterator<String> out_iterator = out.keySet().iterator();

				while(out_iterator.hasNext()) {
					String key = out_iterator.next();

					if(out.get(key).compareTo(BigDecimal.ZERO) == 0) {
						out_iterator.remove();
					}
				}

				List<Map.Entry<String, BigDecimal>> category_list = new ArrayList<>(out.entrySet());

				Map<String, Object> model = new HashMap<>();

				model.put("account", this.account_manipulator.createAccountMap(account));
				model.put("categories", category_list);

				return new ModelAndView<>("account/summary.hbs", model);
			}

			else {
				throw new MissingValueException("Not logged in");
			}
		}

		catch(MissingValueException e) {
			session.put("login_redirect", 2);
			session.put("logged_in", false);

			ctx.sendRedirect("/");
			return null;
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);
			return this.buildErrorPage("Error whilst finding your page", "Sorry! something unexpected happened when looking for your page");
		}
	}

	/** Get & populate the handlebars template with information for a single account's transactions
	 * @param ctx The current context
	 * @return The model to build & deploy
	*/
	@GET("/transactions")
	public ModelAndView<Map<String, Object>> getAccountTransactionDetails(Context ctx) {
		Session session = ctx.session();

		try {
			if(session.get("logged_in").booleanValue()) {
				String uuid = session.get("uuid").toString();

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

			else {
				throw new MissingValueException("Not logged in");
			}
		}

		catch(MissingValueException e) {
			session.put("login_redirect", 2);
			session.put("logged_in", false);

			ctx.sendRedirect("/");
			return null;
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);
			return this.buildErrorPage("Error whilst finding your page", "Sorry! something unexpected happened when looking for your page");
		}
	}
}