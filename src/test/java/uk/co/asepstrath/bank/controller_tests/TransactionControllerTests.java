package uk.co.asepstrath.bank.controller_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jooby.test.JoobyTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import uk.co.asepstrath.bank.App;
import uk.co.asepstrath.bank.Transaction;
import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;
import uk.co.asepstrath.bank.view.TransactionController;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JoobyTest(App.class)
public class TransactionControllerTests {
	static OkHttpClient client = new OkHttpClient();

	@Test
	public void checkTransactionObjects(int serverPort) {
		TransactionAPIManipulator mockManip = mock(TransactionAPIManipulator.class);
		TransactionController control = new TransactionController(mock(Logger.class), null);
		control.setTransactionAPIManipulator(mockManip);

		// Create fake data to test against
		ArrayList<Transaction> mockTransactions = new ArrayList<>();
		mockTransactions.add(new Transaction("TIMESTAMP", BigDecimal.ZERO, "SENDER", "ID", "RECIPIENT", "TYPE"));
		when(mockManip.jsonToTransactions()).thenReturn(mockTransactions);

		// Check raw output
		String transactionData = control.transactionObjects();

		assertNotNull(transactionData);
		assertTrue(transactionData.contains("TIMESTAMP"));
		assertTrue(transactionData.contains("0"));
		assertTrue(transactionData.contains("SENDER"));
		assertTrue(transactionData.contains("ID"));
		assertTrue(transactionData.contains("RECIPIENT"));
		assertTrue(transactionData.contains("TYPE"));

		// Check HTTP output
		Request req = new Request.Builder()
				.url("http://localhost:"+serverPort+"/transactions/transaction-objects")
				.build();

		try(Response rsp = client.newCall(req).execute()) {
			assertNotNull(rsp.body());

			// ! NEEDS FIXED
//			assertTrue(rsp.body().string().contains("TIMESTAMP"));
//			assertTrue(rsp.body().string().contains("0"));
//			assertTrue(rsp.body().string().contains("SENDER"));
//			assertTrue(rsp.body().string().contains("ID"));
//			assertTrue(rsp.body().string().contains("RECIPIENT"));
//			assertTrue(rsp.body().string().contains("TYPE"));
		} catch(Exception ignored) {}
	}

	@Test
	public void checkTransactionObject(int serverPort) {
		// Mock manipulator & insert into controller
		TransactionAPIManipulator manip = mock(TransactionAPIManipulator.class);
		TransactionController control = new TransactionController(mock(Logger.class), null);
		control.setTransactionAPIManipulator(manip);

		// Create fake data to test against
		ArrayList<Transaction> mockTransactions = new ArrayList<>();
		mockTransactions.add(new Transaction("TIMESTAMP", BigDecimal.ZERO, "SENDER", "ID", "RECIPIENT", "TYPE"));
		when(manip.jsonToTransactions()).thenReturn(mockTransactions);

		// Check raw output
		String transactionData = control.transactionObject(0);

		assertNotNull(transactionData);

		assertTrue(transactionData.contains("TIMESTAMP"));
		assertTrue(transactionData.contains("0"));
		assertTrue(transactionData.contains("SENDER"));
		assertTrue(transactionData.contains("ID"));
		assertTrue(transactionData.contains("RECIPIENT"));
		assertTrue(transactionData.contains("TYPE"));

		// Check HTTP output
		Request req = new Request.Builder()
				.url("http://localhost:"+serverPort+"/transactions/transaction-object?pos=0")
				.build();

		try(Response rsp = client.newCall(req).execute()) {
			assertNotNull(rsp.body());

			// ! NEEDS FIXED
//			assertTrue(rsp.body().string().contains("TIMESTAMP"));
//			assertTrue(rsp.body().string().contains("0"));
//			assertTrue(rsp.body().string().contains("SENDER"));
//			assertTrue(rsp.body().string().contains("ID"));
//			assertTrue(rsp.body().string().contains("RECIPIENT"));
//			assertTrue(rsp.body().string().contains("TYPE"));
		} catch(Exception ignored) {}

		// Check for out of bounds pos

		// Check raw output
		assertNotNull(control.transactionObject(-1));

		// Check HTTP output
		Request err_req_1 = new Request.Builder()
				.url("http://localhost:"+serverPort+"/transactions/transaction-object?pos=-1")
				.build();

		try(Response rsp = client.newCall(err_req_1).execute()) {
			assertNotNull(rsp.body());

			// ! NEEDS FIXED
//			assertTrue(rsp.body().string().contains("400"));
//			assertTrue(rsp.body().string().contains("Bad Request"));

		} catch(Exception ignored) {}

		// Check for out of bounds pos 2

		// Check raw output
		assertNotNull(control.transactionObject(1000));

		// Check HTTP output
		Request err_req_2 = new Request.Builder()
				.url("http://localhost:"+serverPort+"/transactions/transaction-object?pos=1000")
				.build();

		try(Response rsp = client.newCall(err_req_2).execute()) {
			assertNotNull(rsp.body());

			// ! NEEDS FIXED
//			assertTrue(rsp.body().string().contains("400"));
//			assertTrue(rsp.body().string().contains("Bad Request"));
		} catch(Exception ignored) {}
	}

	@Test
	public void checkGetTransactions(int serverPort) {
		// Mock manipulator & Insert into controller
		TransactionAPIManipulator manip = mock(TransactionAPIManipulator.class);
		TransactionController control = new TransactionController(mock(Logger.class), null);
		control.setTransactionAPIManipulator(manip);

		// Fake data to test
		JsonArray arr = new JsonArray();
		JsonObject obj = new JsonObject();

		obj.addProperty("timestamp", "TIMESTAMP");
		obj.addProperty("amount", BigDecimal.ZERO);
		obj.addProperty("sender", "SENDER");
		obj.addProperty("id", "UNIQUEID");
		obj.addProperty("recipient", "RECIPIENT");
		obj.addProperty("type", "TYPE");

		arr.add(obj);

		when(manip.getApiInformation()).thenReturn(arr);

		// Check raw output
		assertNotNull(control.getTransactions());

		// Check HTTP output
		Request req = new Request.Builder()
				.url("http://localhost:"+serverPort+"/transactions/transaction-view")
				.build();

		try(Response rsp = client.newCall(req).execute()) {
			assertNotNull(rsp.body());

			// ! NEEDS FIXED
//			assertTrue(rsp.body().string().contains("TIMESTAMP"));
//			assertTrue(rsp.body().string().contains("0"));
//			assertTrue(rsp.body().string().contains("SENDER"));
//			assertTrue(rsp.body().string().contains("RECIPIENT"));
//			assertTrue(rsp.body().string().contains("TYPE"));

		} catch(Exception ignored) {}

	}

	@Test
	public void checkGetTransaction(int serverPort) {
		// Mock manipulator & insert into controller
		TransactionAPIManipulator manip = mock(TransactionAPIManipulator.class);
		TransactionController control = new TransactionController(mock(Logger.class), null);
		control.setTransactionAPIManipulator(manip);

		// Fake data to test
		JsonArray arr = new JsonArray();
		JsonObject obj = new JsonObject();

		obj.addProperty("timestamp", "TIMESTAMP");
		obj.addProperty("amount", BigDecimal.ZERO);
		obj.addProperty("sender", "SENDER");
		obj.addProperty("id", "UNIQUEID");
		obj.addProperty("recipient", "RECIPIENT");
		obj.addProperty("type", "TYPE");

		arr.add(obj);

		when(manip.getApiInformation()).thenReturn(arr);

		// Check raw output
		assertNotNull(control.getTransaction("UNIQUEID"));

		// Check HTTP output
		Request req = new Request.Builder()
				.url("http://localhost:"+serverPort+"/businesses/business?uuid=UNIQUEID")
				.build();

		try(Response rsp = client.newCall(req).execute()) {
			assertNotNull(rsp.body());

			// ! NEEDS FIXED
//			assertTrue(rsp.body().string().contains("TIMESTAMP"));
//			assertTrue(rsp.body().string().contains("0"));
//			assertTrue(rsp.body().string().contains("SENDER"));
//			assertTrue(rsp.body().string().contains("UNIQUEID"));
//			assertTrue(rsp.body().string().contains("RECIPIENT"));
//			assertTrue(rsp.body().string().contains("TYPE"));

		} catch(Exception ignored) {}

		// Wrong transaction name

		// Check raw output
		assertNotNull(control.getTransaction("ALO"));

		// Check HTTP output
		Request err_req_1 = new Request.Builder()
				.url("http://localhost:"+serverPort+"/transactions/transaction?name=Aloo")
				.build();

		try(Response rsp = client.newCall(err_req_1).execute()) {
			assertNotNull(rsp.body());

			// ! NEEDS FIXED
//			assertTrue(rsp.body().string().contains("404"));
//			assertTrue(rsp.body().string().contains("Not Found"));

		} catch(Exception ignored) {}

		// No uuid param

		assertNotNull(control.getTransaction(null));

		Request err_req_2 = new Request.Builder()
				.url("http://localhost:"+serverPort+"/transactions/transaction?uuid=")
				.build();

		try(Response rsp = client.newCall(err_req_2).execute()) {
			assertNotNull(rsp.body());

			// ! NEEDS FIXED
//			assertTrue(rsp.body().string().contains("400"));
//			assertTrue(rsp.body().string().contains("Bad Request"));

		} catch(Exception ignored) {}


	}
}