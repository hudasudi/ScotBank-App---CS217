package uk.co.asepstrath.bank.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.Path;
import io.jooby.annotation.QueryParam;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Business;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Path("/businesses")
public class BusinessController {
	private BusinessAPIManipulator manip;
	private final Logger log;
	private final DataSource ds;

	/** This class controls the Jooby formatting & deployment of business pages to the site
	 * @param log The program log
	 * @param ds The DataSource to pull from
	*/
	public BusinessController(Logger log, DataSource ds) {
		this.log = log;
		this.ds = ds;
		this.manip = new BusinessAPIManipulator(log, ds);
	}

	public void setBusinessAPIManipulator(BusinessAPIManipulator manip) {
		this.manip = manip;
	}

	/** Get & populate the handlebars template with information from the API
	 * @return The model to build & deploy
	*/
	@GET("/business-view")
	public ModelAndView<Map<String, Object>> getBusinesses() {
		Map<String, Object> model = this.manip.createHandleBarsJSONMap("business", 12);

		return new ModelAndView<>("businesses.hbs", model);
	}

	/** Get & populate the handlebars template with information for a single business from the API
	 * @param name The Businesses name
	 * @return The model to build & deploy
	*/
	@GET("/business")
	public ModelAndView<Map<String, String>> getBusiness(@QueryParam String name) {
		try {
			if(name == null) {
				return this.buildErrorPage("400 - Bad Request", "No name parameter provided!");
			}

			JsonArray arr = this.manip.getApiInformation();

			for(int i = 0; i < arr.size(); i++) {
				JsonObject obj = arr.get(i).getAsJsonObject();

				if(obj.get("name").getAsString().equals(name)) {
					return new ModelAndView<>("business.hbs", this.manip.createJsonMap(obj));
				}
			}

			return this.buildErrorPage("404 - Not Found",  "No business found with name " + name);
		}

		catch(Exception e) {
			log.error("Error whilst building handlebars template", e);

			return this.buildErrorPage("500 - Internal Server Error", "An unexpected error occurred while processing your request");
		}
	}

	private ModelAndView<Map<String, String>> buildErrorPage(String error, String msg) {
		Map<String, String> map = new HashMap<>();

		map.put("err", error);
		map.put("msg", msg);

		return new ModelAndView<>("error.hbs", map);
	}

	/** Get an array of Business from the JSON information in the Database & return their information in String form
	 * @return The JSON information as a String
	 * @deprecated Use getBusinesses instead
	*/
	@Deprecated
	@GET("/business-objects")
	public String businessObjects() {
		ArrayList<Business> array = this.manip.jsonToBusinesses();

		StringBuilder out = new StringBuilder();

		for(Business a : array) {
			out.append(a.toString()).append("\n\n");
		}

		return out.toString();
	}

	/** Get specific account information from a user query in the URL
	 * @param pos The Business JSON to get
	 * @return The Business JSON information as a String
	 * @deprecated Use getBusiness instead
	*/
	@Deprecated
	@GET("/business-object")
	public String businessObject(@QueryParam int pos) {
		ArrayList<Business> array = this.manip.jsonToBusinesses();

		if(pos < 0 || pos > array.size()-1) {
			return "Error 400 - Bad Request\nThe index you requested is out of bounds";
		}

		return array.get(pos).toString();
	}
}
