package uk.co.asepstrath.bank.controller_tests.Integration;

import io.jooby.test.JoobyTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.App;
import uk.co.asepstrath.bank.Transaction;
import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;
import uk.co.asepstrath.bank.view.AccountController;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@JoobyTest(App.class)
public class AdminControllerTests {
    static OkHttpClient client = new OkHttpClient();

    @Test
    public void checkGetDashboard(int serverPort) {
        AccountController control = new AccountController(mock(Logger.class), mock(DataSource.class));

        // Account manipulator
        AccountAPIManipulator acc_manip = spy(new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setAccountAPIManipulator(acc_manip);

        Account a = new Account(
                "ID",
                "Name",
                BigDecimal.ZERO,
                false
        );

        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(a);

        doReturn(accounts).when(acc_manip).jsonToAccounts();

        // Transaction manipulator
        TransactionAPIManipulator tra_manip = spy(new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setTransactionAPIManipulator(tra_manip);

        Map<String, Object> map = new HashMap<>();
        map.put("account", a);

        doReturn(map).when(tra_manip).getBalanceForAccount(any(Account.class));

        // Check HTTP Response

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/admin/dashboard")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccount() Failed", e);
        }
    }

    @Test
    public void checkGetSingleAccount(int serverPort) {
        AccountController control = new AccountController(mock(Logger.class), mock(DataSource.class));

        // Account manipulator
        AccountAPIManipulator acc_manip = spy(new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setAccountAPIManipulator(acc_manip);

        Account a = new Account(
                "ID",
                "Name",
                BigDecimal.ZERO,
                false
        );

        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(a);

        doReturn(accounts).when(acc_manip).jsonToAccounts();

        // Transaction manipulator
        TransactionAPIManipulator tra_manip = spy(new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setTransactionAPIManipulator(tra_manip);

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(
                "timestamp",
                BigDecimal.valueOf(10),
                "Sender",
                "ID",
                "Recipient",
                "DEPOSIT"
        ));

        Map<String, Object> map = new HashMap<>();
        map.put("account", a);
        map.put("transaction", transactions);

        doReturn(map).when(tra_manip).getBalanceForAccount(any(Account.class));

        // Check HTTP Response

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/admin/dashboard")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccount() Failed", e);
        }
    }
}
