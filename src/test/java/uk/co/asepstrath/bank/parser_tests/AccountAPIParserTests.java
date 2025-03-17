package uk.co.asepstrath.bank.parser_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.parsers.AccountAPIParser;

import javax.sql.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AccountAPIParserTests {

	@Test
	public void checkWriteAPIInformationAccounts() {
		try {
			// Make mock data source to write to
			DataSource mockDataSource = mock(DataSource.class);

			// Make parser & write to mock source
			AccountAPIParser parser = new AccountAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/accounts", mockDataSource);
			parser.writeAPIInformation();

			// Create manipulator to test data pass through
			AccountAPIManipulator manip = new AccountAPIManipulator(mock(Logger.class), mockDataSource);

			// Get that the API info is retrieved from the mock db properly
			assertNotNull(manip.getApiInformation());

			// Check an example JsonObject for its contents
			JsonObject obj = manip.getApiInformation().get(0).getAsJsonObject();

			assertEquals("Melva Rogahn", obj.get("name").toString());
			assertEquals("04f6ab33-8208-4234-aabd-b6a8be8493da", obj.get("uuid").toString());
			assertEquals(594.82, obj.get("balance").getAsDouble());
			assertFalse(obj.get("roundUpEnabled").getAsBoolean());
		} catch (Exception ignored) {}
	}

	@Test
	public void checkGetInsertQuery() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		AccountAPIParser parser = new AccountAPIParser(mock(Logger.class), "", mock(DataSource.class));

		Method method = AccountAPIParser.class.getDeclaredMethod("getInsertQuery");
		method.setAccessible(true);

		String query = (String) method.invoke(parser);

		assertNotNull(query);
		assertEquals("INSERT INTO Accounts (UUID, Name, Balance, roundUpEnabled) VALUES (?, ?, ?, ?)", query);
	}

	@Test
	public void checkParseResponse() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		AccountAPIParser parser = new AccountAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/accounts", mock(DataSource.class));

		Method method = AccountAPIParser.class.getDeclaredMethod("parseResponse");
		method.setAccessible(true);

		JsonArray json = (JsonArray) method.invoke(parser);

		assertNotNull(json);

		JsonObject obj = json.get(0).getAsJsonObject();

		assertNotNull(obj);

		// Trigger catch statement
		AccountAPIParser err_parser = new AccountAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mock(DataSource.class));

		JsonArray result = (JsonArray) method.invoke(err_parser);

		assertNull(result);
	}
}