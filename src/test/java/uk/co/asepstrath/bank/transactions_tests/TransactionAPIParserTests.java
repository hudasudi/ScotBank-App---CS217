package uk.co.asepstrath.bank.transactions_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.json.XML;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.AccountAPIParser;
import uk.co.asepstrath.bank.api.TransactionAPIParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.sql.DataSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class TransactionAPIParserTests {
    @Test
    public void apiResponse(){
        DataSource mockDataSource = mock(DataSource.class);

        TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mockDataSource);
        JsonArray testArray = new JsonArray();
        testArray = parser.parseJSONResponse();
        Assertions.assertNotNull(testArray);
        Assertions.assertEquals("[",testArray.toString().substring(0,1));
    }

    @Test
    public void temp(){
        DataSource mockDataSource = mock(DataSource.class);

        TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mockDataSource);
        JsonParser p = new JsonParser();

        JSONObject test = XML.toJSONObject(parser.getAPIData(0).body());
        JsonObject t = p.parse(test.toString()).getAsJsonObject();
        JsonArray t2 = p.parse(t.get("pageResult").getAsJsonObject().get("results").toString()).getAsJsonArray();
        System.out.println(t);
    }

    @Test
    public void temp2(){
        DataSource mockDataSource = mock(DataSource.class);

        TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mockDataSource);

        JsonArray test = parser.parseJSONResponse();

        System.out.println(test.get(1325));

    }


//    @Test
//    public void shouldWriteCorrectly() {
//        try {
//            // Make mock data source to write to
//            DataSource mockDataSource = mock(DataSource.class);
//
//            // Make parser & write to mock source
//            TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mockDataSource);
//            parser.writeAPIInformation();
//
//            // Create manipulator to test data pass through
//            AccountAPIManipulator manip = new AccountAPIManipulator(mock(Logger.class), mockDataSource);
//
//            // Get that the API info is retrieved from the mock db properly
//            assertNotNull(manip.getApiInformation());
//
//            // Check an example JsonObject for its contents
//            JsonObject obj = manip.getApiInformation().get(0).getAsJsonObject();
//
//            assertEquals("Miss Lavina Waelchi", obj.get("name").toString());
//            assertEquals("c9dfe369-c5f8-44fd-b9e2-f4fc5ac56ac2", obj.get("uuid").toString());
//            assertEquals(544.91, obj.get("balance").getAsDouble());
//            assertFalse(obj.get("roundUpEnabled").getAsBoolean());
//        } catch (Exception ignored) {}
//    }
}
