package uk.co.asepstrath.bank.api.parsers;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.*;

public class AccountAPIParser extends APIParser {

    public AccountAPIParser(Logger log, String api_url, DataSource ds) {
        super(api_url, log, ds);
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO Accounts (UUID, Name, Balance, roundUpEnabled) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected JsonArray parseResponse() {
        String response = this.getResponseString();

        try {
            return JsonParser.parseString(response).getAsJsonArray();
        }

        catch(JsonSyntaxException e) {
            this.log.error("An error occurred whilst parsing the JSON response", e);
            return null;
        }
    }

    @Override
    protected void bindDataToStatement(JsonObject obj, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, trimString(obj.get("id").toString(), 1, 1));
        stmt.setString(2, trimString(obj.get("name").toString(), 1, 1));
        stmt.setDouble(3, obj.get("startingBalance").getAsDouble());
        stmt.setBoolean(4, obj.get("roundUpEnabled").getAsBoolean());
    }
}