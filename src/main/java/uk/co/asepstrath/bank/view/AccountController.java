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

@Path("/accounts")
public class AccountController {
	private final AccountAPIManipulator manip;
	private final Logger log;

	public AccountController(Logger log) {
		this.log = log;
		this.manip = new AccountAPIManipulator("src/main/resources/api/api.json");
	}

	@GET("/account-view")
	public ModelAndView getAccounts() {
		Map<String, Object> model = manip.manip_ls();

		return new ModelAndView("accounts.hbs", model);
	}

	@GET("/account-objects")
	public String accountsObjects() {
		ArrayList<Account> array = manip.jsonToAccounts();

		StringBuilder out = new StringBuilder();

		for(Account a : array) {
			out.append(a.toString()).append("\n\n");
		}

		return out.toString();
	}

	@GET("/account-object")
	public String accountsObject(@QueryParam int pos) {
		ArrayList<Account> array = manip.jsonToAccounts();

		return array.get(pos).toString();
	}
}