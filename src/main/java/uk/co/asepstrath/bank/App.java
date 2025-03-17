package uk.co.asepstrath.bank;

import io.jooby.netty.NettyServer;
import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.api.AccountAPIParser;
import uk.co.asepstrath.bank.api.TransactionAPIParser;
import uk.co.asepstrath.bank.view.AccountController_;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App extends Jooby {
    private AccountAPIParser accountParser;
    private TransactionAPIParser transactionParser;

    {
        /*
        This section is used for setting up the Jooby Framework modules
         */
        install(new NettyServer());
        install(new UniRestExtension());
        install(new HandlebarsModule());
        install(new HikariModule("mem"));

        /*
        This will host any files in src/main/resources/assets on <host>/assets
        For example in the dice template (dice.hbs) it references "assets/dice.png" which is in resources/assets folder
         */
        assets("/assets/*", "/assets");
        assets("/service_worker.js","/service_worker.js");

        /*
        Now we set up our controllers and their dependencies
         */
        DataSource ds = require(DataSource.class);
        Logger log = getLog();

        mvc(new AccountController_(log, ds));

        /*
        Finally we register our application lifecycle methods
         */
        onStarted(() -> onStart());
        onStop(() -> onStop());
    }

    public static void main(final String[] args) {
        runApp(args, App::new);
    }

    /** This function is called on program startup, it should ensure that a DB is properly set up & API information is retrieved successfully
     */
    public void onStart() {
        Logger log = getLog();
        log.info("Starting Up...");

        log.info("Attempting to retrieve API information");

        // Fetch DB Source
        DataSource ds = require(DataSource.class);

        // Open Connection to DB
        try(Connection connection = ds.getConnection()) {
            Statement stmt = connection.createStatement();

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `Accounts` (`UUID` varchar(255), `Name` varchar(255), `Balance` double, `roundUpEnabled` bit)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `Transactions` (`Timestamp` varchar(255), `Amount` double, `From` varchar(255), `TransactionId` varchar(255), `Recipient` varchar(255), `Type` varchar(255))");
            stmt.close();

            accountParser = new AccountAPIParser(log, "https://api.asep-strath.co.uk/api/accounts", ds);
            accountParser.writeAPIInformation();
            transactionParser = new TransactionAPIParser(log, "https://api.asep-strath.co.uk/api/transactions", ds);
            transactionParser.writeAPIInformation();
        }

        catch(SQLException e) {
            log.error("Database Creation Error", e);
        }
    }

    /** This function is called upon program shutdown
     */
    public void onStop() {
        Logger log = getLog();

        log.info("Shutting Down...");
    }
}