package uk.co.asepstrath.bank.view;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.Path;

import io.jooby.exception.MissingValueException;
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
	public ModelAndView<Map<String, Object>> getDashboard(Context ctx) {
		Session session = ctx.session();

		try {
			// If we're logged in & an admin
			if(session.get("logged_in").booleanValue() && session.get("is_admin").booleanValue()) {
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

				return new ModelAndView<>("admin/dashboard.hbs", model);
			}

			// We're either not logged in or can't access this page
			else {
				// We're a normal user
				if(session.get("logged_in").booleanValue()) {
					ctx.sendRedirect("/account/dashboard");
					return null;
				}

				// You've tried to access a page you can't
				else {
					session.put("login_redirect", 4);
					ctx.sendRedirect("/");
					return null;
				}
			}
		}

		catch(MissingValueException e) {
			ctx.sendRedirect("/");
			return null;
		}

		catch(Exception e) {
			session.put("page_error", "Error whilst finding your page");
			session.put("page_msg", "Sorry! something unexpected happened when looking for your page");

			ctx.sendRedirect("/error");
			return null;
		}
	}

	/** Get an accounts details for the admin
	 * @param ctx The current context
	 * @return The model to build & deploy
	*/
	@GET("/account")
	@SuppressWarnings("unchecked")
	public ModelAndView<Map<String, Object>> getSingleAccount(Context ctx) {
		Session session = ctx.session();

		try {
			// Logged in & is an admin
			if(session.get("logged_in").booleanValue() && session.get("is_admin").booleanValue()) {
				String uuid = session.get("user_uuid").toString();

				Account account = this.account_manipulator.getAccountByUUID(uuid);

				if(account == null) {
					session.put("page_error", "Error 404 - Account Not Found");
					session.put("page_msg", "The account you're looking for was not found!");

					ctx.sendRedirect("/error");
					return null;
				}

				// Get Transaction Info
				Map<String, Object> saved_account_info = this.transaction_manipulator.getBalanceForAccount(account);

				ArrayList<Transaction> transactions = (ArrayList<Transaction>) saved_account_info.get("transactions");

				List<Map<String, Object>> transaction_list = new ArrayList<>();

				// Backwards so transactions are most recent
				for(int i = transactions.size(); i > 0; i--) {
					Transaction transaction = transactions.get(i-1);
					Map<String, Object> transaction_map = new HashMap<>();

					transaction_map.put("timestamp_date", transaction.getTimestamp().split(" ")[0]);
					transaction_map.put("timestamp_time", transaction.getTimestamp().split(" ")[1]);
					transaction_map.put("amount", transaction.getAmount());

					switch(transaction.getType()) {
						case "DEPOSIT" -> transaction_map.put("to-from", "Deposit");
						case "WITHDRAWAL" -> transaction_map.put("to-from", "Withdrawal");
						case "TRANSFER" -> {
							if(transaction.getSender() == null) {
								transaction_map.put("to-from", transaction.getRecipient());
							}

							else {
								transaction_map.put("to-from", transaction.getSender());
							}
						}

						default -> transaction_map.put("to-from", transaction.getRecipient());
					}

					transaction_list.add(transaction_map);
				}

				Map<String, Object> model = new HashMap<>();

				model.put("account", this.account_manipulator.createAccountMap(account));
				model.put("transactions", transaction_list);

				return new ModelAndView<>("admin/account.hbs", model);
			}

			// Either not logged in or not an admin
			else {
				session.put("login_redirect", 4);

				ctx.sendRedirect("/");
				return null;
			}
		}

		catch(Exception e) {
			session.put("page_error", "Error whilst finding your page");
			session.put("page_msg", "Sorry! something unexpected happened when looking for your page");

			ctx.sendRedirect("/error");
			return null;
		}
	}

	/** Find a specific user with their uuid
	 * @param ctx The current context
	 * @param uuid The uuid of the account to search for
	*/
	@POST("/find/users")
	public void findAccount(Context ctx, String uuid) {
		Session session = ctx.session();

		try {
			// Check for logged in & admin
			if(session.get("logged_in").booleanValue() && session.get("is_admin").booleanValue()) {
				// UUID never passed
				if(uuid == null) {
					ctx.sendRedirect("/admin/dashboard");
				}

				else {
					Account account = this.account_manipulator.getAccountByUUID(uuid);

					// Account does not exist
					if(account == null) {
						ctx.sendRedirect("/account/dashboard");
					}

					else {
						session.put("user_uuid", uuid);

						ctx.sendRedirect("/admin/account");
					}
				}
			}

			// Either not logged in or not an admin
			else {
				// User logged in, so not admin
				if(session.get("logged_in").booleanValue()) {
					ctx.sendRedirect("/account/dashboard");
				}

				// Neither is true
				else {
					session.put("login_redirect", 4);
					ctx.sendRedirect("/");
				}
			}
		}

		catch(Exception e) {
			session.put("page_error", "Error whilst finding your page");
			session.put("page_msg", "Sorry! something unexpected happened when looking for your page");

			ctx.sendRedirect("/error");
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
				return new ModelAndView<>("admin/settings.hbs", new HashMap<>());
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