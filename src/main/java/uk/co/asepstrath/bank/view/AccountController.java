package uk.co.asepstrath.bank.view;

import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.Path;
import io.jooby.annotation.QueryParam;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.api.AccountAPIManipulator;

import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;

import javax.sql.DataSource;

@Path("/accounts")
public class AccountController {
	private final AccountAPIManipulator manip;
	private final Logger log;
	private final DataSource ds;

	/** This class controls the Jooby formatting & deployment of pages to the site
	 * @param log The program log
	 */
	public AccountController(Logger log, DataSource ds) {
		this.log = log;
		this.manip = new AccountAPIManipulator(log, ds);
		this.ds = ds;
	}

	/** Get & populate the handlebars template with information from the API file
	 * @return The model to build & deploy
	*/
	@GET("/account-view")
	public ModelAndView getAccounts() {
		Map<String, Object> model = this.manip.createHandleBarsJSONMap();

		return new ModelAndView("accounts.hbs", model);
	}

	/** Get an array of Account from the JSON information in the API file & return their information in String form
	 * @return The JSON information as a String
	*/
	@GET("/account-objects")
	public String accountsObjects() {
		ArrayList<Account> array = this.manip.jsonToAccounts();

		StringBuilder out = new StringBuilder();

		for(Account a : array) {
			out.append(a.toString()).append("\n\n");
		}

		return out.toString();
	}

	/** Get specific account information from a user query in the URL
	 * @param pos The Account JSON to get
 	 * @return The Account JSON information as a String
	 */
	@GET("/account-object")
	public String accountsObject(@QueryParam int pos) {
		ArrayList<Account> array = this.manip.jsonToAccounts();

		return array.get(pos).toString();
	}
}