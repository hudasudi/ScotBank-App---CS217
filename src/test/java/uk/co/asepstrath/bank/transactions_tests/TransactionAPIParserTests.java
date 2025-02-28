package uk.co.asepstrath.bank.transactions_tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.TransactionAPIParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

public class TransactionAPIParserTests {
    @Test
    public void apiResponse(){
        DataSource mockDataSource = mock(DataSource.class);

        TransactionAPIParser parser = new TransactionAPIParser(mock(Logger.class), "https://api.asep-strath.co.uk/api/transactions", mockDataSource);
        JsonArray test = new JsonArray();
        test = parser.parseJSONResponse();
        System.out.println(test.size());
    }

}
