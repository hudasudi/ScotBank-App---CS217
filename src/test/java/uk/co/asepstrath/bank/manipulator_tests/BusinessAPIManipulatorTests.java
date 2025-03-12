package uk.co.asepstrath.bank.manipulator_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;

import uk.co.asepstrath.bank.Business;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;

import javax.sql.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BusinessAPIManipulatorTests {

	@Test
	public void testBusinessAPIManipulator() {
		BusinessAPIManipulator manipulator = new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class));
		assertNotNull(manipulator);
	}

	@Test
	public void checkMakeJsonObject() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, SQLException {
		BusinessAPIManipulator manipulator = new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class));

		Method method = BusinessAPIManipulator.class.getDeclaredMethod("makeJsonObject", ResultSet.class);
		method.setAccessible(true);

		ResultSet s = mock(ResultSet.class);

		when(s.getString("ID")).thenReturn("IDENTIFICATION");
		when(s.getString("Name")).thenReturn("NAME");
		when(s.getString("Category")).thenReturn("CATEGORY");
		when(s.getBoolean("Sanctioned")).thenReturn(false);

		JsonObject out = (JsonObject) method.invoke(manipulator, s);
		JsonObject out_err = (JsonObject) method.invoke(manipulator, (Object) null);

		assertNotNull(out);
		assertEquals("IDENTIFICATION", out.get("id").getAsString());
		assertEquals("NAME", out.get("name").getAsString());
		assertEquals("CATEGORY", out.get("category").getAsString());
		assertFalse(out.get("sanctioned").getAsBoolean());

		// Forcing Exception by passing null ResultSet
		assertNull(out_err);
	}

	@Test
	public void checkGetTableQuery() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		BusinessAPIManipulator manipulator = new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class));

		Method method = BusinessAPIManipulator.class.getDeclaredMethod("getTableQuery");
		method.setAccessible(true);

		String query = (String) method.invoke(manipulator);

		assertNotNull(query);
		assertEquals("SELECT * FROM Businesses", query);
	}

	@Test
	public void checkCreateJsonMap() {
		BusinessAPIManipulator manipulator = new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class));
		JsonObject obj = new JsonObject();

		obj.addProperty("id", "ID");
		obj.addProperty("name", "NAME");
		obj.addProperty("category", "CATEGORY");
		obj.addProperty("sanctioned", false);

		Map<String, Object> map = manipulator.createJsonMap(obj);

		assertNotNull(map);
		assertEquals("ID", map.get("id"));
		assertEquals("NAME", map.get("name"));
		assertEquals("CATEGORY", map.get("category"));
		assertEquals("No", map.get("sanctioned"));
	}

	@Test
	public void checkJsonToBusinesses() {
		// Setup
		BusinessAPIManipulator manip = spy(new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class)));

		JsonArray arr = new JsonArray();
		JsonObject obj = new JsonObject();

		obj.addProperty("id", "ID");
		obj.addProperty("name", "NAME");
		obj.addProperty("category", "CATEGORY");
		obj.addProperty("sanctioned", false);

		arr.add(obj);

		doReturn(arr).when(manip).getApiInformation();

		ArrayList<Business> businesses = manip.jsonToBusinesses();

		assertNotNull(businesses);
		assertEquals(1, businesses.size());

		Business b = businesses.getFirst();

		assertNotNull(b);
		assertEquals("ID", b.getId());
		assertEquals("NAME", b.getName());
		assertEquals("CATEGORY", b.getCategory());
		assertFalse(b.isSanctioned());
	}
}