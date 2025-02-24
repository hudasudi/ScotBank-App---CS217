package uk.co.asepstrath.bank.account_tests;

import io.jooby.test.JoobyTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.App;
import uk.co.asepstrath.bank.view.AccountController;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JoobyTest(App.class)
public class AccountControllerTests {

    static OkHttpClient client = new OkHttpClient();

    @Test
    public void checkAccountsObjects(int serverPort) {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        try {
            when(mockDataSource.getConnection()).thenReturn(mockConnection);
        } catch (SQLException e) {
			e.printStackTrace();
		}

        try {
            when(mockConnection.createStatement()).thenReturn(mockStatement);
        } catch(SQLException e) {
            e.printStackTrace();
        }

        try {
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        } catch(SQLException e) {
            e.printStackTrace();
        }

		AccountController control = new AccountController(mock(Logger.class), mockDataSource);

		// ISSUE WITH CONNECTION BECAUSE MOCKING IT
        assertNotNull(control.accountsObjects());

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/accounts/account-objects")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

        } catch (Exception ignored) {}
    }

    @Test
    public void checkAccountsObject(int serverPort) {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        try {
            when(mockDataSource.getConnection()).thenReturn(mockConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            when(mockConnection.createStatement()).thenReturn(mockStatement);
        } catch(SQLException e) {
            e.printStackTrace();
        }

        try {
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        } catch(SQLException e) {
            e.printStackTrace();
        }

        try {
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getString(1)).thenReturn("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2");
            when(mockResultSet.getString(2)).thenReturn("Miss Lavina Waelchi");
            when(mockResultSet.getDouble(3)).thenReturn(544.91);
            when(mockResultSet.getString(4)).thenReturn("No");
        } catch (SQLException e) {
            e.printStackTrace();
		}

		AccountController control = new AccountController(mock(Logger.class), mockDataSource);

        assertNotNull(control.accountsObject(0));

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/accounts/account-object?pos=0")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

        } catch (Exception ignored) {}
    }

    @Test
    public void checkGetAccounts(int serverPort) {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        try {
            when(mockDataSource.getConnection()).thenReturn(mockConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            when(mockConnection.createStatement()).thenReturn(mockStatement);
        } catch(SQLException e) {
            e.printStackTrace();
        }

        try {
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        } catch(SQLException e) {
            e.printStackTrace();
        }

        try {
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getString(1)).thenReturn("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2");
            when(mockResultSet.getString(2)).thenReturn("Miss Lavina Waelchi");
            when(mockResultSet.getDouble(3)).thenReturn(544.91);
            when(mockResultSet.getString(4)).thenReturn("No");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        AccountController control = new AccountController(mock(Logger.class), mockDataSource);

        assertNotNull(control.getAccounts());

        Request req = new Request.Builder()
                .url("http://localhost:"+serverPort+"/accounts/accounts-view")
                .build();

        try(Response rsp = client.newCall(req).execute()) {
            assertNotNull(rsp.body());

        } catch(Exception ignored) {}
    }
}
