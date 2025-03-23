package uk.co.asepstrath.bank.controller_tests.Unit;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.ValueNode;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.Business;
import uk.co.asepstrath.bank.Transaction;
import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;
import uk.co.asepstrath.bank.view.AccountController;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AccountControllerTests {

    // getAccount

    @Test
    public void checkGetAccountNoUUID() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            AccountAPIManipulator manipulator = spy(new AccountAPIManipulator(mock(Logger.class), mockDataSource));
            control.setAccountAPIManipulator(manipulator);

            doReturn(null).when(manipulator).getAccountByUUID(null);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn(null);
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);

            when(sess.put("page_error", "Error 404 - Account Not Found")).thenThrow(new Error("Account is null"));

            // Check for null uuid
            ModelAndView<Map<String, Object>> model_no_uuid = control.getAccount(ctx);


        }

        catch (Error e){
            assertTrue(true);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountNoUUID() Failed", e);
        }
    }

    @Test
    public void checkGetAccountUUID() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            // Setup manipulators
            AccountAPIManipulator acc_manipulator = spy(new AccountAPIManipulator(mock(Logger.class), mockDataSource));
            control.setAccountAPIManipulator(acc_manipulator);

            TransactionAPIManipulator transaction_manip = spy(new TransactionAPIManipulator(mock(Logger.class), mockDataSource));
            control.setTransactionAPIManipulator(transaction_manip);

            // Fake account
            doReturn(new Account(
                    "ID",
                    "Name",
                    BigDecimal.ZERO,
                    false
            )).when(acc_manipulator).getAccountByUUID(anyString());

            // Fake transaction
            ArrayList<Transaction> transaction = new ArrayList<>();
            transaction.add(new Transaction(
                    "timestamp",
                    BigDecimal.valueOf(10),
                    "Sender",
                    "ID",
                    "Recipient",
                    "DEPOSIT"
            ));

            doReturn(transaction).when(transaction_manip).getTransactionForAccount(anyString());

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);

            // Get created model
            ModelAndView<Map<String, Object>> model_uuid = control.getAccount(ctx);
            Map<String, Object> uuid_map = model_uuid.getModel();

            // Make sure it's not null
            assertNotNull(model_uuid);

            // Check contents
            assertTrue(uuid_map.containsKey("account"));
            assertTrue(uuid_map.containsKey("income"));
            assertTrue(uuid_map.containsKey("outgoings"));

            // Check account
            assertNotNull(uuid_map.get("account"));

            // Check income array
            assertNotNull(uuid_map.get("income"));

            // Check outgoings array
            assertNotNull(uuid_map.get("outgoings"));
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountUUID() Failed", e);
        }
    }

    @Test
    public void checkGetAccountNoAccount() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            AccountAPIManipulator manipulator = spy(new AccountAPIManipulator(mock(Logger.class), mockDataSource));
            control.setAccountAPIManipulator(manipulator);

            doReturn(null).when(manipulator).getAccountByUUID(anyString());

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(sess.put(anyString(), anyString())).thenThrow(new Error("No account"));

            ModelAndView<Map<String, Object>> model_no_uuid = control.getAccount(ctx);
            Map<String, Object> no_uuid_map = model_no_uuid.getModel();

            assertNotNull(model_no_uuid);
            assertTrue(no_uuid_map.containsKey("err"));
            assertEquals("Error 404 - Account Not Found", no_uuid_map.get("err"));
        }

        catch(Error e){
            assertTrue(true);
        }
        catch(Exception e) {
            throw new AssertionError("checkGetAccountNoAccount() Failed", e);
        }
    }

    @Test
    public void checkGetAccountError() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            // Setup manipulators
            AccountAPIManipulator acc_manipulator = mock(AccountAPIManipulator.class);
            control.setAccountAPIManipulator(acc_manipulator);

            doThrow(new NullPointerException("Error out")).when(acc_manipulator).getAccountByUUID(anyString());

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);

            ModelAndView<Map<String, Object>> model_error = control.getAccount(ctx);

            assertNull(model_error);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountError() Failed", e);
        }
    }

    @Test
    public void checkGetAccountDetailsNoUUID() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(sess.put(anyString(), anyString())).thenThrow(new Error("No account"));

            // Check for null uuid
            ModelAndView<Map<String, Object>> model_no_uuid = control.getAccountSettings(ctx);
            Map<String, Object> no_uuid_map = model_no_uuid.getModel();

            assertNotNull(model_no_uuid);
            assertTrue(no_uuid_map.containsKey("err"));
            assertEquals("Error 400 - Bad Request", no_uuid_map.get("err"));
        }

        catch (Error e){
            assertTrue(true);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountDetailsNoUUID() Failed", e);
        }
    }

    @Test
    public void checkGetAccountDetailsUUID() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);

            ModelAndView<Map<String, Object>> model_uuid = control.getAccountSettings(ctx);
            Map<String, Object> uuid_map = model_uuid.getModel();

            assertNotNull(model_uuid);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountDetailsUUID() Failed", e);
        }
    }

    @Test
    public void checkGetAccountSummaryNoUUID() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(sess.put(anyString(), anyString())).thenThrow(new Error("No uuid"));

            // Check for null uuid
            ModelAndView<Map<String, Object>> model_no_uuid = control.getAccountSummary(ctx);

        }

        catch(Error e){
            assertTrue(true);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountSummaryNoUUID() Failed", e);
        }
    }

    @Test
    public void checkGetAccountSummaryNoAccount() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            AccountAPIManipulator manipulator = spy(new AccountAPIManipulator(mock(Logger.class), mockDataSource));
            control.setAccountAPIManipulator(manipulator);

            doReturn(null).when(manipulator).getAccountByUUID(anyString());

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(sess.put(anyString(), anyString())).thenThrow(new Error("No account"));

            ModelAndView<Map<String, Object>> model_no_uuid = control.getAccountSummary(ctx);

        }

        catch(Error e){
            assertTrue(true);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountSummaryNoAccount() Failed", e);
        }
    }

    @Test
    public void checkGetAccountSummaryUUID() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            // Setup manipulators
            AccountAPIManipulator acc_manipulator = spy(new AccountAPIManipulator(mock(Logger.class), mockDataSource));
            control.setAccountAPIManipulator(acc_manipulator);

            BusinessAPIManipulator bus_manipulator = spy(new BusinessAPIManipulator(mock(Logger.class), mockDataSource));
            control.setBusinessAPIManipulator(bus_manipulator);

            TransactionAPIManipulator transaction_manip = spy(new TransactionAPIManipulator(mock(Logger.class), mockDataSource));
            control.setTransactionAPIManipulator(transaction_manip);

            // Fake account
            doReturn(new Account(
                    "ACCOUNTID",
                    "Name",
                    BigDecimal.ZERO,
                    false
            )).when(acc_manipulator).getAccountByUUID(anyString());

            // Fake Business
            ArrayList<Business> businesses = new ArrayList<>();
            businesses.add(new Business(
                    "BUS",
                    "Business",
                    "LordBusiness",
                    false
            ));

            doReturn(businesses).when(bus_manipulator).jsonToBusinesses();

            // Fake transaction
            ArrayList<Transaction> transaction = new ArrayList<>();
            transaction.add(new Transaction(
                    "timestamp",
                    BigDecimal.valueOf(10),
                    "ACCOUNTID",
                    "ID",
                    "BUS",
                    "PAYMENT"
            ));

            doReturn(transaction).when(transaction_manip).getTransactionForAccount(anyString());

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);

            // Get created model
            ModelAndView<Map<String, Object>> model_uuid = control.getAccount(ctx);
            Map<String, Object> uuid_map = model_uuid.getModel();

            // Make sure it's not null
            assertNotNull(model_uuid);

            // Check contents
            assertTrue(uuid_map.containsKey("account"));
//            assertTrue(uuid_map.containsKey("categories"));

            // Check account
            assertNotNull(uuid_map.get("account"));

            // Check categories
//            assertNotNull(uuid_map.get("categories"));
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountSummaryUUID() Failed", e);
        }
    }

    @Test
    public void checkGetAccountSummaryError() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            // Setup manipulators
            BusinessAPIManipulator bus_manipulator = mock(BusinessAPIManipulator.class);
            control.setBusinessAPIManipulator(bus_manipulator);

            AccountAPIManipulator manipulator = spy(new AccountAPIManipulator(mock(Logger.class), mockDataSource));
            control.setAccountAPIManipulator(manipulator);

            Account a = new Account(
                    "ID",
                    "Name",
                    BigDecimal.ZERO,
                    false
            );

            doReturn(a).when(manipulator).getAccountByUUID(anyString());

            doThrow(new NullPointerException("Error out")).when(bus_manipulator).jsonToBusinesses();

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(sess.put(anyString(), anyString())).thenThrow(new Error("No business manip"));

            ModelAndView<Map<String, Object>> model_error = control.getAccountTransactionDetails(ctx);

            assertNull(model_error);
        }
        catch(Error e){
            assertTrue(true);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountSummaryError() Failed", e);
        }
    }

    @Test
    public void checkGetAccountTransactionDetailsNoUUID() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(sess.put(anyString(), anyString())).thenThrow(new Error("No account"));

            // Check for null uuid
            ModelAndView<Map<String, Object>> model_no_uuid = control.getAccountTransactionDetails(ctx);

        }

        catch (Error e){
            assertTrue(true);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountTransactionDetailsNoUUID() Failed", e);
        }
    }

    @Test
    public void checkGetAccountTransactionDetailsNoAccount() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            AccountAPIManipulator manipulator = spy(new AccountAPIManipulator(mock(Logger.class), mockDataSource));
            control.setAccountAPIManipulator(manipulator);

            doReturn(null).when(manipulator).getAccountByUUID(anyString());

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(sess.put(anyString(), anyString())).thenThrow(new Error("No account"));

            ModelAndView<Map<String, Object>> model_no_uuid = control.getAccountTransactionDetails(ctx);

        }

        catch(Error e){
            assertTrue(true);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountTransactionDetailsNoUUID() Failed", e);
        }
    }

    @Test
    public void checkGetAccountTransactionDetailsUUID() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            // Setup manipulators
            AccountAPIManipulator acc_manipulator = spy(new AccountAPIManipulator(mock(Logger.class), mockDataSource));
            control.setAccountAPIManipulator(acc_manipulator);

            BusinessAPIManipulator bus_manipulator = spy(new BusinessAPIManipulator(mock(Logger.class), mockDataSource));
            control.setBusinessAPIManipulator(bus_manipulator);

            TransactionAPIManipulator transaction_manip = spy(new TransactionAPIManipulator(mock(Logger.class), mockDataSource));
            control.setTransactionAPIManipulator(transaction_manip);

            // Fake account
            doReturn(new Account(
                    "ACCOUNTID",
                    "Name",
                    BigDecimal.valueOf(20),
                    false
            )).when(acc_manipulator).getAccountByUUID(anyString());

            // Fake transaction
            ArrayList<Transaction> transaction = new ArrayList<>();
            transaction.add(new Transaction(
                    "timestamp timestamp",
                    BigDecimal.valueOf(10),
                    "ACCOUNTID",
                    "ID",
                    "BUS",
                    "PAYMENT"
            ));

            doReturn(transaction).when(transaction_manip).getTransactionForAccount(anyString());
            doReturn(new Business(
                    "id",
                    "name",
                    "category",
                    false
                    )

            ).when(bus_manipulator).getBusiness(anyString());

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(sess.put(anyString(), anyString())).thenThrow(new Error("No account"));

            ModelAndView<Map<String, Object>> model_uuid = control.getAccountTransactionDetails(ctx);
            Map<String, Object> uuid_map = model_uuid.getModel();

            assertNotNull(model_uuid);

            assertTrue(uuid_map.containsKey("transactions"));
            assertTrue(uuid_map.containsKey("income"));
            assertTrue(uuid_map.containsKey("outgoings"));
            assertTrue(uuid_map.containsKey("account"));
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountTransactionDetailsUUID() Failed", e);
        }
    }

    @Test
    public void checkGetAccountTransactionDetailsError() {
        try {
            DataSource mockDataSource = mock(DataSource.class);

            AccountController control = new AccountController(mock(Logger.class), mockDataSource);

            // Setup manipulators
            AccountAPIManipulator acc_manipulator = mock(AccountAPIManipulator.class);
            control.setAccountAPIManipulator(acc_manipulator);

            doThrow(new NullPointerException("Error out")).when(acc_manipulator).getAccountByUUID(anyString());

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(valueNode.booleanValue()).thenReturn(true);
            when(valueNode.toString()).thenReturn("str");
            when(sess.get("logged_in")).thenReturn(valueNode);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(sess.put(anyString(), anyString())).thenThrow(new Error("No account"));

            ModelAndView<Map<String, Object>> model_error = control.getAccountTransactionDetails(ctx);

            assertNull(model_error);
        }

        catch(Error e){
            assertTrue(true);
        }

        catch(Exception e) {
            throw new AssertionError("checkGetAccountTransactionDetailsError() Failed", e);
        }
    }
}
