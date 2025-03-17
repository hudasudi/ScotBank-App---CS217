package uk.co.asepstrath.bank.parser_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;

import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;
import uk.co.asepstrath.bank.api.parsers.TransactionAPIParser;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class TransactionAPIParserTests {

	@Test
	public void checkWriteAPIInformationTransactions() {
		try {
			// Make mock data source to write to
			DataSource mockDataSource = mock(DataSource.class);

			// Make parser & write to mock source
			TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mockDataSource);
			parser.writeAPIInformation();

			// Create manipulator to test data pass through
			TransactionAPIManipulator manip = new TransactionAPIManipulator(mock(Logger.class), mockDataSource);

			// Get that the API info is retrieved from the mock db properly
			assertNotNull(manip.getApiInformation());

			// Check an example JsonObject for its contents
			JsonObject obj = manip.getApiInformation().get(0).getAsJsonObject();

			assertEquals("2023-04-10 08:43", obj.get("timestamp").toString());
			assertEquals("ae89778c-0e6e-4bf7-937f-462d66c55974", obj.get("sender").toString());
			assertEquals("026b53d4-990a-4373-a88d-e491de65489f", obj.get("id").toString());
			assertEquals("YAN", obj.get("recipient").toString());
			assertEquals("PAYMENT", obj.get("type").toString());
			assertEquals(BigDecimal.valueOf(48.00), obj.get("amount").getAsBigDecimal());
		} catch (Exception ignored) {}
	}

	@Test
	public void checkInsertQuery() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mock(DataSource.class));

		Method method = TransactionAPIParser.class.getDeclaredMethod("getInsertQuery");
		method.setAccessible(true);

		String query = (String) method.invoke(parser);

		assertNotNull(query);
		assertEquals("INSERT INTO Transactions (Timestamp, Amount, Sender, TransactionID, Recipient, Type) VALUES (?, ?, ?, ?, ?, ?)", query);
	}

	@Test
	public void checkCreateRequest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mock(DataSource.class));

		Method method = TransactionAPIParser.class.getDeclaredMethod("createRequest", int.class);
		method.setAccessible(true);

		HttpRequest result = (HttpRequest) method.invoke(parser, 1);

		assertNotNull(result);
	}

	@Test
	public void checkGetResponseStringForPage() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

	@Test
	public void checkGetAPIDataForPage() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mock(DataSource.class));

		Method method = TransactionAPIParser.class.getDeclaredMethod("getAPIDataForPage", int.class);
		method.setAccessible(true);

		@SuppressWarnings("unchecked")
		HttpResponse<String> result = (HttpResponse<String>) method.invoke(parser, 1);

		assertNotNull(result);
		assertEquals(200, result.statusCode());

		// Force catch statement
		TransactionAPIParser err_parser = new TransactionAPIParser(mock(Logger.class), "https://www.example", mock(DataSource.class));

		@SuppressWarnings("unchecked")
		HttpResponse<String> err_result = (HttpResponse<String>) method.invoke(err_parser, 1);

		assertNull(err_result);
	}

	@Test
	public void checkParseResponse() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
}