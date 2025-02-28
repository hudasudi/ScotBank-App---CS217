package uk.co.asepstrath.bank.transactions_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
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

//        JsonParser parser2 = new JsonParser();
//        JsonArray test = parser2.parse(parser.getAPIData(0).body()).getAsJsonObject().get("results").getAsJsonArray();
//        System.out.println(parser2.parse(parser.getAPIData(0).body()).getAsJsonObject());
//        System.out.println(test.get(0));
        System.out.println(parser.getAPIData(0).body());
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
