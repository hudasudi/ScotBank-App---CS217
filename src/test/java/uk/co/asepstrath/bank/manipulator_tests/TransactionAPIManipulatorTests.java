package uk.co.asepstrath.bank.manipulator_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Transaction;
import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

public class TransactionAPIManipulatorTests {

	/** Testing whether class instantiation works as intended */
	@Test
	public void testTransactionAPIManipulator() {
		TransactionAPIManipulator manip = new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class));
		assertNotNull(manip);
	}

	/** Testing whether makeJsonObject() works as intended.
	 * Expected normal output: out with values TIMESTAMP, 0, SENDER, TRANSACTIONID, RECIPIENT & TYPE
	 * Expected error output: out_err is null
	*/
	@Test
	public void checkMakeJsonObject() {
        try {
            // Make class
            TransactionAPIManipulator manipulator = new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class));

            // Set method to accessible
            Method method = TransactionAPIManipulator.class.getDeclaredMethod("makeJsonObject", ResultSet.class);
            method.setAccessible(true);

            // Insert our dummy information on method call
            ResultSet s = mock(ResultSet.class);

            when(s.getString("Timestamp")).thenReturn("TIMESTAMP");
            when(s.getDouble("Amount")).thenReturn(0.00);
            when(s.getString("Sender")).thenReturn("SENDER");
            when(s.getString("TransactionID")).thenReturn("TRANSACTIONID");
            when(s.getString("Recipient")).thenReturn("RECIPIENT");
            when(s.getString("Type")).thenReturn("TYPE");

            // Get normal & error output
            JsonObject out = (JsonObject) method.invoke(manipulator, s);
            JsonObject out_err = (JsonObject) method.invoke(manipulator, (Object) null);

            // Check normal output
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

		catch(Exception e) {
			throw new AssertionError("checkMakeJsonObject() Failed", e);
		}
    }

	/** Testing whether getTableQuery() works as intended.
	 * Expected normal output: String with value "SELECT * FROM Transactions".
	*/
	@Test
	public void checkGetTableQuery() {
		try {
			// Make class
			TransactionAPIManipulator manipulator = new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class));

			// Set method to accessible
			Method method = TransactionAPIManipulator.class.getDeclaredMethod("getTableQuery");
			method.setAccessible(true);

			// Get output
			String query = (String) method.invoke(manipulator);

			// Check normal output
			assertNotNull(query);
			assertEquals("SELECT * FROM Transactions", query);
		}

		catch(Exception e) {
			throw new AssertionError("checkGetTableQuery() Failed", e);
		}
	}

	/** Testing whether makeTransaction() works as intended.
	 * Expected normal output: Transaction with values TIMESTAMP, 0, SENDER, TRANSACTIONID, RECIPIENT & TYPE.
	 * Expected error output: Transaction is null
	*/
	@Test
	public void checkMakeTransaction() {
		try {
			// Make class
			TransactionAPIManipulator manipulator = new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class));

			// Set method to accessible
			Method method = TransactionAPIManipulator.class.getDeclaredMethod("makeTransaction", ResultSet.class);
			method.setAccessible(true);

			// Insert our dummy information on method call
			ResultSet s = mock(ResultSet.class);

			when(s.getString("Timestamp")).thenReturn("TIMESTAMP");
			when(s.getBigDecimal("Amount")).thenReturn(BigDecimal.ZERO);
			when(s.getString("Sender")).thenReturn("SENDER");
			when(s.getString("TransactionID")).thenReturn("TRANSACTIONID");
			when(s.getString("Recipient")).thenReturn("RECIPIENT");
			when(s.getString("Type")).thenReturn("TYPE");

			// Get normal & error output
			Transaction out = (Transaction) method.invoke(manipulator, s);
			Transaction out_err = (Transaction) method.invoke(manipulator, (Object) null);

			// Check normal output
			assertNotNull(out);
			assertEquals("TIMESTAMP", out.getTimestamp());
			assertEquals(BigDecimal.ZERO, out.getAmount());
			assertEquals("SENDER", out.getSender());
			assertEquals("TRANSACTIONID", out.getID());
			assertEquals("RECIPIENT", out.getRecipient());
			assertEquals("TYPE", out.getType());

			// Forcing Exception by passing null ResultSet
			assertNull(out_err);
		}

		catch(Exception e) {
			throw new AssertionError("checkMakeTransaction() Failed", e);
		}
	}

	/** Testing whether createJsonMap() works as intended.
	 * Expected normal output: map with values TIMESTAMP, 0.0, SENDER, TRANSACTIONID, RECIPIENT & TYPE.
	 * Expected error output: err_map is null
	*/
	@Test
	public void checkCreateJsonMap() {
		// Make class
		TransactionAPIManipulator manip = new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class));

		// Make dummy information
		JsonObject obj = new JsonObject();

		obj.addProperty("timestamp", "TIMESTAMP");
		obj.addProperty("amount", BigDecimal.valueOf(0.00));
		obj.addProperty("sender", "SENDER");
		obj.addProperty("id", "TRANSACTIONID");
		obj.addProperty("recipient", "RECIPIENT");
		obj.addProperty("type", "TYPE");

		// Get output
		Map<String, Object> map = manip.createJsonMap(obj);

		// Check output
		assertNotNull(map);
		assertEquals("TIMESTAMP", map.get("timestamp"));
		assertEquals("0.0", map.get("amount"));
		assertEquals("SENDER", map.get("sender"));
		assertEquals("TRANSACTIONID", map.get("id"));
		assertEquals("RECIPIENT", map.get("recipient"));
		assertEquals("TYPE", map.get("type"));

		// Get error output
		Map<String, Object> err_map = manip.createJsonMap(null);

		// Check error output
		assertNull(err_map);
	}

	/** Testing whether createTransactionMap() works as intended.
	 * Expected normal output: Map with values TIMESTAMP, 0, SENDER, TRANSACTIONID, RECIPIENT, TYPE
	 * Expected withdraw output: Same as above but Sender = Branch Deposit
	 * Expected deposit output: Same as above but Recipient = ATM
	 * Expected error output: map is null
	*/
	@Test
	public void checkCreateTransactionMap() {
		// Make class
		TransactionAPIManipulator manip = new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class));

		// Make dummy information
		Transaction transaction = new Transaction(
				"TIMESTAMP",
				BigDecimal.ZERO,
				"SENDER",
				"TRANSACTIONID",
				"RECIPIENT",
				"TYPE"
		);

		Transaction deposit_transaction = new Transaction(
				"TIMESTAMP",
				BigDecimal.ZERO,
				null,
				"TRANSACTIONID",
				"RECIPIENT",
				"TYPE"
		);

		Transaction withdraw_transaction = new Transaction(
				"TIMESTAMP",
				BigDecimal.ZERO,
				"SENDER",
				"TRANSACTIONID",
				null,
				"TYPE"
		);

		// Get output
		Map<String, Object> map = manip.createTransactionMap(transaction);
		Map<String, Object> dep_map = manip.createTransactionMap(deposit_transaction);
		Map<String, Object> with_map = manip.createTransactionMap(withdraw_transaction);

		// Check outputs

		// Normal
		assertNotNull(map);
		assertEquals("TIMESTAMP", map.get("timestamp"));
		assertEquals(0, new BigDecimal(String.valueOf(map.get("amount"))).compareTo(BigDecimal.ZERO));
		assertEquals("SENDER", map.get("sender"));
		assertEquals("TRANSACTIONID", map.get("id"));
		assertEquals("RECIPIENT", map.get("recipient"));
		assertEquals("TYPE", map.get("type"));
		assertEquals("DECLINED", map.get("processed"));

		// Deposit
		assertNotNull(dep_map);
		assertEquals("TIMESTAMP", dep_map.get("timestamp"));
		assertEquals(0, new BigDecimal(String.valueOf(dep_map.get("amount"))).compareTo(BigDecimal.ZERO));
		assertEquals("Branch Deposit", dep_map.get("sender"));
		assertEquals("TRANSACTIONID", dep_map.get("id"));
		assertEquals("RECIPIENT", dep_map.get("recipient"));
		assertEquals("TYPE", dep_map.get("type"));
		assertEquals("DECLINED", dep_map.get("processed"));

		// Withdraw
		assertNotNull(with_map);
		assertEquals("TIMESTAMP", with_map.get("timestamp"));
		assertEquals(0, new BigDecimal(String.valueOf(map.get("amount"))).compareTo(BigDecimal.ZERO));
		assertEquals("SENDER", with_map.get("sender"));
		assertEquals("TRANSACTIONID", with_map.get("id"));
		assertEquals("ATM", with_map.get("recipient"));
		assertEquals("TYPE", with_map.get("type"));
		assertEquals("DECLINED", with_map.get("processed"));

		// Get error output
		Map<String, Object> err_map = manip.createTransactionMap(null);

		// Check error output
		assertNull(err_map);
	}

	/** Testing whether getTransactionforAccount() works as intended.
	 * Expected normal output: Transaction with values TIMESTAMP, 0, SENDER, TRANSACTIONID, RECIPIENT, TYPE.
	 * Expected error output: Transaction is null
	*/
	@Test
	public void checkGetTransactionForAccount() {
		try {
			// Setup insertion
			DataSource mockDataSource = mock(DataSource.class);
			Connection mockConnection = mock(Connection.class);
			PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
			ResultSet mockResultSet = mock(ResultSet.class);

			when(mockDataSource.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
			when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

			when(mockResultSet.next()).thenReturn(true).thenReturn(false);

			// insert information
			when(mockResultSet.getString("Timestamp")).thenReturn("TIMESTAMP");
			when(mockResultSet.getString("Sender")).thenReturn("SENDER");
			when(mockResultSet.getString("TransactionID")).thenReturn("TRANSACTIONID");
			when(mockResultSet.getString("Recipient")).thenReturn("RECIPIENT");
			when(mockResultSet.getString("Type")).thenReturn("TYPE");

			// make class
			TransactionAPIManipulator manip = new TransactionAPIManipulator(mock(Logger.class), mockDataSource);

			// Get list
			ArrayList<Transaction> output = manip.getTransactionForAccount("SENDER");

			// Check list
			assertNotNull(output);
			assertEquals(1, output.size());

			// Get output
			Transaction out = output.getFirst();

			// Check output
			assertNotNull(out);
			assertEquals("TIMESTAMP", out.getTimestamp());
			assertEquals("SENDER", out.getSender());
			assertEquals("TRANSACTIONID", out.getID());
			assertEquals("RECIPIENT", out.getRecipient());
			assertEquals("TYPE", out.getType());

			// Force error
			when(mockDataSource.getConnection()).thenThrow(new SQLException("error connecting to the database"));

			ArrayList<Transaction> err_output = manip.getTransactionForAccount("SENDER");

			assertNull(err_output);
		}

		catch(Exception e) {
			throw new AssertionError("checkGetTransactionForAccount() Failed", e);
		}
	}

	/** Testing whether getBalanceForAccount() works as intended.
	 * Expected normal output: Account a with balance 10 & Transaction with isProcessed() true
	*/
	@Test
	@SuppressWarnings("unchecked")
	public void checkGetBalanceForAccount() {
		TransactionAPIManipulator manip = spy(new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class)));

		// Insert our array list into method
		ArrayList<Transaction> mock_transactions = new ArrayList<>();
		mock_transactions.add(new Transaction(
				"TIMESTAMP",
				BigDecimal.valueOf(10),
				"SENDER",
				"ID",
				"RECIPIENT",
				"DEPOSIT"
		));

		doReturn(mock_transactions).when(manip).getTransactionForAccount(anyString());

		// make mock account
		Account a = new Account(
				"ID",
				"NAME",
				BigDecimal.ZERO,
				false
		);

		Map<String, Object> map = manip.getBalanceForAccount(a);

		assertNotNull(map);

		// Get output
		a = (Account) map.get("account");
		ArrayList<Transaction> out_transaction = (ArrayList<Transaction>) map.get("transactions");

		assertNotNull(out_transaction);
		assertEquals(1, out_transaction.size());

		// Get transaction output
		Transaction out = out_transaction.getFirst();

		// Check transaction
		assertNotNull(out);
		assertEquals("TIMESTAMP", out.getTimestamp());
		assertEquals("SENDER", out.getSender());
		assertEquals("ID", out.getID());
		assertEquals("RECIPIENT", out.getRecipient());
		assertEquals("DEPOSIT", out.getType());
		assertTrue(out.isProcessed());

		// Check account
		assertEquals("ID", a.getID());
		assertEquals("NAME", a.getName());
		assertEquals(BigDecimal.valueOf(10), out.getAmount());
		assertFalse(a.isRoundUpEnabled());
	}

	/** Testing whether jsonToTransactions() works as intended.
	 * Expected normal output: transaction with values TIMESTAMP, 0, SENDER, TRANSACTIONID, RECIPIENT, TYPE
	*/
	@Test
	public void checkJsonToTransactions() {
		// Make class
		TransactionAPIManipulator manip = spy(new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class)));

		JsonArray arr = new JsonArray();
		JsonObject obj = new JsonObject();

		// Make dummy information
		obj.addProperty("timestamp", "TIMESTAMP");
		obj.addProperty("amount", BigDecimal.valueOf(0.00));
		obj.addProperty("sender", "SENDER");
		obj.addProperty("id", "TRANSACTIONID");
		obj.addProperty("recipient", "RECIPIENT");
		obj.addProperty("type", "TYPE");

		arr.add(obj);

		// Return dummy info on method call
		doReturn(arr).when(manip).getApiInformation();

		// Get list of transactions
		ArrayList<Transaction> transactions = manip.jsonToTransactions();

		// Make sure transaction is there
		assertNotNull(transactions);
		assertEquals(1, transactions.size());

		// Get output
		Transaction t = transactions.getFirst();

		// Check output
		assertNotNull(t);
		assertEquals("TIMESTAMP", t.getTimestamp());
		assertEquals(BigDecimal.valueOf(0.00), t.getAmount());
		assertEquals("SENDER", t.getSender());
		assertEquals("TRANSACTIONID", t.getID());
		assertEquals("RECIPIENT", t.getRecipient());
		assertEquals("TYPE", t.getType());
	}
}
