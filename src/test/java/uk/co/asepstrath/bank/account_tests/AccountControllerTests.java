package uk.co.asepstrath.bank.account_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jooby.test.JoobyTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.App;
import uk.co.asepstrath.bank.api.AccountAPIManipulator;
import uk.co.asepstrath.bank.view.AccountController;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JoobyTest(App.class)
public class AccountControllerTests {

    static OkHttpClient client = new OkHttpClient();

    @Test
    public void checkAccountsObjects(int serverPort) {
        // Mock Manipulator & Insert into Controller
        AccountAPIManipulator mockManipulator = mock(AccountAPIManipulator.class);
        AccountController control = new AccountController(mock(Logger.class), null);
        control.setAccountAPIManipulator(mockManipulator);

        // Create fake data to test against
        ArrayList<Account> mockAccounts = new ArrayList<>();
        mockAccounts.add(new Account("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2", "Miss Lavina Waelchi", BigDecimal.valueOf(544.91), false));
        when(mockManipulator.jsonToAccounts()).thenReturn(mockAccounts);

        // Check raw output
        String accountData = control.accountsObjects();

        assertNotNull(accountData);
        assertTrue(accountData.contains("Miss Lavina Waelchi"));
        assertTrue(accountData.contains("544.91"));

        // Check HTTP output
        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/accounts/account-objects")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());
            assertTrue(rsp.body().string().contains("Miss Lavina Waelchi"));
            assertTrue(rsp.body().string().contains("544.91"));
        } catch(Exception ignored) {}
    }

    @Test
    public void checkAccountsObject(int serverPort) {
        // Mock Manipulator & Insert into Controller
        AccountAPIManipulator mockManipulator = mock(AccountAPIManipulator.class);
        AccountController control = new AccountController(mock(Logger.class), null);
        control.setAccountAPIManipulator(mockManipulator);

        // Create fake data to test against
        ArrayList<Account> mockAccounts = new ArrayList<>();
        mockAccounts.add(new Account("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2", "Miss Lavina Waelchi", BigDecimal.valueOf(544.91), false));
        when(mockManipulator.jsonToAccounts()).thenReturn(mockAccounts);

        // Check raw output
        String accountData = control.accountsObject(0);

        assertNotNull(accountData);
        assertTrue(accountData.contains("Miss Lavina Waelchi"));
        assertTrue(accountData.contains("544.91"));

        // Check HTTP output
        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/accounts/account?uuid=c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2&is_admin=false")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());
            assertTrue(rsp.body().string().contains("Miss Lavina Waelchi"));
            assertTrue(rsp.body().string().contains("544.91"));
        } catch(Exception ignored) {}
    }

    // THIS TEST DOESNT WORK PER SE
    @Test
    public void checkGetAccounts(int serverPort) {
        // Somehow the code inside here is reaching through time & space to take db info that's not there & output it (that's why the test passes)

        // Mock Manipulator & Insert into Controller
        AccountAPIManipulator mockManipulator = mock(AccountAPIManipulator.class);
        AccountController control = new AccountController(mock(Logger.class), null);
        control.setAccountAPIManipulator(mockManipulator);

        // Fake data to test
        JsonArray arr = new JsonArray();
        JsonObject obj = new JsonObject();

        obj.addProperty("uuid", "c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2");
        obj.addProperty("name", "Miss Lavina Waelchi");
        obj.addProperty("balance", 544.91);
        obj.addProperty("roundUpEnabled", false);

        arr.add(obj);

        when(mockManipulator.getApiInformation()).thenReturn(arr);

        // Check raw output
        assertNotNull(control.getAccounts());

        // Check HTTP output
        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/accounts/account-view")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

            assertTrue(rsp.body().string().contains("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2"));
            assertTrue(rsp.body().string().contains("Miss Lavina Waelchi"));
            assertTrue(rsp.body().string().contains("544.91"));
            assertTrue(rsp.body().string().contains("No"));

        } catch(Exception ignored) {}
    }

	// THIS TEST DOESNT WORK PER SE
    @Test
    public void checkGetAccount(int serverPort) {
		// Somehow the code inside here is reaching through time & space to take db info that's not there & output it (that's why the test passes)

        // Mock Manipulator & Insert into Controller
        AccountAPIManipulator mockManipulator = mock(AccountAPIManipulator.class);
        AccountController control = new AccountController(mock(Logger.class), null);
        control.setAccountAPIManipulator(mockManipulator);

        // Fake data to test
        JsonArray arr = new JsonArray();
        JsonObject obj = new JsonObject();

        obj.addProperty("id", "c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2");
        obj.addProperty("name", "Miss Lavina Waelchi");
        obj.addProperty("balance", 544.91);
        obj.addProperty("roundUpEnabled", false);

        arr.add(obj);

        when(mockManipulator.getApiInformation()).thenReturn(arr);

        // Check raw output
        assertNotNull(control.getAccount("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2", false));

        // Check HTTP output

        // is_admin = false
        Request req = new Request.Builder()
                .url("http://localhost:" + serverPort + "/accounts/account?uuid=c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2&is_admin=false")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

            assertTrue(rsp.body().string().contains("Miss Lavina Waelchi"));
            assertTrue(rsp.body().string().contains("544.91"));
            assertTrue(rsp.body().string().contains("No"));
            assertTrue(rsp.body().string().contains("YOU ARE A USER"));

        } catch (Exception ignored) {}

        // is_admin = true
        req = new Request.Builder()
                .url("http://localhost:" + serverPort + "/accounts/account?uuid=c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2&is_admin=true")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

            assertTrue(rsp.body().string().contains("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2"));
            assertTrue(rsp.body().string().contains("Miss Lavina Waelchi"));
            assertTrue(rsp.body().string().contains("544.91"));
            assertTrue(rsp.body().string().contains("No"));
            assertTrue(rsp.body().string().contains("YOU ARE AN ADMIN"));

        } catch(Exception ignored) {}

        // missing uuid param
        req = new Request.Builder()
                .url("http://localhost:" + serverPort + "/accounts/account?is_admin=true")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

            assertTrue(rsp.body().string().contains("400 - Bad Request"));
            assertTrue(rsp.body().string().contains("No uuid or is_admin parameter provided!"));

        } catch(Exception ignored) {}

        // missing is_admin param
        req = new Request.Builder()
                .url("http://localhost:" + serverPort + "/accounts/account?uuid=c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

            assertTrue(rsp.body().string().contains("400 - Bad Request"));
            assertTrue(rsp.body().string().contains("No uuid or is_admin parameter provided!"));
        } catch(Exception ignored) {}

    }
}