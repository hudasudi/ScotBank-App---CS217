package uk.co.asepstrath.bank.manipulator_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.manipulators.APIManipulator;
import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Using ANY subclass of APIManipulator will suffice
public class APIManipulatorTests {

	/** Testing whether class instantiation works as expected */
	@Test
	public void testAPIManipulator() {
		APIManipulator manip = mock(APIManipulator.class);
		assertNotNull(manip);
	}

	/** Testing whether getApiInformation() errors as expected due to wrong DataSource.
	 * Expected output: SQLException().
	*/
	@Test
	public void testGetApiInformation() throws SQLException {
		try {
			DataSource ds = mock(DataSource.class);

			// This is handled elsewhere, but will return us null
			when(ds.getConnection()).thenThrow(new SQLException("Unable to connect to the database"));

			BusinessAPIManipulator manip = new BusinessAPIManipulator(mock(Logger.class), ds);

			// This will be null if the method fails, which it should because ds is not correct
			JsonArray result = manip.getApiInformation();

			assertNull(result);
		}

		catch(Exception e) {
			throw new AssertionError("getApiInformation() failed", e);
		}
	}

	/** Testing whether executeDatabaseQuery() works as expected.
	 * Expected normal output: out with value Affirmative.
	 * Expected error output: out_err throws SQLException & is null.
	 */
	@Test
	public void checkExecuteDatabaseQuery() {
		try {
			// Setup mocked stuff to insert information where necessary
			DataSource ds = mock(DataSource.class);
			Connection conn = mock(Connection.class);
			PreparedStatement stmt = mock(PreparedStatement.class);
			ResultSet mockSet = mock(ResultSet.class);

			when(ds.getConnection()).thenReturn(conn);
			when(conn.prepareStatement(anyString())).thenReturn(stmt);
			when(stmt.executeQuery()).thenReturn(mockSet);
			when(mockSet.getString("test")).thenReturn("Affirmative");

			// Make manipulator class
			AccountAPIManipulator manip = new AccountAPIManipulator(mock(Logger.class), ds);

			// Set method to visible for our tests as its protected
			Method method = APIManipulator.class.getDeclaredMethod("executeDatabaseQuery", String.class);
			method.setAccessible(true);

			String query = "SELECT * FROM Accounts LIMIT 1;";

			// Get method output
			ResultSet out = (ResultSet) method.invoke(manip, query);

			// Check normal output
			assertNotNull(out);
			assertEquals("Affirmative", out.getString("test"));

			when(ds.getConnection()).thenThrow(new SQLException("failed to connect to the database"));

			// Check error output
			ResultSet err_out = (ResultSet) method.invoke(manip, (Object) null);

			assertNull(err_out);
		}

		catch(Exception e) {
			throw new AssertionError("checkExecuteDatabaseQuery() failed\n", e);
		}

	}

	/** Testing whether getDatabaseResults() works as expected.
	 * Expected normal output: out with value Affirmative.
	 * Expected error output: out2 is null.
	*/
	@Test
	public void checkGetDatabaseResults() {
		try {
			// Setup mock stuff to insert dummy info
			DataSource ds = mock(DataSource.class);
			Connection conn = mock(Connection.class);
			PreparedStatement stmt = mock(PreparedStatement.class);
			ResultSet mockSet = mock(ResultSet.class);
			ResultSetMetaData mockSetMetaData = mock(ResultSetMetaData.class);

			when(ds.getConnection()).thenReturn(conn);
			when(conn.prepareStatement(anyString())).thenReturn(stmt);
			when(stmt.executeQuery()).thenReturn(mockSet);
			when(mockSet.getMetaData()).thenReturn(mockSetMetaData);
			when(mockSet.next()).thenReturn(true).thenReturn(false);
			when(mockSetMetaData.getColumnCount()).thenReturn(1);
			when(mockSetMetaData.getColumnName(anyInt())).thenReturn("id");
			when(mockSet.getObject(anyInt())).thenReturn("Affirmative");

			// Make manipulator clas
			AccountAPIManipulator manip = new AccountAPIManipulator(mock(Logger.class), ds);

			// Get method output
			JsonArray out = manip.getDatabaseResults("SELECT * FROM Accounts LIMIT 1;");

			// Check normal output
			assertNotNull(out);
			assertEquals("Affirmative", out.get(0).getAsJsonObject().get("id").getAsString());

			// Check error output
			when(mockSet.next()).thenReturn(true).thenReturn(false);
			when(mockSet.getObject(anyInt())).thenReturn(null);

			JsonArray out2 = manip.getDatabaseResults("SELECT * FROM Accounts LIMIT 1;");

			assertNotNull(out2);
			assertTrue(out2.get(0).getAsJsonObject().get("id").isJsonNull());
		}

		catch(Exception e) {
			throw new AssertionError("checkGetDatabaseResults() failed", e);
		}
	}

	/** Testing whether createGenericJsonMap() works as expected.
	 * Expected normal output (1 param): out with values "test_value1" & "test_value2".
	 * Expected normal output (2 params): out with values "test_value1" & "test_value2".
	 * Expected error output (1 param): err_out1 with value {}.
	 * Expected error output (2 param): err_out2 with value {}.
	*/
	@Test
	public void checkCreateGenericJsonMap() {
		// Make manipulator
		DataSource ds = mock(DataSource.class);
		AccountAPIManipulator manip = new AccountAPIManipulator(mock(Logger.class), ds);

		// Make dummy data
		JsonObject obj = new JsonObject();
		obj.addProperty("test_1", "test_value1");
		obj.addProperty("test_2", "test_value2");

		// Get method output 1
		Map<String, Object> out = manip.createGenericJsonMap(obj);

		// Do checks on output 1
		assertNotNull(out);
		assertEquals("\"test_value1\"", out.get("test_1").toString());
		assertEquals("\"test_value2\"", out.get("test_2").toString());

		// Get method output 2
		Set<String> key_set = new HashSet<>();
		key_set.add("new_test_1");
		key_set.add("new_test_2");

		Map<String, Object> out2 = manip.createGenericJsonMap(obj, key_set);

		// Do checks on output 2
		assertNotNull(out2);
		assertEquals("\"test_value1\"", out2.get("new_test_1").toString());
		assertEquals("\"test_value2\"", out2.get("new_test_2").toString());

		// Error output 1
		Map<String, Object> err_out1 = manip.createGenericJsonMap(new JsonObject());
		assertEquals("{}", err_out1.toString());

		// Error output 2
		Map<String, Object> err_out2 = manip.createGenericJsonMap(new JsonObject(), new HashSet<>());
		assertEquals("{}", err_out2.toString());
	}
}