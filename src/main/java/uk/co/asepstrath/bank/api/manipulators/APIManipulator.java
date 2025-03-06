package uk.co.asepstrath.bank.api.manipulators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class APIManipulator {
	final Logger log;
	final DataSource ds;

	/** This class manipulates API information to format it into varied forms
	 * @param log The program log
	 * @param ds The DataSource to pull info from
	*/
	public APIManipulator(Logger log, DataSource ds) {
		this.log = log;
		this.ds = ds;
	}

	/** Make a Json Object with an APIs information
	 * @param set The ResultSet to pull data from
	 * @return a JsonObject containing all necessary information
	*/
	protected abstract JsonObject makeJsonObject(ResultSet set);

	/** Get a table-specific query for the database
	 * @return The query to run
	*/
	protected abstract String getTableQuery();

	/** Get the API information stored in db, parse it into a JsonArray & return it
	 * @return The API Information
	*/
	public JsonArray getApiInformation() {
		JsonArray arr = new JsonArray();

		try(Connection conn = this.ds.getConnection()) {
			Statement stmt = conn.createStatement();

			ResultSet set = stmt.executeQuery(this.getTableQuery());

			while(set.next()) {
				arr.add(this.makeJsonObject(set));
			}

			stmt.close();

			return arr;
		}

		catch(SQLException e) {
			log.error("An error occurred whilst trying to retrieve API information from the database", e);
			return null;
		}
	}

	/** Take the response JSON from the API & format it so that Handlebars can utilise the information
	 * @return Formatted (Handlebars compliant) map for the HTML template
	*/
	public Map<String, Object> createHandleBarsJSONMap(String model_name, int col_size) {
		JsonArray json = this.getApiInformation();

		List<Map<String, String>> object_list = new ArrayList<>();

		for(int i = 0; i < json.size(); i++) {
			JsonObject obj = json.get(i).getAsJsonObject();

			object_list.add(this.createJsonMap(obj));
		}

		List<List<Map<String, String>>> chunked_objects = this.chunkList(object_list, col_size);

		Map<String, Object> model = new HashMap<>();
		model.put(model_name+"_chunks", chunked_objects);

		return model;
	}

	/** Take a JsonObject & Convert it into a Map of Key-Value Pairs
	 * @param object The JsonObject to convert
	 * @return A map of all the JsonObject's Key-Value Pairs
	*/
	protected abstract Map<String, String> createJsonMap(JsonObject object);

	/** Takes a list of all accounts, chunks them into sections of chunk_size then returns a new list in chunks of chunk_size
	 * @param list_to_chunk The list to chunk into sections
	 * @param chunk_size The number of elements per chunk in the list
	 * @return The newly chunked list
	*/
	private List<List<Map<String, String>>> chunkList(List<Map<String, String>> list_to_chunk, int chunk_size) {
		List<List<Map<String, String>>> chunks = new ArrayList<>();

		for(int i = 0; i < list_to_chunk.size(); i += chunk_size) {
			chunks.add(list_to_chunk.subList(i, Math.min(i + chunk_size, list_to_chunk.size())));
		}

		return chunks;
	}
}
