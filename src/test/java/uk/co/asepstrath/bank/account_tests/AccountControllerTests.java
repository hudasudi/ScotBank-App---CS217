package uk.co.asepstrath.bank.account_tests;

import io.jooby.test.JoobyTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.App;
import uk.co.asepstrath.bank.view.AccountController;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@JoobyTest(App.class)
public class AccountControllerTests {

    static OkHttpClient client = new OkHttpClient();

    @Test
    public void checkAccountsObjects(int serverPort) {
        AccountController control = new AccountController(mock(Logger.class), "src/test/java/uk/co/asepstrath/bank/account_tests/api.json");

        assertNotNull(control.accountsObjects());

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/accounts/account-objects")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

        } catch (Exception ignored) {}
    }

    @Test
    public void checkGetAccount(int serverPort) {
        AccountController control = new AccountController(mock(Logger.class), "src/test/java/uk/co/asepstrath/bank/account_tests/api.json");

        assertNotNull(control.getAccount("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2", false));

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/accounts/account?uuid=c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2&is_admin=false")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

        } catch (Exception ignored) {}
    }

    @Test
    public void checkGetAccounts(int serverPort) {
        AccountController control = new AccountController(mock(Logger.class), "src/test/java/uk/co/asepstrath/bank/account_tests/api.json");

        assertNotNull(control.getAccounts());

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/accounts/accounts-view")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

        } catch(Exception ignored) {}
    }
}