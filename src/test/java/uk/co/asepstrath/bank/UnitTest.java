package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.AccountAPIParser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnitTest {
    /*
    Unit tests should be here
    Example can be found in example/UnitTest.java
     */

    @Test
    public void AccountApiParserTest(){
        Logger log = mock(Logger.class);
        AccountAPIParser test = new AccountAPIParser(log,"test", null);
        assertNotNull(test);
    }

    @Test
    public void AccountApiManipulatorTest(){
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

        AccountAPIManipulator testManipulator = new AccountAPIManipulator(mock(Logger.class), mockDataSource);

        assertNotNull(testManipulator);

        ArrayList<Account> testList = testManipulator.jsonToAccounts();

        // THIS IS ALWAYS NULL BECAUSE getAPIInformation ALWAYS CALLS REAL DB NEVER FAKE
        assertEquals("Miss Lavina Waelchi",testList.getFirst().getName());
        Map<String, Object> map = testManipulator.createHandleBarsJSONMap();

        assertNotNull(map);
    }

    @Test
    public void AppTests(){

    }

    @Test
    public void AccountControllerTests(){

    }
}
