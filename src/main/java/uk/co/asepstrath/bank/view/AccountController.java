package uk.co.asepstrath.bank.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

	/** This class controls the Jooby formatting & deployment of pages to the site
	 * @param log The program log
	 */
	public AccountController(Logger log, String api_file) {
		this.log = log;
		this.manip = new AccountAPIManipulator(log, api_file);
	}

	/** Get & populate the handlebars template with information from the API file
	 * @return The model to build & deploy
	*/
	@GET("/accounts-view")
	public ModelAndView<Map<String, Object>> getAccounts() {
		Map<String, Object> model = manip.createHandleBarsJSONMap();

		return new ModelAndView<>("accounts.hbs", model);
	}

	/** Get an array of Account from the JSON information in the API file & return their information in String form
	 * @return The JSON information as a String
	*/
	@GET("/account-objects")
	public String accountsObjects() {
		ArrayList<Account> array = manip.jsonToAccounts();

		StringBuilder out = new StringBuilder();

		for(Account a : array) {
			out.append(a.toString()).append("\n\n");
		}

		return out.toString();
	}

	/** Get specific account information from a user query in the URL
	 * @param uuid The Account UUID
	 * @param is_admin Whether the account is an admin account
	 * @return The model to build & deploy
	 */
	@GET("/account")
	public ModelAndView<Map<String, String>> getAccount(@QueryParam String uuid, @QueryParam boolean is_admin) {
		// NOTE: Might be useful later to change these from @QueryParam to @HeaderParam
		// https://jooby.io/#mvc-api-parameters-header

		JsonArray arr = manip.getApiInformation();

		for(int i = 0; i < arr.size(); i++) {
			JsonObject obj = arr.get(i).getAsJsonObject();

			if(is_admin) {
				if(obj.get("id").toString().equals("\""+uuid+"\"")) {
					return new ModelAndView<>("account_admin.hbs", manip.createJsonMap(obj));
				}
			}

			else {
				if(obj.get("id").toString().equals("\""+uuid+"\"")) {
					return new ModelAndView<>("account.hbs", manip.createJsonMap(obj));
				}
			}
		}

		log.error("Unable to find an account with that uuid");
		return null;
	}
}