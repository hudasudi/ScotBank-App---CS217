package uk.co.asepstrath.bank.controller_tests.Unit;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.ValueNode;
import io.jooby.exception.MissingValueException;
import io.jooby.internal.MissingValue;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.view.AuthenticationController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticationControllerTests {

    @Test
    public void getLoginSuccessful(){
        DataSource mockDataSource = mock(DataSource.class);
        AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDataSource);

        Context ctx = mock(Context.class);
        Session sess = mock(Session.class);
        ValueNode valueNode = mock(ValueNode.class);

        when(ctx.session()).thenReturn(sess);
        when(valueNode.booleanValue()).thenReturn(true);
        when(sess.get("signup_success")).thenReturn(valueNode);
        when(sess.remove("signup_success")).thenReturn(valueNode);

        ModelAndView<Map<String, Object>> model = control.getLogin(ctx);
        assertNotNull(model);
        Map<String,Object> map = model.getModel();
        assertTrue(map.containsKey("success_header"));

    }

    @Test
    public void getLoginAlreadyLoggedIn(){
        DataSource mockDataSource = mock(DataSource.class);
        AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDataSource);

        Context ctx = mock(Context.class);
        Session sess = mock(Session.class);
        ValueNode valueNode = mock(ValueNode.class);

        when(ctx.session()).thenReturn(sess);
        when(valueNode.booleanValue()).thenReturn(true);
        when(sess.get("signup_success")).thenThrow(new MissingValueException("ignored"));
        when(sess.get("logged_in")).thenReturn(valueNode);

        ModelAndView<Map<String, Object>> model = control.getLogin(ctx);
        assertNull(model);


    }

    @Test
    public void getLoginRedirect(){
        DataSource mockDataSource = mock(DataSource.class);
        AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDataSource);

        Context ctx = mock(Context.class);
        Session sess = mock(Session.class);
        ValueNode valueNode = mock(ValueNode.class);

        when(ctx.session()).thenReturn(sess);
        when(valueNode.booleanValue()).thenReturn(false);
        when(valueNode.intValue()).thenReturn(1);
        when(sess.get("signup_success")).thenThrow(new MissingValueException("ignored"));
        when(sess.get("logged_in")).thenReturn(valueNode);
        when(sess.get("login_redirect")).thenReturn(valueNode);
        when(sess.remove("signup_success")).thenReturn(valueNode);
        when(sess.remove("login_redirect")).thenReturn(valueNode);

        ModelAndView<Map<String, Object>> model = control.getLogin(ctx);
        assertNotNull(model);
        Map<String,Object> map = model.getModel();
        assertTrue(map.containsKey("login_error"));

    }

    @Test
    public void successfulLogin(){
        try {
            DataSource mockDataSource = mock(DataSource.class);
            AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDataSource);

            Connection conn = mock(Connection.class);
            PreparedStatement stmt = mock(PreparedStatement.class);
            ResultSet set = mock(ResultSet.class);

            when(mockDataSource.getConnection()).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(set);
            when(set.next()).thenReturn(true);

            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(ctx.session()).thenReturn(sess);
            when(ctx.sendRedirect(anyString())).thenThrow(new UnknownError());

            control.authLogin(ctx, "username", "password");
        } catch(UnknownError e){
            assertTrue(true);
        } catch (Exception e){
            fail();
        }

    }

    @Test
    public void redirectAdmin(){
        try {
            DataSource mockDataSource = mock(DataSource.class);
            AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDataSource);


            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(valueNode.toString()).thenReturn("R8EckqhZ0Vwx2RoJfKEDyXFyqd9Q2ufFiIU8");
            when(ctx.session()).thenReturn(sess);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(ctx.sendRedirect(anyString())).thenThrow(new UnknownError());

            control.redirectUser(ctx);
        } catch(UnknownError e){
            assertTrue(true);
        } catch (Exception e){
            fail();
        }
    }

    @Test
    public void redirectNormalUser(){
        try {
            DataSource mockDataSource = mock(DataSource.class);
            AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDataSource);


            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);
            ValueNode valueNode = mock(ValueNode.class);

            when(valueNode.toString()).thenReturn("string");
            when(ctx.session()).thenReturn(sess);
            when(sess.get("uuid")).thenReturn(valueNode);
            when(ctx.sendRedirect(anyString())).thenThrow(new UnknownError());

            control.redirectUser(ctx);
        } catch(UnknownError e){
            assertTrue(true);
        } catch (Exception e){
            fail();
        }
    }

    @Test
    public void signUpLoggedInAsAdmin(){
        DataSource mockDataSource = mock(DataSource.class);
        AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDataSource);


        Context ctx = mock(Context.class);
        Session sess = mock(Session.class);
        ValueNode valueNode = mock(ValueNode.class);

        when(valueNode.booleanValue()).thenReturn(true);
        when(ctx.session()).thenReturn(sess);
        when(sess.get("logged_in")).thenReturn(valueNode);
        when(sess.get("is_admin")).thenReturn(valueNode);

        ModelAndView<Map<String, Object>> model = control.getSignup(ctx);
        assertNull(model);

    }

    @Test
    public void signUpLoggedInAsRegularUser(){
        DataSource mockDataSource = mock(DataSource.class);
        AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDataSource);


        Context ctx = mock(Context.class);
        Session sess = mock(Session.class);
        ValueNode valueNode = mock(ValueNode.class);
        ValueNode valueNode1 = mock(ValueNode.class);

        when(valueNode.booleanValue()).thenReturn(true);
        when(valueNode1.booleanValue()).thenReturn(false);
        when(ctx.session()).thenReturn(sess);
        when(sess.get("logged_in")).thenReturn(valueNode);
        when(sess.get("is_admin")).thenReturn(valueNode1);

        ModelAndView<Map<String, Object>> model = control.getSignup(ctx);
        assertNull(model);

    }

    @Test
    public void signUpError(){
        DataSource mockDataSource = mock(DataSource.class);
        AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDataSource);


        Context ctx = mock(Context.class);
        Session sess = mock(Session.class);
        ValueNode valueNode = mock(ValueNode.class);
        ValueNode valueNode1 = mock(ValueNode.class);

        when(valueNode.booleanValue()).thenReturn(true);
        when(valueNode1.booleanValue()).thenReturn(false);
        when(valueNode.intValue()).thenReturn(2);
        when(ctx.session()).thenReturn(sess);
        when(sess.get("logged_in")).thenReturn(valueNode1);
        when(sess.get("is_admin")).thenReturn(valueNode1);
        when(sess.get("signup_redirect")).thenReturn(valueNode);
        when(sess.remove("signup_redirect")).thenReturn(valueNode);

        ModelAndView<Map<String, Object>> model = control.getSignup(ctx);
        Map<String,Object> map = model.getModel();

        assertTrue(map.containsKey("signup_error"));
    }

    @Test
    public void signUpNormal(){
        DataSource mockDataSource = mock(DataSource.class);
        AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDataSource);


        Context ctx = mock(Context.class);
        Session sess = mock(Session.class);

        when(ctx.session()).thenReturn(sess);
        when(sess.get("logged_in")).thenThrow(new MissingValueException("ignored"));


        ModelAndView<Map<String, Object>> model = control.getSignup(ctx);
        Map<String,Object> map = model.getModel();

        assertTrue(map.isEmpty());
    }

    @Test
    public void signUpAuth(){
        try {
            DataSource mockDs = mock(DataSource.class);
            AuthenticationController control = new AuthenticationController(mock(Logger.class), mockDs);

            Connection conn = mock(Connection.class);
            PreparedStatement stmt = mock(PreparedStatement.class);
            PreparedStatement stmt2 = mock(PreparedStatement.class);
            PreparedStatement stmt3 = mock(PreparedStatement.class);
            ResultSet set = mock(ResultSet.class);
            ResultSet set2 = mock(ResultSet.class);

            when(mockDs.getConnection()).thenReturn(conn);
            when(conn.prepareStatement("SELECT Username FROM Users WHERE Username = ?")).thenReturn(stmt2);
            when(stmt2.executeQuery()).thenReturn(set2);
            when(conn.prepareStatement("SELECT UUID FROM Users WHERE UUID = ?")).thenReturn(stmt2);
            when(conn.prepareStatement("SELECT UUID FROM Accounts WHERE UUID = ?")).thenReturn(stmt);
            when(stmt.executeQuery()).thenReturn(set);
            when(set2.next()).thenReturn(false);
            when(set.next()).thenReturn(true);

            when(conn.prepareStatement("INSERT INTO Users VALUES (?, ?, ?)")).thenReturn(stmt3);
            when(stmt3.executeUpdate()).thenReturn(0);


            Context ctx = mock(Context.class);
            Session sess = mock(Session.class);

            when(ctx.session()).thenReturn(sess);

            control.authSignup(ctx,"username","password","password","uuid");

            when(ctx.sendRedirect(anyString())).thenThrow(new UnknownError());
        } catch(UnknownError e){
            assertTrue(true);
        } catch(Exception e){
            fail();
        }
    }

}
