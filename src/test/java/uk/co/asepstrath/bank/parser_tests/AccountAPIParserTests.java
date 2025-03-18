package uk.co.asepstrath.bank.parser_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.parsers.AccountAPIParser;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AccountAPIParserTests {

	/** Testing whether writeAPIInformation() works as intended.
	 * Expected normal output: no errors.
	*/
	@Test
	public void checkWriteAPIInformation() {
		try {
			// Make mock objects to fake info
			DataSource ds = mock(DataSource.class);
			Connection con = mock(Connection.class);
			PreparedStatement ps = mock(PreparedStatement.class);
			ResultSet rs = mock(ResultSet.class);

			// Setup what to do upon method invocation
			when(ds.getConnection()).thenReturn(con);
			when(con.prepareStatement(anyString())).thenReturn(ps);

			// For getApiInformation
			when(con.prepareStatement(anyString())).thenReturn(ps);
			when(rs.next()).thenReturn(true, false);

			// Override parseResponse for our fake info
			AccountAPIParser parser = new AccountAPIParser(mock(Logger.class), "", ds) {
				@Override
				protected JsonArray parseResponse() {
					// Setup fake info
					JsonObject json = new JsonObject();
					json.addProperty("id", "1234-5678-9101");
					json.addProperty("name", "name");
					json.addProperty("startingBalance", 100);
					json.addProperty("roundUpEnabled", false);

					JsonArray arr = new JsonArray();
					arr.add(json);

					return arr;
				}
			};

			// 'Write' Info to db
			parser.writeAPIInformation();
		}

		catch(Exception e) {
			throw new AssertionError("checkWriteAPIInformation() Failed", e);
		}
	}

	/** Testing whether getInsertQuery() works as intended.
	 * Expected normal output: String with value "INSERT INTO Accounts (UUID, Name, Balance, roundUpEnabled) VALUES (?, ?, ?, ?)"
	*/
	@Test
	public void checkGetInsertQuery() {
		try {
			// Make class
			AccountAPIParser parser = new AccountAPIParser(mock(Logger.class), "", mock(DataSource.class));

			// Set method to accessible
			Method method = AccountAPIParser.class.getDeclaredMethod("getInsertQuery");
			method.setAccessible(true);

			// Get output
			String query = (String) method.invoke(parser);

			// Check output
			assertNotNull(query);
			assertEquals("INSERT INTO Accounts (UUID, Name, Balance, roundUpEnabled) VALUES (?, ?, ?, ?)", query);
		}

		catch(Exception e) {
			throw new AssertionError("checkGetInsertQuery() Failed", e);
		}
	}

	/** Testing whether parseResponse() works as intended.
	 * Expected normal output: values String, String, Double & Boolean.
	*/
	@Test
	public void checkParseResponse() {
		try {
			// Make class
			AccountAPIParser parser = new AccountAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/accounts", mock(DataSource.class));

			// Set method as accessible
			Method method = AccountAPIParser.class.getDeclaredMethod("parseResponse");
			method.setAccessible(true);

			// Get output array
			JsonArray json = (JsonArray) method.invoke(parser);

			assertNotNull(json);

			// Get output
			JsonObject obj = json.get(0).getAsJsonObject();

			// Check output
			assertNotNull(obj.get("id").getAsString());
			assertNotNull(obj.get("name").getAsString());
			assertNotNull(obj.get("startingBalance"));
			assertNotNull(obj.get("roundUpEnabled"));

			// Trigger catch statement
			AccountAPIParser err_parser = new AccountAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mock(DataSource.class));

			JsonArray result = (JsonArray) method.invoke(err_parser);

			assertNull(result);
		}

		catch(Exception e) {
			throw new AssertionError("checkParseResponse() Failed", e);
		}
	}
}