package uk.co.asepstrath.bank.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.QueryParam;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Transaction;

import io.jooby.annotation.Path;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;

@Path("/transactions")
public class TransactionController extends Controller {

	/** This class controls the Jooby formatting & deployment of transaction pages to the site
	 * @param log The program log
	 * @param ds The DataSource to pull from
	*/
	public TransactionController(Logger log, DataSource ds) {
		super(log, ds);
	}

	/** Get & populate the handlebars template with information from the API
	 * @return The model to build & deploy
	*/
	@GET("/transaction-view")
	public ModelAndView<Map<String, Object>> getTransactions() {
		Map<String, Object> model = this.transaction_manipulator.createHandleBarsJSONMap("transaction", 1000);

		return new ModelAndView<>("transactions.hbs", model);
	}

	/** Get & populate the handlebars template with information for a single transaction from the API
	 * @param uuid The Transaction uuid
	 * @return The model to build & deploy
	 */
	@GET("/transaction")
	public ModelAndView<Map<String, Object>> getTransaction(@QueryParam String uuid) {
		try {
			if(uuid == null) {
				return this.buildErrorPage("400 - Bad Request", "No uuid parameter provided!");
			}

			JsonArray arr = this.transaction_manipulator.getApiInformation();

			for(int i =0; i < arr.size(); i++) {
				JsonObject obj = arr.get(i).getAsJsonObject();

				if(obj.get("id").getAsString().equals(uuid)) {
					return new ModelAndView<>("transaction.hbs", this.transaction_manipulator.createJsonMap(obj));
				}
			}

			return this.buildErrorPage("404 - Not Found", "No transaction found with uuid " + uuid);
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);

			return this.buildErrorPage("500 - Internal Server Error", "An unexpected error occurred while processing your request");
		}
	}

	/** Get an array of Transaction from the JSON information in the Database & return their information in String form
	 * @return The JSON information as a String
	 * @deprecated Use getTransactions instead
	*/
	@Deprecated
	@GET("/transaction-objects")
	public String transactionObjects() {
		ArrayList<Transaction> arr = this.transaction_manipulator.jsonToTransactions();

		StringBuilder out = new StringBuilder();

		for(Transaction a : arr) {
			out.append(a.toString()).append("\n\n");
		}

		return out.toString();
	}

	/** Get specific account information from a user query in the URL
	 * @param pos The Business JSON to get
	 * @return The Transaction JSON information as a String
	 * @deprecated Use getTransaction instead
	*/
	@Deprecated
	@GET("/transaction-object")
	public String transactionObject(@QueryParam int pos) {
		ArrayList<Transaction> arr = this.transaction_manipulator.jsonToTransactions();

		if(pos < 0 || pos > arr.size()-1) {
			return "Error 400 - Bad Request\nThe index you requested is out of bounds";
		}

		return arr.get(pos).toString();
	}
}
