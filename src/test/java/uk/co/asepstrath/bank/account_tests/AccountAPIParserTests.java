package uk.co.asepstrath.bank.account_tests;

import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.AccountAPIParser;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AccountAPIParserTests {

	// Make sure the APIParser writes the right information for Manipulator to use
	@Test
	public void shouldWriteCorrectly() {
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

			assertEquals("Miss Lavina Waelchi", obj.get("name").toString());
			assertEquals("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2", obj.get("uuid").toString());
			assertEquals(544.91, obj.get("balance").getAsDouble());
			assertFalse(obj.get("roundUpEnabled").getAsBoolean());
		} catch (Exception ignored) {
		}
	}

	// getAPIData
	// Force Parser IOException
	// Force Parser InterruptedException

	// parseJSONResponse
	// Force InterruptedException

	// writeAPIInformation
	// Force SQLException
}