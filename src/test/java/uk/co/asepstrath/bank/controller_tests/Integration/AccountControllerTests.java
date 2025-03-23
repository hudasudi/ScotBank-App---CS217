package uk.co.asepstrath.bank.controller_tests.Integration;

import io.jooby.test.JoobyTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.App;
import uk.co.asepstrath.bank.Business;
import uk.co.asepstrath.bank.Transaction;
import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;
import uk.co.asepstrath.bank.view.AccountController;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@JoobyTest(App.class)
public class AccountControllerTests {
    static OkHttpClient client = new OkHttpClient();

    @Test
    public void checkGetAccount(int serverPort) {
        AccountController control = new AccountController(mock(Logger.class), mock(DataSource.class));

        // Account manipulator

        AccountAPIManipulator acc_manip = spy(new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setAccountAPIManipulator(acc_manip);

        doReturn(new Account(
                "ID",
                "Name",
                BigDecimal.valueOf(10),
                false
        )).when(acc_manip).getAccountByUUID(anyString());

        // Transaction manipulator

        TransactionAPIManipulator tra_manip = spy(new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setTransactionAPIManipulator(tra_manip);

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(
           "timestamp",
           BigDecimal.valueOf(10),
           null,
           "ID",
           "Recipient",
            "DEPOSIT"
        ));

        doReturn(transactions).when(tra_manip).getTransactionForAccount(anyString());

        // Check HTTP Response

        Request req = new Request.Builder()
            .url("http://localhost:"+serverPort+"/account/dashboard")
            .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccount() Failed", e);
        }
    }

    @Test
    public void checkGetAccountDetails(int serverPort) {
        // Check HTTP Response

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/account/summary")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountDetails() Failed", e);
        }
    }

    @Test
    public void checkGetAccountSummary(int serverPort) {
        AccountController control = new AccountController(mock(Logger.class), mock(DataSource.class));

        // Account manipulator

        AccountAPIManipulator acc_manip = spy(new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setAccountAPIManipulator(acc_manip);

        doReturn(new Account(
                "ID",
                "Name",
                BigDecimal.valueOf(10),
                false
        )).when(acc_manip).getAccountByUUID(anyString());

        // Business manipulator
        BusinessAPIManipulator bus_manip = spy(new BusinessAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setBusinessAPIManipulator(bus_manip);

        ArrayList<Business> businesses = new ArrayList<>();
        businesses.add(new Business(
                "ID",
                "Name",
                "Category",
                false
        ));

        doReturn(businesses).when(bus_manip).jsonToBusinesses();

        // Transaction manipulator

        TransactionAPIManipulator tra_manip = spy(new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setTransactionAPIManipulator(tra_manip);

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(
                "timestamp",
                BigDecimal.valueOf(10),
                null,
                "ID",
                "Recipient",
                "DEPOSIT"
        ));

        doReturn(transactions).when(tra_manip).jsonToTransactions();

        // Check HTTP Response

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/account/dashboard")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccount() Failed", e);
        }
    }

    @Test
    public void checkGetAccountTransactionDetails(int serverPort) {
        AccountController control = new AccountController(mock(Logger.class), mock(DataSource.class));

        // Account manipulator

        AccountAPIManipulator acc_manip = spy(new AccountAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setAccountAPIManipulator(acc_manip);

        doReturn(new Account(
                "ID",
                "Name",
                BigDecimal.valueOf(10),
                false
        )).when(acc_manip).getAccountByUUID(anyString());

        // Transaction manipulator

        TransactionAPIManipulator tra_manip = spy(new TransactionAPIManipulator(mock(Logger.class), mock(DataSource.class)));
        control.setTransactionAPIManipulator(tra_manip);

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(
                "timestamp",
                BigDecimal.valueOf(10),
                null,
                "ID",
                "Recipient",
                "DEPOSIT"
        ));

        doReturn(transactions).when(tra_manip).jsonToTransactions();

        // Check HTTP Response

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/account/dashboard")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccount() Failed", e);
        }
    }
}