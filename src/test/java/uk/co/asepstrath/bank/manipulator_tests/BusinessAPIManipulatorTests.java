package uk.co.asepstrath.bank.manipulator_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Business;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BusinessAPIManipulatorTests {

	/** Check whether class instantiation works as expected */
	@Test
	public void testBusinessAPIManipulator() {
		BusinessAPIManipulator manipulator = new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class));
		assertNotNull(manipulator);
	}

	/** Testing whether makeJsonObject() works as intended.
	 * Expected normal output: out with values IDENTIFICATION, NAME, CATEGORY & False.
	 * Expected error output: out_err is null.
	*/
	@Test
	public void checkMakeJsonObject() {
		try {
			// Make manipulator
			BusinessAPIManipulator manipulator = new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class));

			// Set method as accessible
			Method method = BusinessAPIManipulator.class.getDeclaredMethod("makeJsonObject", ResultSet.class);
			method.setAccessible(true);

			ResultSet s = mock(ResultSet.class);

			// Set dummy information on method call
			when(s.getString("ID")).thenReturn("IDENTIFICATION");
			when(s.getString("Name")).thenReturn("NAME");
			when(s.getString("Category")).thenReturn("CATEGORY");
			when(s.getBoolean("Sanctioned")).thenReturn(false);

			// Get method output & error output
			JsonObject out = (JsonObject) method.invoke(manipulator, s);
			JsonObject out_err = (JsonObject) method.invoke(manipulator, (Object) null);

			// Check normal output
			assertNotNull(out);
			assertEquals("IDENTIFICATION", out.get("id").getAsString());
			assertEquals("NAME", out.get("name").getAsString());
			assertEquals("CATEGORY", out.get("category").getAsString());
			assertFalse(out.get("sanctioned").getAsBoolean());

			// Forcing Exception by passing null ResultSet
			assertNull(out_err);
		}

		catch(Exception e) {
			throw new AssertionError("checkMakeJsonObject() failed", e);
		}

	}

	/** Testing whether getTableQuery() works as intended.
	 * Expected normal output: String with value "SELECT * FROM Businesses".
	*/
	@Test
	public void checkGetTableQuery() {
		try {
			// Make manipulator
			BusinessAPIManipulator manipulator = new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class));

			// Set method as accessible
			Method method = BusinessAPIManipulator.class.getDeclaredMethod("getTableQuery");
			method.setAccessible(true);

			// Get output
			String query = (String) method.invoke(manipulator);

			// Check output
			assertNotNull(query);
			assertEquals("SELECT * FROM Businesses", query);
		}

		catch(Exception e) {
			throw new AssertionError("checkGetTableQuery() failed", e);
		}

	}

	/** Testing whether createJsonMap() works as intended.
	 * Expected normal output: map with values ID, NAME, CATEGORY & No.
	 * Expected error output: map is null
	*/
	@Test
	public void checkCreateJsonMap() {
		BusinessAPIManipulator manipulator = new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class));
		JsonObject obj = new JsonObject();

		// Make dummy information
		obj.addProperty("id", "ID");
		obj.addProperty("name", "NAME");
		obj.addProperty("category", "CATEGORY");
		obj.addProperty("sanctioned", false);

		// Get output
		Map<String, Object> map = manipulator.createJsonMap(obj);

		// Check output
		assertNotNull(map);
		assertEquals("ID", map.get("id"));
		assertEquals("NAME", map.get("name"));
		assertEquals("CATEGORY", map.get("category"));
		assertEquals("No", map.get("sanctioned"));

		// Check error output
		Map<String, Object> err_map = manipulator.createJsonMap(null);

		assertNull(err_map);
	}

	/** Testing whether checkJsonToBusinesses() works as intended.
	 * Expected normal output: b with values ID, NAME, CATEGORY & False.
	*/
	@Test
	public void checkJsonToBusinesses() {
		// Setup
		BusinessAPIManipulator manip = spy(new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class)));

		JsonArray arr = new JsonArray();
		JsonObject obj = new JsonObject();

		// Create dummy information
		obj.addProperty("id", "ID");
		obj.addProperty("name", "NAME");
		obj.addProperty("category", "CATEGORY");
		obj.addProperty("sanctioned", false);

		arr.add(obj);

		// When method called, return our array
		doReturn(arr).when(manip).getApiInformation();

		// Get all businesses
		ArrayList<Business> businesses = manip.jsonToBusinesses();

		// Check business exists
		assertNotNull(businesses);
		assertEquals(1, businesses.size());

		// Get output
		Business b = businesses.getFirst();

		// Check output
		assertNotNull(b);
		assertEquals("ID", b.getId());
		assertEquals("NAME", b.getName());
		assertEquals("CATEGORY", b.getCategory());
		assertFalse(b.isSanctioned());
	}
}