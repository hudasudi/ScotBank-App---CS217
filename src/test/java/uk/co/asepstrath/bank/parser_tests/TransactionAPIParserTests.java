package uk.co.asepstrath.bank.parser_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.parsers.TransactionAPIParser;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionAPIParserTests {

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
			TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "", ds) {
				@Override
				protected JsonArray parseResponse() {
					// Setup fake info
					JsonObject json = new JsonObject();
					json.addProperty("timestamp", "1");
					json.addProperty("amount", "2");
					json.addProperty("sender", "3");
					json.addProperty("id", "4");
					json.addProperty("recipient", "5");
					json.addProperty("type", "6");

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
			TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "", mock(DataSource.class));

			// Set method to accessible
			Method method = TransactionAPIParser.class.getDeclaredMethod("getInsertQuery");
			method.setAccessible(true);

			// Get output
			String query = (String) method.invoke(parser);

			// Check output
			assertNotNull(query);
			assertEquals("INSERT INTO Transactions (Timestamp, Amount, Sender, TransactionID, Recipient, Type) VALUES (?, ?, ?, ?, ?, ?)", query);
		}

		catch(Exception e) {
			throw new AssertionError("checkGetInsertQuery() Failed", e);
		}
	}

	/** Testing whether createRequest() works as intended.
	 * Expected normal output: HttpRequest
	*/
	@Test
	public void checkCreateRequest() {
        try {
            TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mock(DataSource.class));

            Method method = TransactionAPIParser.class.getDeclaredMethod("createRequest", int.class);
            method.setAccessible(true);

            HttpRequest result = (HttpRequest) method.invoke(parser, 1);

            assertNotNull(result);
        }

		catch(Exception e) {
			throw new AssertionError("checkCreateRequest() Failed", e);
		}
    }

	/** Testing whether getResponseStringForPage() works as intended.
	 * Expected normal output: String.
	 * Expected error output: String is null
	 */
	@Test
	public void checkGetResponseStringForPage() {
        try {
            TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mock(DataSource.class));

            Method method = TransactionAPIParser.class.getDeclaredMethod("getResponseStringForPage", int.class);
            method.setAccessible(true);

            String result = (String) method.invoke(parser, 1);

            assertNotNull(result);

            // Force catch statement
            TransactionAPIParser err_parser = new TransactionAPIParser(mock(Logger.class), "https://www.example", mock(DataSource.class));

            String err_result = (String) method.invoke(err_parser, 1);

            assertNull(err_result);
        }

		catch(Exception e) {
			throw new AssertionError("checkGetResponseStringForPage() Failed", e);
		}
    }

	@Test
	@SuppressWarnings("unchecked")
	public void checkGetAPIDataForPage() {
        try {
            TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mock(DataSource.class));

            Method method = TransactionAPIParser.class.getDeclaredMethod("getAPIDataForPage", int.class);
            method.setAccessible(true);

            HttpResponse<String> result = (HttpResponse<String>) method.invoke(parser, 1);

            assertNotNull(result);
            assertEquals(200, result.statusCode());

            // Force catch statement
            TransactionAPIParser err_parser = new TransactionAPIParser(mock(Logger.class), "https://www.example", mock(DataSource.class));

            HttpResponse<String> err_result = (HttpResponse<String>) method.invoke(err_parser, 1);

            assertNull(err_result);
        }

		catch(Exception e) {
			throw new AssertionError("checkGetAPIDataForPage() Failed", e);
		}
    }

	@Test
	public void checkParseResponse() {
        try {
            TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mock(DataSource.class));

            Method method = TransactionAPIParser.class.getDeclaredMethod("parseResponse");
            method.setAccessible(true);

            JsonArray result = (JsonArray) method.invoke(parser);

            assertNotNull(result);

            JsonObject obj = result.get(0).getAsJsonObject();

            assertNotNull(obj);

            TransactionAPIParser err_parser = new TransactionAPIParser(mock(Logger.class), "https://www.example", mock(DataSource.class));

            JsonArray err_result = (JsonArray) method.invoke(err_parser);
            assertTrue(err_result.isEmpty());
        }

		catch(Exception e) {
			throw new AssertionError("checkParseResponse() Failed", e);
		}
    }
}