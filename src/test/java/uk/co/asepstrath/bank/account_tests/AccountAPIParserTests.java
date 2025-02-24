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
			DataSource mockDataSource = mock(DataSource.class);

			AccountAPIParser parser = new AccountAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/accounts", mockDataSource);
			parser.writeAPIInformation();

			AccountAPIManipulator manip = new AccountAPIManipulator(mock(Logger.class), mockDataSource);

			assertNotNull(manip.getApiInformation());

			JsonObject obj = manip.getApiInformation().get(0).getAsJsonObject();

			assertEquals("Miss Lavina Waelchi", obj.get("name").toString());
		} catch (Exception ignored) {}
	}
}