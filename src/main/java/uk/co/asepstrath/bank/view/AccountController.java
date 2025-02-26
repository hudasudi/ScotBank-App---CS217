package uk.co.asepstrath.bank.view;

import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jooby.annotation.Path;
import io.jooby.annotation.QueryParam;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.api.AccountAPIManipulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

import javax.sql.DataSource;

@Path("/accounts")
public class AccountController {
	private AccountAPIManipulator manip;
	private final Logger log;
	private final DataSource ds; // ? Necessary?

	/** This class controls the Jooby formatting & deployment of pages to the site
	 * @param log The program log
	 */
	public AccountController(Logger log, DataSource ds) {
		this.log = log;
		this.manip = new AccountAPIManipulator(log, ds);
		this.ds = ds;
	}

	public void setAccountAPIManipulator(AccountAPIManipulator manip) {
		this.manip = manip;
	}

	/** Get & populate the handlebars template with information from the API
	 * @return The model to build & deploy
	*/
	@GET("/account-view")
	public ModelAndView<Map<String, Object>> getAccounts() {
		Map<String, Object> model = this.manip.createHandleBarsJSONMap();

		return new ModelAndView<>("accounts.hbs", model);
	}

	/** Get & populate the handlebars template with information for a single account from the API
	 * @param uuid The Account's UUID
	 * @param is_admin Whether the Account is an admin account or not
	 * @return The model to build & deploy
	*/
	@GET("/account")
	public ModelAndView<Map<String, String>> getAccount(@QueryParam String uuid, @QueryParam Boolean is_admin) {

		if(uuid == null || is_admin == null) {
			Map<String, String> map = new HashMap<>();
			map.put("err", "400 - Bad Request");

			map.put("msg", "No uuid or is_admin parameter provided!");

			return new ModelAndView<>("error.hbs", map);
		}

		JsonArray arr = this.manip.getApiInformation();

		for(int i = 0; i < arr.size(); i++) {
			JsonObject obj = arr.get(i).getAsJsonObject();

			if(obj.get("id").toString().equals("\""+uuid+"\"")) {
				if(is_admin) {
					return new ModelAndView<>("account_admin.hbs", this.manip.createJsonMap(obj));
				} else {
					return new ModelAndView<>("account.hbs", this.manip.createJsonMap(obj));
				}
			}
		}

		log.error("Unable to find an account with that uuid");
		return null;
	}

	/** Get an array of Account from the JSON information in the API file & return their information in String form
	 * @return The JSON information as a String
	 * @deprecated Use getAccounts instead
	*/
	@Deprecated
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
	 * @deprecated Use getAccount instead
	 */
	@Deprecated
	@GET("/account-object")
	public String accountsObject(@QueryParam int pos) {
		ArrayList<Account> array = this.manip.jsonToAccounts();

		return array.get(pos).toString();
	}
}