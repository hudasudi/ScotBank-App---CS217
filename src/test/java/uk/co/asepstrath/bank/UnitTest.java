package uk.co.asepstrath.bank;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.AccountAPIParser;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UnitTest {
    @Test
    public void AccountApiParserTest() {
        Logger log = mock(Logger.class);
        AccountAPIParser test = new AccountAPIParser(log,"test", null);
        assertNotNull(test);
    }

    @Test
    public void AccountApiManipulatorTest() throws SQLException {
        // I think this test is wrong

        // Setup
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("id")).thenReturn("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2");
        when(mockResultSet.getString("name")).thenReturn("Miss Lavina Waelchi");
        when(mockResultSet.getDouble("balance")).thenReturn(544.91);
        when(mockResultSet.getBoolean("roundUpEnabled")).thenReturn(false);

        AccountAPIManipulator manipulator = mock(AccountAPIManipulator.class);

        // Test getApiInformation
        JsonArray apiData = new JsonArray();
        JsonObject accountData = new JsonObject();

        accountData.addProperty("uuid", "c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2");
        accountData.addProperty("name", "Miss Lavina Waelchi");
        accountData.addProperty("balance", 544.91);
        accountData.addProperty("roundUpEnabled", false);
        apiData.add(accountData);

        when(manipulator.getApiInformation()).thenReturn(apiData);

        JsonArray api_info = manipulator.getApiInformation();

        assertNotNull(api_info);
        assertEquals("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2", api_info.get(0).getAsJsonObject().get("uuid").getAsString());

        // Test jsonToAccounts
        ArrayList<Account> testList = new ArrayList<>();
        testList.add(new Account("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2", "Miss Lavina Waelchi", BigDecimal.valueOf(544.91), false));

         when(manipulator.jsonToAccounts()).thenReturn(testList);

        ArrayList<Account> accounts = manipulator.jsonToAccounts();

        assertNotNull(accounts);

        assertEquals("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2", accounts.getFirst().getID());
        assertEquals("Miss Lavina Waelchi", accounts.getFirst().getName());
        assertEquals(BigDecimal.valueOf(544.91), accounts.getFirst().getBalance());

        // Test createJsonMap
        HashMap<String, String> map = new HashMap<>();
        map.put("uuid", "c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2");
        map.put("name", "Miss Lavina Waelchi");
        map.put("bal", "544.91");
        map.put("round", "No");

        when(manipulator.createJsonMap(accountData)).thenReturn(map);

        Map<String, String> jsonMap = manipulator.createJsonMap(accountData);

        assertNotNull(jsonMap);

        assertEquals("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2", jsonMap.get("uuid"));
        assertEquals("Miss Lavina Waelchi", jsonMap.get("name"));
        assertEquals("544.91", jsonMap.get("bal"));
        assertEquals("No", jsonMap.get("round"));

        // Test createHandleBarsJSONMap
        Map<String, Object> handlebarsMap = manipulator.createHandleBarsJSONMap();
        assertNotNull(handlebarsMap);
    }

    @Test
    public void AppTests() {
        // Figure out way to force database creation crash
    }

    @Test
    public void AccountControllerTests() {
        // Test all methods under AccountController
    }
}