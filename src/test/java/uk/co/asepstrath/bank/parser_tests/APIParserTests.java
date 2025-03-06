package uk.co.asepstrath.bank.parser_tests;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import uk.co.asepstrath.bank.api.parsers.APIParser;
import uk.co.asepstrath.bank.api.parsers.AccountAPIParser;

import javax.sql.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class APIParserTests {

	// Using any subclass of APIParser works fine for testing
	@Test
	public void checkCreateRequest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		AccountAPIParser parser = new AccountAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/accounts", mock(DataSource.class));

		Method method = APIParser.class.getDeclaredMethod("createRequest");
		method.setAccessible(true);

		HttpRequest result = (HttpRequest) method.invoke(parser);

		assertNotNull(result);
	}

	@Test
	public void checkGetAPIData() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		AccountAPIParser parser = new AccountAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/accounts", mock(DataSource.class));

		Method method = APIParser.class.getDeclaredMethod("getAPIData");
		method.setAccessible(true);

		@SuppressWarnings("unchecked")
		HttpResponse<String> result = (HttpResponse<String>) method.invoke(parser);

		assertNotNull(result);

		// Load parser with malformed URL
		AccountAPIParser err_parser = new AccountAPIParser(mock(Logger.class), "https://www.example", mock(DataSource.class));

		@SuppressWarnings("unchecked")
		HttpResponse<String> err_result = (HttpResponse<String>) method.invoke(err_parser);

		// Check for catch
		assertNull(err_result);
	}

	@Test
	public void checkGetResponseString() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		AccountAPIParser parser = new AccountAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/accounts", mock(DataSource.class));

		Method method = APIParser.class.getDeclaredMethod("getResponseString");
		method.setAccessible(true);

		String result = (String) method.invoke(parser);

		assertNotNull(result);

		AccountAPIParser err_parser = new AccountAPIParser(mock(Logger.class), "https://www.example", mock(DataSource.class));

		String err_result = (String) method.invoke(err_parser);

		// Check for catch
		assertNull(err_result);
	}
}