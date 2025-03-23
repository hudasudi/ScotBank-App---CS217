package uk.co.asepstrath.bank.view;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.annotation.Path;
import io.jooby.exception.MissingValueException;
import org.slf4j.Logger;

import uk.co.asepstrath.bank.api.manipulators.AccountAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.BusinessAPIManipulator;
import uk.co.asepstrath.bank.api.manipulators.TransactionAPIManipulator;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public abstract class Controller {
    protected Logger log;
    protected DataSource ds;
    protected AccountAPIManipulator account_manipulator;
    protected BusinessAPIManipulator business_manipulator;
    protected TransactionAPIManipulator transaction_manipulator;

    public Controller(Logger log, DataSource ds) {
        this.log = log;
        this.ds = ds;

        this.account_manipulator = new AccountAPIManipulator(log, ds);
        this.business_manipulator = new BusinessAPIManipulator(log, ds);
        this.transaction_manipulator = new TransactionAPIManipulator(log, ds);
    }

    // FOR TESTING

    public void setBusinessAPIManipulator(BusinessAPIManipulator manipulator) {
        this.business_manipulator = manipulator;
    }

    public void setAccountAPIManipulator(AccountAPIManipulator manipulator) {
        this.account_manipulator = manipulator;
    }

    public void setTransactionAPIManipulator(TransactionAPIManipulator manipulator) {
        this.transaction_manipulator = manipulator;
    }

    protected ModelAndView<Map<String, Object>> buildErrorPage(Context ctx) {
        Session session = ctx.session();

        try {
            String error = session.get("page_error").toString();
            String msg = session.get("page_msg").toString();

            // If there is a valid error
            if(!error.equals("<missing>") && !msg.equals("<missing>")) {
                Map<String, Object> map = new HashMap<>();

                map.put("err", error);
                map.put("msg", msg);

                // Reset error info
                session.remove("page_error");
                session.remove("page_msg");

                return new ModelAndView<>("error.hbs", map);
            }

            // There is no error
            else {
                session.put("login_redirect", 4);

                ctx.sendRedirect("/");
                return null;
            }
        }

        // No error message
        catch(MissingValueException e) {
            session.put("login_redirect", 4);
            ctx.sendRedirect("/");
            return null;
        }
    }
}