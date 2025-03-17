package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.parsers.AccountAPIParser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class UnitTest {
    @Test
    public void AccountApiParserTest() {
        Logger log = mock(Logger.class);
        AccountAPIParser test = new AccountAPIParser(log,"test", null);
        assertNotNull(test);
    }

//    @Test
//    public void AccountApiManipulatorTest() throws SQLException {
//        // I think this test is wrong
//
//        // Setup
//        DataSource mockDataSource = mock(DataSource.class);
//        Connection mockConnection = mock(Connection.class);
//        Statement mockStatement = mock(Statement.class);
//        ResultSet mockResultSet = mock(ResultSet.class);
//
//        when(mockDataSource.getConnection()).thenReturn(mockConnection);
//        when(mockConnection.createStatement()).thenReturn(mockStatement);
//        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
//
//        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
//        when(mockResultSet.getString("id")).thenReturn("04f6ab33-8208-4234-aabd-b6a8be8493da");
//        when(mockResultSet.getString("name")).thenReturn("Melva Rogahn");
//        when(mockResultSet.getDouble("balance")).thenReturn(594.82);
//        when(mockResultSet.getBoolean("roundUpEnabled")).thenReturn(false);
//
//        AccountAPIManipulator manipulator = mock(AccountAPIManipulator.class);
//
//        // Test getApiInformation
//        JsonArray apiData = new JsonArray();
//        JsonObject accountData = new JsonObject();
//
//        accountData.addProperty("uuid", "04f6ab33-8208-4234-aabd-b6a8be8493da");
//        accountData.addProperty("name", "Melva Rogahn");
//        accountData.addProperty("balance", 594.82);
//        accountData.addProperty("roundUpEnabled", false);
//        apiData.add(accountData);
//
//        when(manipulator.getApiInformation()).thenReturn(apiData);
//
//        JsonArray api_info = manipulator.getApiInformation();
//
//        assertNotNull(api_info);
//        assertEquals("04f6ab33-8208-4234-aabd-b6a8be8493da", api_info.get(0).getAsJsonObject().get("uuid").getAsString());
//
//        // Test jsonToAccounts
//        ArrayList<Account> testList = new ArrayList<>();
//        testList.add(new Account("04f6ab33-8208-4234-aabd-b6a8be8493da", "Melva Rogahn", BigDecimal.valueOf(594.82), false));
//
//         when(manipulator.jsonToAccounts()).thenReturn(testList);
//
//        ArrayList<Account> accounts = manipulator.jsonToAccounts();
//
//        assertNotNull(accounts);
//
//        assertEquals("04f6ab33-8208-4234-aabd-b6a8be8493da", accounts.getFirst().getID());
//        assertEquals("Melva Rogahn", accounts.getFirst().getName());
//        assertEquals(BigDecimal.valueOf(594.82), accounts.getFirst().getBalance());
//
//        // Test createJsonMap
//        HashMap<String, String> map = new HashMap<>();
//        map.put("uuid", "04f6ab33-8208-4234-aabd-b6a8be8493da");
//        map.put("name", "Melva Rogahn");
//        map.put("bal", "594.82");
//        map.put("round", "No");
//
//        when(manipulator.createJsonMap(accountData)).thenReturn(map);
//
//        Map<String, String> jsonMap = manipulator.createJsonMap(accountData);
//
//        assertNotNull(jsonMap);
//
//        assertEquals("04f6ab33-8208-4234-aabd-b6a8be8493da", jsonMap.get("uuid"));
//        assertEquals("Melva Rogahn", jsonMap.get("name"));
//        assertEquals("594.82", jsonMap.get("bal"));
//        assertEquals("No", jsonMap.get("round"));
//
//        // Test createHandleBarsJSONMap
//        Map<String, Object> handlebarsMap = manipulator.createHandleBarsJSONMap("account");
//        assertNotNull(handlebarsMap);
//    }

    @Test
    public void AppTests() {
        // Figure out way to force database creation crash
    }

    @Test
    public void AccountControllerTests() {
        // Test all methods under AccountController
    }
}