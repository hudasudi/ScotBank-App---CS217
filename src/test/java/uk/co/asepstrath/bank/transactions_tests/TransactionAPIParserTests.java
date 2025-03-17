package uk.co.asepstrath.bank.transactions_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.json.XML;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.TransactionAPIParser;
import com.google.gson.JsonObject;
import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

public class TransactionAPIParserTests {
    @Test
    public void apiResponse(){
        DataSource mockDataSource = mock(DataSource.class);

        TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mockDataSource);
        JsonArray testArray = parser.parseJSONResponse();
        Assertions.assertNotNull(testArray);
        Assertions.assertEquals("[",testArray.toString().substring(0,1));
    }

    @Test
    public void temp(){
        DataSource mockDataSource = mock(DataSource.class);

        TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mockDataSource);

        JSONObject test = XML.toJSONObject(parser.getAPIData(0).body());
        JsonObject t = JsonParser.parseString(test.toString()).getAsJsonObject();
        JsonArray t2 = JsonParser.parseString(t.get("pageResult").getAsJsonObject().get("results").toString()).getAsJsonArray();
        System.out.println(t);
    }

    @Test
    public void temp2(){
        DataSource mockDataSource = mock(DataSource.class);

        TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mockDataSource);

        JsonArray test = parser.parseJSONResponse();

        System.out.println(test.get(1325));

    }
}
