package uk.co.asepstrath.bank.manipulator_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.manipulators.APIManipulator;
import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class APIManipulatorTests {

	@Test
	public void testAPIManipulator() {
		APIManipulator manip = mock(APIManipulator.class);
		assertNotNull(manip);
	}

	// Using ANY subclass of APIManipulator will suffice
	@Test
	public void testGetApiInformation() throws SQLException {
		DataSource ds = mock(DataSource.class);

		when(ds.getConnection()).thenThrow(new SQLException("Unable to connect to the database"));

		BusinessAPIManipulator manip = new BusinessAPIManipulator(mock(Logger.class), ds);

		JsonArray result = manip.getApiInformation();

		assertNull(result);
	}

	@Test
	public void checkExecuteDatabaseQuery() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SQLException {
		DataSource ds = mock(DataSource.class);
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		ResultSet mockSet = mock(ResultSet.class);

		when(ds.getConnection()).thenReturn(conn);
		when(conn.prepareStatement(anyString())).thenReturn(stmt);
		when(stmt.executeQuery()).thenReturn(mockSet);
		when(mockSet.getString("test")).thenReturn("Affirmative");

		AccountAPIManipulator manip = new AccountAPIManipulator(mock(Logger.class), ds);

		Method method = APIManipulator.class.getDeclaredMethod("executeDatabaseQuery", String.class);
		method.setAccessible(true);

		String query = "SELECT * FROM Accounts LIMIT 1;";

		ResultSet out = (ResultSet) method.invoke(manip, query);

		assertNotNull(out);
		assertEquals("Affirmative", out.getString("test"));
	}

	@Test
	public void checkGetDatabaseResults() throws SQLException {
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

		AccountAPIManipulator manip = new AccountAPIManipulator(mock(Logger.class), ds);

		JsonArray out = manip.getDatabaseResults("SELECT * FROM Acounts LIMIT 1;");

		assertNotNull(out);

		assertEquals("Affirmative", out.get(0).getAsJsonObject().get("id").getAsString());

		// Check null version
		when(mockSet.next()).thenReturn(true).thenReturn(false);
		when(mockSet.getObject(anyInt())).thenReturn(null);

		JsonArray out2 = manip.getDatabaseResults("SELECT * FROM Accounts LIMIT 1;");

		assertNotNull(out2);

		// ! null != null ... ?
//		assertNull(out2.get(0).getAsJsonObject().get("id"));
	}

	@Test
	public void checkCreateGenericJsonMap() {
		DataSource ds = mock(DataSource.class);
		AccountAPIManipulator manip = new AccountAPIManipulator(mock(Logger.class), ds);

		JsonObject obj = new JsonObject();
		obj.addProperty("test_1", "test_value1");
		obj.addProperty("test_2", "test_value2");

		Map<String, Object> out = manip.createGenericJsonMap(obj);

		assertNotNull(out);
		assertEquals("\"test_value1\"", out.get("test_1").toString());
		assertEquals("\"test_value2\"", out.get("test_2").toString());

		Set<String> key_set = new HashSet<>();
		key_set.add("new_test_1");
		key_set.add("new_test_2");

		Map<String, Object> out2 = manip.createGenericJsonMap(obj, key_set);

		assertNotNull(out2);
		assertEquals("\"test_value1\"", out2.get("new_test_1").toString());
		assertEquals("\"test_value2\"", out2.get("new_test_2").toString());
	}
}