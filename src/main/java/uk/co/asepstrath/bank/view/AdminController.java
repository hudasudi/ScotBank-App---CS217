package uk.co.asepstrath.bank.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.Path;

import org.slf4j.Logger;

import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;

import javax.sql.DataSource;
import java.math.BigDecimal;
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
}