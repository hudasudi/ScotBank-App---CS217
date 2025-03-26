package uk.co.asepstrath.bank.view;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.annotation.GET;

import io.jooby.annotation.Path;

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
					session.put("page_error", "Error 404 - Account Not Found");
					session.put("page_msg", "The account you're looking for was not found");

					ctx.sendRedirect("/error");
					return null;
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
			session.put("page_error", "Error whilst finding your page");
			session.put("page_msg", "Sorry! something unexpected happened when looking for your page");

			ctx.sendRedirect("/error");
			return null;
		}
	}

	/** Get & populate the handlebars template with information for a single account's transactions
	 * @param ctx The current context
	 * @return The model to build & deploy
	 */
	@GET("/transactions")
	@SuppressWarnings("unchecked")
	public ModelAndView<Map<String, Object>> getAccountTransactionDetails(Context ctx) {
		Session session = ctx.session();

		try {
			if(session.get("logged_in").booleanValue()) {
				String uuid = session.get("uuid").toString();

				Account account = this.account_manipulator.getAccountByUUID(uuid);

				if(account == null) {
					session.put("page_error", "Error 404 - Account Not Found");
					session.put("page_msg", "Account with UUID '" + uuid + "' not found");

					ctx.sendRedirect("/error");
					return null;
				}

				Map<String, Object> transaction_mappings = this.transaction_manipulator.getBalanceForAccount(account);


				account = (Account) transaction_mappings.get("account");

				ArrayList<Transaction> transactions = (ArrayList<Transaction>) transaction_mappings.get("transactions");

				List<Map<String, Object>> transaction_list = new ArrayList<>();

				BigDecimal income = BigDecimal.valueOf(0);
				BigDecimal outgoings = BigDecimal.valueOf(0);

				for(Transaction transaction : transactions.reversed()) {
					Map<String, Object> transaction_map = new HashMap<>();

					transaction_map.put("timestamp_date", transaction.getTimestamp().split(" ")[0]);
					transaction_map.put("timestamp_time", transaction.getTimestamp().split(" ")[1]);

					Business business = this.business_manipulator.getBusiness(transaction.getRecipient());

					if(business == null) {
						if(transaction.getType().equals("WITHDRAWAL")) transaction_map.put("category", "Withdrawal");
						else if(transaction.getType().equals("DEPOSIT")) transaction_map.put("category", "Deposit");
						else transaction_map.put("category", "Personal Payment");
					}

					else {
						transaction_map.put("category", business.getCategory());
					}

					switch(transaction.getType()) {
						case "PAYMENT" -> {
							if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
								transaction.setProcessed(true);

								outgoings = outgoings.add(transaction.getAmount());
							}

							transaction_map.put("amount", "-" + transaction.getAmount().toString());
						}

						case "WITHDRAWAL" -> {
							if(account.getBalance().compareTo(transaction.getAmount()) > 0) {
								transaction.setProcessed(true);

								outgoings = outgoings.add(transaction.getAmount());
							}

							transaction_map.put("amount", "-" + transaction.getAmount().toString());
						}

						case "DEPOSIT" -> {
							if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
								transaction.setProcessed(true);

								income = income.add(transaction.getAmount());
							}

							transaction_map.put("amount", "+" + transaction.getAmount().toString());
						}

						case "TRANSFER" -> {
							// We're sending money out
							if(transaction.getSender().equals(uuid)) {
								if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
									if(account.getBalance().compareTo(transaction.getAmount()) > 0) {
										transaction.setProcessed(true);

										outgoings = outgoings.add(transaction.getAmount());
									}
								}

								transaction_map.put("amount", "-" + transaction.getAmount().toString());
							}

							// We're getting money in
							else {
								if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
									transaction.setProcessed(true);

									income = income.add(transaction.getAmount());
								}

								transaction_map.put("amount", "+" + transaction.getAmount().toString());
							}
						}

						// We don't know the transaction type
						default -> log.error("Unknown Transaction Type: {}", transaction);
					}

					transaction_map.put("to-from", transaction.getRecipient() == null ? "Withdrawal" : transaction.getRecipient());
					transaction_map.put("current_balance", account.getBalance());
					transaction_map.put("type", transaction.getType().toLowerCase());

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

			session.put("page_error", "Error whilst finding your page");
			session.put("page_msg", "Sorry! something unexpected happened whilst looking for your page");

			ctx.sendRedirect("/error");
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
					session.put("page_error", "Error 404 - Account Not Found");
					session.put("page_msg", "Account with UUID '" + uuid + "' not found");

					ctx.sendRedirect("/error");
					return null;
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

				// Remove all categories with a spending total of 0 (they are irrelevant)
                out.keySet().removeIf(key -> out.get(key).compareTo(BigDecimal.ZERO) == 0);

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
			session.put("page_error", "Error whilst finding your page");
			session.put("page_msg", "Sorry! something unexpected happened when looking for your page");

			ctx.sendRedirect("/error");
			return null;
		}
	}

	/** Get & populate the handlebars template with information for a single account's settings
	 * @param ctx The current context
	 * @return The model to build & deploy
	*/
	@GET("/settings")
	public ModelAndView<Map<String, Object>> getAccountSettings(Context ctx) {
		Session session = ctx.session();

		try {
			if(session.get("logged_in").booleanValue()) {
				return new ModelAndView<>("account/settings.hbs", new HashMap<>());
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
			session.put("login_redirect", 1);
			ctx.sendRedirect("/");
			return null;
		}
	}
}