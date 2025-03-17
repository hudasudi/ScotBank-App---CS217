package uk.co.asepstrath.bank.manipulator_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;

import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;

import javax.sql.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.math.BigDecimal;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountAPIManipulatorTests {

	@Test
	public void testAccountAPIManipulator() {
		AccountAPIManipulator manipulator = new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class));
		assertNotNull(manipulator);
	}

	@Test
	public void checkMakeJsonObject() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, SQLException {
		AccountAPIManipulator manipulator = new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class));

		Method method = AccountAPIManipulator.class.getDeclaredMethod("makeJsonObject", ResultSet.class);
		method.setAccessible(true);

		ResultSet s = mock(ResultSet.class);

		when(s.getString("UUID")).thenReturn("ID");
		when(s.getString("Name")).thenReturn("NAME");
		when(s.getString("Balance")).thenReturn("0.0");
		when(s.getBoolean("roundUpEnabled")).thenReturn(false);

		JsonObject out = (JsonObject) method.invoke(manipulator, s);
		JsonObject out_err = (JsonObject) method.invoke(manipulator, (Object) null);

		assertNotNull(out);
		assertEquals("ID", out.get("id").getAsString());
		assertEquals("NAME", out.get("name").getAsString());
		assertEquals(BigDecimal.valueOf(0.00), out.get("startingBalance").getAsBigDecimal());
		assertFalse(out.get("roundUpEnabled").getAsBoolean());

		// Forcing Exception by passing null ResultSet
		assertNull(out_err);
	}

	@Test
	public void checkGetTableQuery() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		AccountAPIManipulator manipulator = new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class));

		Method method = AccountAPIManipulator.class.getDeclaredMethod("getTableQuery");
		method.setAccessible(true);

		String query = (String) method.invoke(manipulator);

		assertNotNull(query);
		assertEquals("SELECT * FROM Accounts", query);
	}

	@Test
	public void checkCreateJsonMap() {
		AccountAPIManipulator manipulator = new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class));
		JsonObject obj = new JsonObject();

		obj.addProperty("id", "ID");
		obj.addProperty("name", "NAME");
		obj.addProperty("startingBalance", BigDecimal.valueOf(0.00));
		obj.addProperty("roundUpEnabled", false);

		Map<String, String> map = manipulator.createJsonMap(obj);

		assertNotNull(map);
		assertEquals("ID", map.get("uuid"));
		assertEquals("NAME", map.get("name"));
		assertEquals("0.0", map.get("bal"));
		assertEquals("No", map.get("round"));
	}

	@Test
	public void checkJsonToAccounts() {
		// Setup
		AccountAPIManipulator manip = spy(new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class)));

		JsonArray arr = new JsonArray();
		JsonObject obj = new JsonObject();

		obj.addProperty("id", "ID");
		obj.addProperty("name", "NAME");
		obj.addProperty("startingBalance", BigDecimal.valueOf(0.00));
		obj.addProperty("roundUpEnabled", false);

		arr.add(obj);

		doReturn(arr).when(manip).getApiInformation();

		ArrayList<Account> accounts = manip.jsonToAccounts();

		assertNotNull(accounts);
		assertEquals(1, accounts.size());

		Account a = accounts.getFirst();

		assertNotNull(a);
		assertEquals("ID", a.getID());
		assertEquals("NAME", a.getName());
		assertEquals(0.00, a.getBalance().doubleValue());
	}
}