package uk.co.asepstrath.bank;

import io.jooby.netty.NettyServer;
import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;

import org.slf4j.Logger;

import uk.co.asepstrath.bank.api.parsers.AccountAPIParser;
import uk.co.asepstrath.bank.api.parsers.BusinessAPIParser;
import uk.co.asepstrath.bank.api.parsers.TransactionAPIParser;
import uk.co.asepstrath.bank.view.AccountController_;
import uk.co.asepstrath.bank.view.AdminController_;
import uk.co.asepstrath.bank.view.AuthenticationController_;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class App extends Jooby {

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
        mvc(new AdminController_(log, ds));
        mvc(new AuthenticationController_(log, ds));

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
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `Businesses` (`ID` varchar(255), `Name` varchar(255), `Category` varchar(255), `Sanctioned` bit)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `Transactions` (`Timestamp` varchar(255), `Amount` double, `Sender` varchar(255), `TransactionID` varchar(255), `Recipient` varchar(255), `Type` varchar(255))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `Users` (`UUID` varchar(255), `Username` varchar(255), `Password` varchar(256))");

            // Set up fake user & admin

            MessageDigest hashing_instance = MessageDigest.getInstance("SHA-256");
            BigInteger hashed_password = new BigInteger(1, hashing_instance.digest("password1".getBytes()));

            stmt.executeUpdate("INSERT INTO Users VALUES ('006274fa-16fd-4a79-968b-df889c4a2e75', 'username', '" + hashed_password + "')");
            stmt.executeUpdate("INSERT INTO users VALUES ('R8EckqhZ0Vwx2RoJfKEDyXFyqd9Q2ufFiIU8', 'admin', " + hashed_password + ")");

            stmt.close();

            log.info("Retrieving Database Information");

			AccountAPIParser account_parser = new AccountAPIParser(log, "https://api.asep-strath.co.uk/api/accounts", ds);
            account_parser.writeAPIInformation();

			BusinessAPIParser business_parser = new BusinessAPIParser(log, "https://api.asep-strath.co.uk/api/businesses", ds);
            business_parser.writeAPIInformation();

			TransactionAPIParser transaction_parser = new TransactionAPIParser(log, "https://api.asep-strath.co.uk/api/transactions", ds);
            transaction_parser.writeAPIInformation();

            log.info("Retrieved Database Information Successfully");
        }

        catch(SQLException e) {
            log.error("Database Creation Error", e);
        }

        catch (NoSuchAlgorithmException e) {
            log.error("Error with hashing algorithm", e);
        }
    }

    /** This function is called upon program shutdown
     */
    public void onStop() {
        Logger log = getLog();

        log.info("Shutting Down...");
    }
}