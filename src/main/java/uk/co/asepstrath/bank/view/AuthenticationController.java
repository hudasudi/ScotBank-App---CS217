package uk.co.asepstrath.bank.view;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.Path;
import io.jooby.exception.MissingValueException;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class AuthenticationController extends Controller {

    /** An authentication controller that controls all authentication for logging in & signing up a user
     * @param log The log to print info, warnings & errors to
     * @param ds The data source to pull info from
    */
    public AuthenticationController(Logger log, DataSource ds) {
        super(log, ds);
    }

    /** Build & deploy the login page as the first thing the user sees
     * @return The model to build & deploy
    */
    @GET
    public ModelAndView<Map<String, Object>> getLogin(Context ctx) {
        Session session = ctx.session();

        // This will be empty if no errors occur
        Map<String, Object> model = new HashMap<>();

        try {
            if (session.get("signup_success").booleanValue()) {
                model.put("success_header", "Success!");
                model.put("success_message", "You have successfully signed up!");

                session.remove("signup_success");

                return new ModelAndView<>("auth/login.hbs", model);
            }
        }

        catch (MissingValueException ignored) {}

        try {
            // If you're already logged in, redirect back to dashboard
            if(session.get("logged_in").booleanValue()) {
                ctx.sendRedirect("/account/dashboard");
                return null;
            }

            int redirect_code = session.get("login_redirect").intValue();
            String error_message = this.getLoginErrorMessage(redirect_code);

            // If error message not null, then the redirect code was set to a known code
            if(error_message != null) {
                model.put("login_error", "There was an error whilst logging in");
                model.put("login_message", error_message);

                // Reset redirect
                session.remove("login_redirect");

                return new ModelAndView<>("auth/login.hbs", model);
            }
        }

        // Not been redirected & not logged in. Catch the exception at is & set default session info
        catch(MissingValueException ignored) {}

        session.put("logged_in", false);
        session.put("is_admin", false);
        return new ModelAndView<>("auth/login.hbs", model);
    }

    /** Authenticate user details against the database before setting the correct values in session & redirecting
     * @param ctx The current context
     * @param username The users username
     * @param password The users password
    */
    @POST("/login/auth")
    public void authLogin(Context ctx, String username, String password) {
        Session session = ctx.session();

        try(Connection conn = ds.getConnection()) {
            MessageDigest hashing_instance = MessageDigest.getInstance("SHA-256");
            BigInteger hash = new BigInteger(1, hashing_instance.digest(password.getBytes()));

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE Username = ? AND Password = ?");

            stmt.setString(1, username);
            stmt.setString(2, hash.toString());

            ResultSet set = stmt.executeQuery();

            // If there is a row in our set, then the user exists
            if(set.next()) {
                session.put("logged_in", true);
                session.put("uuid", set.getString("UUID"));

                ctx.sendRedirect("/redirect");
                return;
            }

            else {
                session.put("login_redirect", 3);
            }
        }

        catch(SQLException e) {
            log.error("Error whilst checking user details against database", e);
            session.put("login_redirect", 1);
        }

        // We don't have sha-256 somehow :(
        catch(NoSuchAlgorithmException e) {
            log.error("Somehow the hashing algorithm was not found");
            session.put("login_redirect", 1);
        }

        // If we fall out of either catches, redirect back to the login page
        ctx.sendRedirect("/");
    }

    @POST("/logout")
    public void logout(Context ctx) {
        Session session = ctx.session();

        session.destroy();
        ctx.sendRedirect("/");
    }

    /** If the user logs in correctly, then we must redirect them to the right page. Either the user page or the admin page
     * @param ctx The current context
    */
    @GET("/redirect")
    public void redirectUser(Context ctx) {
        Session session = ctx.session();

        // If the person logged in, we need to check their info
        try {
            // The user is an admin (uuid different to normal)
            if(session.get("uuid").toString().equals("R8EckqhZ0Vwx2RoJfKEDyXFyqd9Q2ufFiIU8")) {
                session.put("is_admin", true);

                ctx.sendRedirect("/admin/dashboard");
            }

            // Normal uuid
            else {
                session.put("is_admin", false);
                ctx.sendRedirect("/account/dashboard");
            }
        }

        // If we encounter an issue, send the user back to the login page
        catch(MissingValueException e) {
            log.error("Something went wrong during user redirect");

            session.put("login_redirect", 2);
            ctx.sendRedirect("/");
        }
    }

    /** Build & deploy the sign-up page so the user can sign-up to access their details
     * @param ctx The current context
     * @return The model to build & deploy
    */
    @GET("/signup")
    public ModelAndView<Map<String, Object>> getSignup(Context ctx) {
        Session session = ctx.session();

        try {
            // If the user is logged in & an admin, redirect to admin dashboard
            if(session.get("logged_in").booleanValue() && session.get("is_admin").booleanValue()) {
                ctx.sendRedirect("/admin/dashboard");
                return null;
            }

            // If the user is not an admin, redirect to normal account dashboard
            else if(session.get("logged_in").booleanValue() && !session.get("is_admin").booleanValue()) {
                ctx.sendRedirect("/account/dashboard");
                return null;
            }

            // Get redirect code (if any)
            int redirect_code = session.get("signup_redirect").intValue();

            // Make the model, this will be empty if there is no errors
            Map<String, Object> model = new HashMap<>();

            String error_message = this.getSignupErrorMessage(redirect_code);

            // If there's an error message, then build & return the ModelAndView
            if(error_message != null) {
                model.put("signup_error", "There was an error whilst signing up");
                model.put("signup_message", error_message);

                session.remove("signup_redirect");

                return new ModelAndView<>("auth/signup.hbs", model);
            }
        }

        catch(MissingValueException ignored) {}

        // If we pass all cases, then simply show the standard sign up page
        return new ModelAndView<>("auth/signup.hbs", new HashMap<>());
    }

    /** Authenticate sign-up details against the database before creating a new row in it & redirecting
     * @param ctx The current context
     * @param username The username from the form
     * @param password The password from the from
     * @param confirm_password The verification password from the form
     * @param uuid The uuid from the form
    */
    @POST("/signup/auth")
    public void authSignup(Context ctx, String username, String password, String confirm_password, String uuid) {
        Session session = ctx.session();

        try(Connection conn = ds.getConnection()) {
            // Get booleans for all valid user inputs
            boolean valid_password = password.equals(confirm_password);
            boolean valid_username = this.isUsernameUnique(conn, username);
            boolean uuid_not_in_users = !this.isUUIDInUsers(conn, uuid);
            boolean uuid_in_accounts = this.isUUIDInAccounts(conn, uuid);

            // Check for issues, if any arise then change the redirect code

            int redirect_code = 0;

            if(!valid_username) { redirect_code = 1; } // Username already exists (need unique username)
            else if(!valid_password) { redirect_code = 2; } // Password & confirm password do not match
            else if(!uuid_not_in_users) { redirect_code = 3; } // UUID already signed up under Users table
            else if(!uuid_in_accounts) { redirect_code = 4; } // UUID not in Accounts table (no account for specific user)

            // If there has been an issue with 1 of the required fields
            if(redirect_code != 0) {
                session.put("signup_redirect", redirect_code);
                ctx.sendRedirect("/signup");
                return;
            }

            // No issues, proceed with signing in
            else {
                PreparedStatement insert_user = conn.prepareStatement("INSERT INTO Users VALUES (?, ?, ?)");

                insert_user.setString(1, uuid);
                insert_user.setString(2, username);

                MessageDigest hashing_instance = MessageDigest.getInstance("SHA-256");
                BigInteger hashed_password = new BigInteger(1, hashing_instance.digest(password.getBytes()));

                insert_user.setString(3, hashed_password.toString());

                insert_user.executeUpdate();

                session.put("signup_success", true);

                ctx.sendRedirect("/");
                return;
            }
        }

        catch(SQLException e) {
            log.error("Error whilst checking sign up details with database", e);
        }

        catch(NoSuchAlgorithmException e) {
            log.error("Somehow the hashing algorithm does not exist :(", e);
        }

        // If something goes wrong, send a generic error message
        session.put("signup_redirect", 5);
        ctx.sendRedirect("/signup");
    }

    /** Deploy the error page to the endpoint
     * @param ctx The current context
     * @return The model to build & deploy
     */
    @GET("/error")
    public ModelAndView<Map<String, Object>> deployErrorPage(Context ctx) {
        return this.buildErrorPage(ctx);
    }

    /** Check whether a specific username already exists inside the Users table
     * @param conn The connection to the database
     * @param username The username to check for
     * @return Whether the username is unique
     * @throws SQLException If we fail a database connection/query
    */
    private boolean isUsernameUnique(Connection conn, String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT Username FROM Users WHERE Username = ?");
        stmt.setString(1, username);

        ResultSet set = stmt.executeQuery();

        // If the username is unique
        return !set.next();
    }

    /** Check whether a specific uuid is inside the Users table
     * @param conn The connection to the database
     * @param uuid The uuid to check for
     * @return Whether the uuid exists inside the Users table
     * @throws SQLException If we fail a database connection/query
    */
    private boolean isUUIDInUsers(Connection conn, String uuid) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT UUID FROM Users WHERE UUID = ?");
        stmt.setString(1, uuid);

        ResultSet set = stmt.executeQuery();

        return set.next();
    }

    /** Check whether a specific uuid is inside the Accounts table
     * @param conn The connection to the database
     * @param uuid The uuid to check for
     * @return Whether the uuid exists inside the Accounts table
     * @throws SQLException If we fail a database connection/query
    */
    private boolean isUUIDInAccounts(Connection conn, String uuid) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT UUID FROM Accounts WHERE UUID = ?");
        stmt.setString(1, uuid);

        ResultSet set = stmt.executeQuery();

        return set.next();
    }

    /** Return a specific login error message depending on the redirect code
     * @param redirect_code The code for the error
     * @return An error message for the user
    */
    private String getLoginErrorMessage(int redirect_code) {
        return switch (redirect_code) {
            case 1 -> "Please try again in a few minutes";
            case 2 -> "You need to log in first!";
            case 3 -> "Invalid username/password!";
            case 4 -> "You can't access that page!";
            default -> null;
        };
    }

    /** Return a specific sign-up error message depending on the redirect code
     * @param redirect_code The code for the error
     * @return An error message for the user
    */
    private String getSignupErrorMessage(int redirect_code) {
        return switch (redirect_code) {
            case 1 -> "Your username is already taken!";
            case 2 -> "Your passwords don't match!";
            case 3 -> "You've already signed up!";
            case 4 -> "You don't have an account with us!";
            case 5 -> "Please try again in a few minutes!";
            default -> null;
        };
    }
}