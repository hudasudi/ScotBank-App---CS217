package uk.co.asepstrath.bank.api.manipulators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import uk.co.asepstrath.bank.Business;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BusinessAPIManipulator extends APIManipulator {

	/**
	 * This class manipulates API information to format it into varied forms
	 * @param log The program log
	 * @param ds  The DataSource to pull info from
	 */
	public BusinessAPIManipulator(Logger log, DataSource ds) {
		super(log, ds);
	}

	/** Make a JsonObject with a given ResultSet
	 * @param set The ResultSet to pull data from
	 * @return a JsonObject with ResultSet values
	*/
	@Override
	protected JsonObject makeJsonObject(ResultSet set) {
		try {
			JsonObject object = new JsonObject();

			object.addProperty("id", set.getString("ID"));
			object.addProperty("name", set.getString("Name"));
			object.addProperty("category", set.getString("Category"));
			object.addProperty("sanctioned", set.getBoolean("Sanctioned"));

			return object;
		}

		catch(Exception e) {
			log.error("An error occurred whilst trying to retrieve API information from the database", e);
			return null;
		}
	}

	/** Get the database query to use on a database
	 * @return The database query
	*/
	@Override
	protected String getTableQuery() {
		return "SELECT * FROM Businesses";
	}

	/** Create a Map with a JsonObject's values
	 * @param object The JsonObject to convert
	 * @return A map with the JsonObject's values
	*/
	@Override
	public Map<String, Object> createJsonMap(JsonObject object) {
		if(object == null) {
			return null;
		}

		Map<String, Object> map = new HashMap<>();

		map.put("id", object.get("id").getAsString());
		map.put("name", object.get("name").getAsString());
		map.put("category", object.get("category").getAsString());
		map.put("sanctioned", object.get("sanctioned").getAsBoolean() ? "Yes" : "No");

		return map;
	}

	/**
	 * Take the response JSON from the API, create a new Business from each element in the JSON array & return an ArrayList of created Businesses
	 * @return ArrayList<Business> of all businesses created from each JSON element
	*/
	public ArrayList<Business> jsonToBusinesses() {
		JsonArray elements = this.getApiInformation();

		ArrayList<Business> businesses = new ArrayList<>();

		for (int i = 0; i < elements.size(); i++) {
			JsonObject element = elements.get(i).getAsJsonObject();

			businesses.add(new Business(
					element.get("id").getAsString(),
					element.get("name").getAsString(),
					element.get("category").getAsString(),
					element.get("sanctioned").getAsBoolean()
			));
		}

		return businesses;
	}

	/** Get a specific business from its name
	 * @param name The businesses name
	 * @return A business object if the name matches, otherwise null
	*/
	public Business getBusiness(String name) {
		Business business = null;

		if(name == null) return null;

		for(Business b : this.jsonToBusinesses()) {
			if(b.getName().equals(name)) {
				business = b;
			}
		}

		return business;
	}
}