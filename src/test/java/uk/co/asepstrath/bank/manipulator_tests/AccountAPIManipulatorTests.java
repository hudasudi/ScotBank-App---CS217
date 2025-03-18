package uk.co.asepstrath.bank.manipulator_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountAPIManipulatorTests {

	/** Testing whether manipulator instantiation works as expected.
	 * Expected output: manipulator not null
	*/
	@Test
	public void testAccountAPIManipulator() {
		AccountAPIManipulator manipulator = new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class));
		assertNotNull(manipulator);
	}

	/** Testing whether makeJsonObject() works as expected, also checking for null output on passing in a null ResultSet.
	 * Expected normal output: out has values ID, NAME, 0 & False.
	 * Expected error output: out_err is null.
	*/
	@Test
	public void checkMakeJsonObject() {
		try {
			// Make manipulator class
			AccountAPIManipulator manipulator = new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class));

			// Get method & set it as accessible as its protected normally
			Method method = AccountAPIManipulator.class.getDeclaredMethod("makeJsonObject", ResultSet.class);
			method.setAccessible(true);

			// Mock ResultSet
			ResultSet s = mock(ResultSet.class);

			// When get{X} is called, return our dummy information
			when(s.getString("UUID")).thenReturn("ID");
			when(s.getString("Name")).thenReturn("NAME");
			when(s.getBigDecimal("Balance")).thenReturn(BigDecimal.ZERO);
			when(s.getBoolean("roundUpEnabled")).thenReturn(false);

			// Get both our normal & error output
			JsonObject out = (JsonObject) method.invoke(manipulator, s);
			JsonObject out_err = (JsonObject) method.invoke(manipulator, (Object) null);

			// Test normal output for dummy values
			assertNotNull(out);
			assertEquals("ID", out.get("id").getAsString());
			assertEquals("NAME", out.get("name").getAsString());
			assertEquals(0, out.get("startingBalance").getAsBigDecimal().compareTo(BigDecimal.ZERO));
			assertFalse(out.get("roundUpEnabled").getAsBoolean());

			// Forcing Exception by passing null ResultSet
			assertNull(out_err);
		}

		catch(Exception e) {
			throw new AssertionError("checkMakeJsonObject() Failed", e);
		}
	}

	/** Testing whether createAccountMap() works as expected, also checking for null output on passing in a null Account.
	 * Expected normal output: map has values ID, NAME, 0 & No.
	 * Expected error output: a_err is null.
	*/
	@Test
	public void checkCreateAccountMap() {
		AccountAPIManipulator manipulator = new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class));

		// Test account object
		Account a = new Account("ID", "NAME", BigDecimal.ZERO, false);

		// Get method output
		Map<String, Object> map = manipulator.createAccountMap(a);

		// Check for values in normal output
		assertNotNull(map);
		assertEquals("ID", map.get("uuid"));
		assertEquals("NAME", map.get("name"));
		assertEquals(0, new BigDecimal(String.valueOf(map.get("bal"))).compareTo(BigDecimal.ZERO));
        assertEquals("No", map.get("round"));

		// Check for null on error output
		Map<String, Object> err_map = manipulator.createAccountMap(null);

		assertNull(err_map);
	}

	/** Testing whether getTableQuery() works as expected.
	 * Expected output: String of "SELECT * FROM Accounts"
	*/
	@Test
	public void checkGetTableQuery() {
		try {
			// Make manipulator class
			AccountAPIManipulator manipulator = new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class));

			// Get method & make it accessible as its protected otherwise
			Method method = AccountAPIManipulator.class.getDeclaredMethod("getTableQuery");
			method.setAccessible(true);

			// Get method output
			String query = (String) method.invoke(manipulator);

			// Check method output for correct information
			assertNotNull(query);
			assertEquals("SELECT * FROM Accounts", query);
		}

		catch(Exception e) {
			throw new AssertionError("checkGetTableQuery() Failed", e);
		}
	}

	/** Testing whether createJsonMap() works as expected, also checking for null output on passing in a null JsonObject.
	 * Expected normal output: map has values ID, NAME, 0 & No.
	 * Expected error output: err_map is null.
	*/
	@Test
	public void checkCreateJsonMap() {
		AccountAPIManipulator manipulator = new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class));

		// Make dummy object to insert
		JsonObject obj = new JsonObject();

		obj.addProperty("id", "ID");
		obj.addProperty("name", "NAME");
		obj.addProperty("startingBalance", BigDecimal.ZERO);
		obj.addProperty("roundUpEnabled", false);

		// Get method output
		Map<String, Object> map = manipulator.createJsonMap(obj);

		// Check normal output
		assertNotNull(map);
		assertEquals("ID", map.get("uuid"));
		assertEquals("NAME", map.get("name"));
		assertEquals(0, new BigDecimal(String.valueOf(map.get("bal"))).compareTo(BigDecimal.ZERO));
		assertEquals("No", map.get("round"));

		// Check error output
		Map<String, Object> err_map = manipulator.createJsonMap(null);

		assertNull(err_map);
	}

	/** Testing whether checkJsonToAccounts() works as expected, making sure elements are processed correctly.
	 * Expected output: Account a with values ID, NAME, 0 & False.
	*/
	@Test
	public void checkJsonToAccounts() {
		// Setup
		// NOTE: We spy so we can watch what it does and insert info where we need it (rather than mock, spy will let the class run as usual)
		AccountAPIManipulator manip = spy(new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class)));

		JsonArray arr = new JsonArray();

		// Make mock JsonObject
		JsonObject obj = new JsonObject();

		obj.addProperty("id", "ID");
		obj.addProperty("name", "NAME");
		obj.addProperty("startingBalance", BigDecimal.valueOf(0.00));
		obj.addProperty("roundUpEnabled", false);

		arr.add(obj);

		// When manip.getApiInformation() return our array
		doReturn(arr).when(manip).getApiInformation();

		// Get the output from the method
		ArrayList<Account> accounts = manip.jsonToAccounts();

		// Do checks to make sure it is real and at least size 1
		assertNotNull(accounts);
		assertEquals(1, accounts.size());

		// Get 1st account object
		Account a = accounts.getFirst();

		// Do checks on account object
		assertNotNull(a);
		assertEquals("ID", a.getID());
		assertEquals("NAME", a.getName());
		assertEquals(0, a.getBalance().compareTo(BigDecimal.ZERO));
		assertFalse(a.isRoundUpEnabled());
	}

	/** Testing whether getAccountByUUID() works as expected, making sure we get the right Account object.
	 * Expected output: Account a with values ID, NAME, 0 & False.
	*/
	@Test
	public void checkGetAccountByUUID() {
		// Spy on manipulator to insert values
		AccountAPIManipulator manip = spy(new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class)));

		// Make fake accounts array
		ArrayList<Account> accounts = new ArrayList<>();
		accounts.add(new Account("ID", "NAME", BigDecimal.ZERO, false));

		// Insert accounts array
		doReturn(accounts).when(manip).jsonToAccounts();

		// Get method output
		Account a = manip.getAccountByUUID("ID");

		// Do checks on output
		assertNotNull(a);
		assertEquals("ID", a.getID());
		assertEquals("NAME", a.getName());
		assertEquals(0, a.getBalance().compareTo(BigDecimal.ZERO));
		assertFalse(a.isRoundUpEnabled());
	}
}