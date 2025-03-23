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

}
