package uk.co.asepstrath.bank.manipulator_tests;

import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.manipulators.APIManipulator;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class APIManipulatorTests {

	@Test
	public void testAPIManipulator() {
		APIManipulator manip = mock(APIManipulator.class);
		assertNotNull(manip);
	}

	// Using ANY subclass of APIManipulator will suffice
	@Test
	public void testGetApiInformation() throws SQLException {
		DataSource ds = mock(DataSource.class);

		when(ds.getConnection()).thenThrow(new SQLException("Unable to connect to the database"));

		BusinessAPIManipulator manip = new BusinessAPIManipulator(mock(Logger.class), ds);

		JsonArray result = manip.getApiInformation();

		assertNull(result);
	}
}