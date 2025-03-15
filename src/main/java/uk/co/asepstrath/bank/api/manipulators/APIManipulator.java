package uk.co.asepstrath.bank.api.manipulators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

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

	/** Make a JsonObject with an APIs information
	 * @param set The ResultSet to pull data from
	 * @return a JsonObject containing all necessary information
	*/
	protected abstract JsonObject makeJsonObject(ResultSet set);

	/** Get a table-specific query for the database
	 * @return The query to run
	*/
	protected abstract String getTableQuery();

	/** Query the database with the given query & return the ResultSet
	 * @param query The query for the database
	 * @return The result of the query
	*/
	private ResultSet executeDatabaseQuery(String query) {
		try(Connection conn = this.ds.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(query);

			return stmt.executeQuery();
		}

		catch(SQLException e) {
			log.error("An error occurred whilst trying to execute a query to the database", e);
			return null;
		}
	}

	/** Format a database result into a JsonArray
	 * @param query The query for the database
	 * @return Formatted JsonArray of the database result
	*/
	public JsonArray getDatabaseResults(String query) {
		JsonArray array = new JsonArray();

		try {
			ResultSet set = this.executeDatabaseQuery(query);

			if(set == null) { return null; }

			ResultSetMetaData meta_data = set.getMetaData();
			int column_count = meta_data.getColumnCount();

			while(set.next()) {
				JsonObject object = new JsonObject();

				for(int i = 1; i <= column_count; i++) {
					String column_name = meta_data.getColumnName(i);
					Object column_value = set.getObject(i);

					object.addProperty(column_name, column_value != null ? column_value.toString() : null);
				}

				array.add(object);
			}

			set.close();
		}

		catch(SQLException e) {
			return null;
		}

		return array;
	}

	/** Create a Map with a JsonObject we don't know the keys to
	 * @param object The object to turn into a Map
	 * @return The Map of the object
	*/
	public Map<String, Object> createGenericJsonMap(JsonObject object) {
		Set<String> key_set = object.keySet();
		Map<String, Object> map = new HashMap<>();

		for(String key : key_set) {
			map.put(key, object.get(key));
		}

		return map;
	}

	/** Create a Map with a given JsonObject & set the keys to a specific value
	 * @param object The object to turn into a Map
	 * @param keys The keys to use in the Map
	 * @return The Map of the object
	*/
	public Map<String, Object> createGenericJsonMap(JsonObject object, Set<String> keys) {
		Map<String, Object> map = new HashMap<>();
		Set<String> json_keys = object.keySet();

		Iterator<String> iterator = json_keys.iterator();

		for(String key : keys) {
			map.put(key, object.get(iterator.next()));
		}

		return map;
	}

	/** Get the API information stored in db, parse it into a JsonArray & return it
	 * @return The API Information
	*/
	@SuppressWarnings("All")
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

		List<Map<String, Object>> object_list = new ArrayList<>();

		for(int i = 0; i < json.size(); i++) {
			JsonObject obj = json.get(i).getAsJsonObject();

			object_list.add(this.createJsonMap(obj));
		}

		List<List<Map<String, Object>>> chunked_objects = this.chunkList(object_list, col_size);

		Map<String, Object> model = new HashMap<>();
		model.put(model_name+"_chunks", chunked_objects);

		return model;
	}

	/**
	 * Take a JsonObject & Convert it into a Map of Key-Value Pairs
	 *
	 * @param object The JsonObject to convert
	 * @return A map of all the JsonObject's Key-Value Pairs
	 */
	protected abstract Map<String, Object> createJsonMap(JsonObject object);

	/** Takes a list of all accounts, chunks them into sections of chunk_size then returns a new list in chunks of chunk_size
	 * @param list_to_chunk The list to chunk into sections
	 * @param chunk_size The number of elements per chunk in the list
	 * @return The newly chunked list
	*/
	private List<List<Map<String, Object>>> chunkList(List<Map<String, Object>> list_to_chunk, int chunk_size) {
		List<List<Map<String, Object>>> chunks = new ArrayList<>();

		for(int i = 0; i < list_to_chunk.size(); i += chunk_size) {
			chunks.add(list_to_chunk.subList(i, Math.min(i + chunk_size, list_to_chunk.size())));
		}

		return chunks;
	}
}
