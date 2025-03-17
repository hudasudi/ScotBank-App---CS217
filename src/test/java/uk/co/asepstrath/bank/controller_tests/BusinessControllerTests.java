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
import uk.co.asepstrath.bank.Business;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;
import uk.co.asepstrath.bank.view.BusinessController;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JoobyTest(App.class)
public class BusinessControllerTests {
	static OkHttpClient client = new OkHttpClient();

	@Test
	public void checkBusinessObjects(int serverPort) {
		BusinessAPIManipulator mockManip = mock(BusinessAPIManipulator.class);
		BusinessController control = new BusinessController(mock(Logger.class), null);
		control.setBusinessAPIManipulator(mockManip);

		// Create fake data to test against
		ArrayList<Business> mockBusinesses = new ArrayList<>();
		mockBusinesses.add(new Business("ALD", "Aldi", "Groceries", false));
		when(mockManip.jsonToBusinesses()).thenReturn(mockBusinesses);

		// Check raw output
		String businessData = control.businessObjects();

		assertNotNull(businessData);
		assertTrue(businessData.contains("ALD"));
		assertTrue(businessData.contains("Aldi"));
		assertTrue(businessData.contains("Groceries"));

		// Check HTTP output
		Request req = new Request.Builder()
				.url("http://localhost:"+serverPort+"/businesses/business-objects")
				.build();

		try(Response rsp = client.newCall(req).execute()) {
			assertNotNull(rsp.body());
			assertTrue(rsp.body().string().contains("ALD"));
			assertTrue(rsp.body().string().contains("Aldi"));
			assertTrue(rsp.body().string().contains("Groceries"));
		} catch(Exception ignored) {}
	}

	@Test
	public void checkBusinessObject(int serverPort) {
		// Mock manipulator & insert into controller
		BusinessAPIManipulator manip = mock(BusinessAPIManipulator.class);
		BusinessController control = new BusinessController(mock(Logger.class), null);
		control.setBusinessAPIManipulator(manip);

		// Create fake data to test against
		ArrayList<Business> mockBusinesses = new ArrayList<>();
		mockBusinesses.add(new Business("ALD", "Aldi", "Groceries", false));
		when(manip.jsonToBusinesses()).thenReturn(mockBusinesses);

		// Check raw output
		String businessData = control.businessObject(0);

		assertNotNull(businessData);
		assertTrue(businessData.contains("ALD"));
		assertTrue(businessData.contains("Aldi"));
		assertTrue(businessData.contains("Groceries"));

		// Check HTTP output
		Request req = new Request.Builder()
				.url("http://localhost:"+serverPort+"/businesses/business-object?pos=0")
				.build();

		try(Response rsp = client.newCall(req).execute()) {
			assertNotNull(rsp.body());
			assertTrue(rsp.body().string().contains("ALD"));
			assertTrue(rsp.body().string().contains("Aldi"));
			assertTrue(rsp.body().string().contains("Groceries"));
		} catch(Exception ignored) {}

		// Check for out of bounds pos

		// Check raw output
		assertNotNull(control.businessObject(-1));

		// Check HTTP output
		Request err_req_1 = new Request.Builder()
				.url("http://localhost:"+serverPort+"/businesses/business-object?pos=-1")
				.build();

		try(Response rsp = client.newCall(err_req_1).execute()) {
			assertNotNull(rsp.body());

			assertTrue(rsp.body().string().contains("400"));
			assertTrue(rsp.body().string().contains("Bad Request"));

		} catch(Exception ignored) {}

		// Check for out of bounds pos 2

		// Check raw output
		assertNotNull(control.businessObject(1000));

		// Check HTTP output
		Request err_req_2 = new Request.Builder()
				.url("http://localhost:"+serverPort+"/businesses/business-object?pos=1000")
				.build();

		try(Response rsp = client.newCall(err_req_2).execute()) {
			assertNotNull(rsp.body());

			assertTrue(rsp.body().string().contains("400"));
			assertTrue(rsp.body().string().contains("Bad Request"));
		} catch(Exception ignored) {}
	}

	@Test
	public void checkGetBusinesses(int serverPort) {
		// Mock manipulator & Insert into controller
		BusinessAPIManipulator manip = mock(BusinessAPIManipulator.class);
		BusinessController control = new BusinessController(mock(Logger.class), null);
		control.setBusinessAPIManipulator(manip);

		// Fake data to test
		JsonArray arr = new JsonArray();
		JsonObject obj = new JsonObject();

		obj.addProperty("id", "ALD");
		obj.addProperty("name", "Aldi");
		obj.addProperty("category", "Groceries");
		obj.addProperty("sanctioned", false);

		arr.add(obj);

		when(manip.getApiInformation()).thenReturn(arr);

		// Check raw output
		assertNotNull(control.getBusinesses());

		// Check HTTP output
		Request req = new Request.Builder()
				.url("http://localhost:"+serverPort+"/businesses/business-view")
				.build();

		try(Response rsp = client.newCall(req).execute()) {
			assertNotNull(rsp.body());

			assertTrue(rsp.body().string().contains("ALD"));
			assertTrue(rsp.body().string().contains("Aldi"));
			assertTrue(rsp.body().string().contains("Groceries"));
			assertTrue(rsp.body().string().contains("No"));

		} catch(Exception ignored) {}

	}

	@Test
	public void checkGetBusiness(int serverPort) {
		// Mock manipulator & insert into controller
		BusinessAPIManipulator manip = mock(BusinessAPIManipulator.class);
		BusinessController control = new BusinessController(mock(Logger.class), null);
		control.setBusinessAPIManipulator(manip);

		// Fake data to test
		JsonArray arr = new JsonArray();
		JsonObject obj = new JsonObject();

		obj.addProperty("id", "ALD");
		obj.addProperty("name", "Aldi");
		obj.addProperty("category", "Groceries");
		obj.addProperty("sanctioned", false);
		arr.add(obj);

		when(manip.getApiInformation()).thenReturn(arr);

		// Check raw output
		assertNotNull(control.getBusiness("ALD"));

		// Check HTTP output
		Request req = new Request.Builder()
				.url("http://localhost:"+serverPort+"/businesses/business?name=Aldi")
				.build();

		try(Response rsp = client.newCall(req).execute()) {
			assertNotNull(rsp.body());

			assertTrue(rsp.body().string().contains("ALD"));
			assertTrue(rsp.body().string().contains("Aldi"));
			assertTrue(rsp.body().string().contains("Groceries"));
			assertTrue(rsp.body().string().contains("No"));

		} catch(Exception ignored) {}

		// Wrong business name

		// Check raw output
		assertNotNull(control.getBusiness("ALO"));

		// Check HTTP output
		Request err_req_1 = new Request.Builder()
				.url("http://localhost:"+serverPort+"/businesses/business?name=Aloo")
				.build();

		try(Response rsp = client.newCall(err_req_1).execute()) {
			assertNotNull(rsp.body());

			// ! NEEDS FIXED
//			assertTrue(rsp.body().string().contains("404"));
//			assertTrue(rsp.body().string().contains("Not Found"));

		} catch(Exception ignored) {}

		// No name param

		assertNotNull(control.getBusiness(null));

		Request err_req_2 = new Request.Builder()
				.url("http://localhost:"+serverPort+"/businesses/business?name=")
				.build();

		try(Response rsp = client.newCall(err_req_2).execute()) {
			assertNotNull(rsp.body());

			// ! NEEDS FIXED
//			assertTrue(rsp.body().string().contains("400"));
//			assertTrue(rsp.body().string().contains("Bad Request"));

		} catch(Exception ignored) {}


	}
}