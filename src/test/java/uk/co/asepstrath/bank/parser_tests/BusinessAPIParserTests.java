package uk.co.asepstrath.bank.parser_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;

import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;
import uk.co.asepstrath.bank.api.parsers.AccountAPIParser;
import uk.co.asepstrath.bank.api.parsers.BusinessAPIParser;

import javax.sql.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class BusinessAPIParserTests {

	@Test
	public void checkWriteAPIInformation() {
		try {
			// Make mock data source to write to
			DataSource mockDataSource = mock(DataSource.class);

			// Make parser & write to mock source
			BusinessAPIParser parser = new BusinessAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/businesses", mockDataSource);
			parser.writeAPIInformation();

			// Create manipulator to test data pass through
			BusinessAPIManipulator manip = new BusinessAPIManipulator(mock(Logger.class), mockDataSource);

			// Get that the API info is retrieved from the mock db properly
			assertNotNull(manip.getApiInformation());

			// Check an example JsonObject for its contents
			JsonObject obj = manip.getApiInformation().get(0).getAsJsonObject();

			assertEquals("ALD", obj.get("id").toString());
			assertEquals("Aldi", obj.get("name").toString());
			assertEquals("Groceries", obj.get("category").toString());
			assertFalse(obj.get("sanctioned").getAsBoolean());
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
	public void checkParseResponse() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		BusinessAPIParser parser = new BusinessAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/businesses", mock(DataSource.class));

		Method method = BusinessAPIParser.class.getDeclaredMethod("parseResponse");
		method.setAccessible(true);

		JsonArray json = (JsonArray) method.invoke(parser);

		assertNotNull(json);

		JsonObject obj = json.get(0).getAsJsonObject();

		assertEquals("ALD", obj.get("id").getAsString());
		assertEquals("Aldi", obj.get("name").getAsString());
		assertEquals("Groceries", obj.get("category").getAsString());
		assertFalse(obj.get("sanctioned").getAsBoolean());

		// Trigger catch statement
		BusinessAPIParser err_parser = new BusinessAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mock(DataSource.class));

		JsonArray result = (JsonArray) method.invoke(err_parser);

		assertTrue(result.isEmpty());
	}
}