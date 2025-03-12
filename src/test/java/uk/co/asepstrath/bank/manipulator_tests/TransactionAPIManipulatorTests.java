package uk.co.asepstrath.bank.manipulator_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;

import uk.co.asepstrath.bank.Transaction;
import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;

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
import static org.mockito.Mockito.doReturn;

public class TransactionAPIManipulatorTests {

	@Test
	public void testTransactionAPIManipulator() {
		TransactionAPIManipulator manip = new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class));
		assertNotNull(manip);
	}

	@Test
	public void checkMakeJsonObject() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, SQLException {
		TransactionAPIManipulator manipulator = new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class));

		Method method = TransactionAPIManipulator.class.getDeclaredMethod("makeJsonObject", ResultSet.class);
		method.setAccessible(true);

		ResultSet s = mock(ResultSet.class);

		when(s.getString("Timestamp")).thenReturn("TIMESTAMP");
		when(s.getDouble("Amount")).thenReturn(0.00);
		when(s.getString("Sender")).thenReturn("SENDER");
		when(s.getString("TransactionID")).thenReturn("TRANSACTIONID");
		when(s.getString("Recipient")).thenReturn("RECIPIENT");
		when(s.getString("Type")).thenReturn("TYPE");

		JsonObject out = (JsonObject) method.invoke(manipulator, s);
		JsonObject out_err = (JsonObject) method.invoke(manipulator, (Object) null);

		assertNotNull(out);
		assertEquals("TIMESTAMP", out.get("timestamp").getAsString());
		assertEquals(0.00, out.get("amount").getAsDouble());
		assertEquals("SENDER", out.get("sender").getAsString());
		assertEquals("TRANSACTIONID", out.get("id").getAsString());
		assertEquals("RECIPIENT", out.get("recipient").getAsString());
		assertEquals("TYPE", out.get("type").getAsString());

		// Forcing Exception by passing null ResultSet
		assertNull(out_err);
	}

	@Test
	public void checkGetTableQuery() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		TransactionAPIManipulator manipulator = new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class));

		Method method = TransactionAPIManipulator.class.getDeclaredMethod("getTableQuery");
		method.setAccessible(true);

		String query = (String) method.invoke(manipulator);

		assertNotNull(query);
		assertEquals("SELECT * FROM Transactions", query);
	}

	@Test
	public void checkCreateJsonMap() {
		TransactionAPIManipulator manip = new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class));

		JsonObject obj = new JsonObject();

		obj.addProperty("timestamp", "TIMESTAMP");
		obj.addProperty("amount", BigDecimal.valueOf(0.00));
		obj.addProperty("sender", "SENDER");
		obj.addProperty("id", "TRANSACTIONID");
		obj.addProperty("recipient", "RECIPIENT");
		obj.addProperty("type", "TYPE");

		Map<String, Object> map = manip.createJsonMap(obj);

		assertNotNull(map);
		assertEquals("TIMESTAMP", map.get("timestamp"));
		assertEquals("0.0", map.get("amount"));
		assertEquals("SENDER", map.get("sender"));
		assertEquals("TRANSACTIONID", map.get("id"));
		assertEquals("RECIPIENT", map.get("recipient"));
		assertEquals("TYPE", map.get("type"));
	}

	@Test
	public void checkJsonToTransactions() {
		// Setup
		TransactionAPIManipulator manip = spy(new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class)));

		JsonArray arr = new JsonArray();
		JsonObject obj = new JsonObject();

		obj.addProperty("timestamp", "TIMESTAMP");
		obj.addProperty("amount", BigDecimal.valueOf(0.00));
		obj.addProperty("sender", "SENDER");
		obj.addProperty("id", "TRANSACTIONID");
		obj.addProperty("recipient", "RECIPIENT");
		obj.addProperty("type", "TYPE");

		arr.add(obj);

		doReturn(arr).when(manip).getApiInformation();

		ArrayList<Transaction> transactions = manip.jsonToTransactions();

		assertNotNull(transactions);
		assertEquals(1, transactions.size());

		Transaction t = transactions.getFirst();

		assertNotNull(t);
		assertEquals("TIMESTAMP", t.getTimestamp());
		assertEquals(BigDecimal.valueOf(0.00), t.getAmount());
		assertEquals("SENDER", t.getSender());
		assertEquals("TRANSACTIONID", t.getID());
		assertEquals("RECIPIENT", t.getRecipient());
		assertEquals("TYPE", t.getType());
	}
}
