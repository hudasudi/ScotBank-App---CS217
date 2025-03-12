package uk.co.asepstrath.bank.view;

import io.jooby.ModelAndView;
import io.jooby.annotation.Path;
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

    protected ModelAndView<Map<String, Object>> buildErrorPage(String error, String msg) {
        Map<String, Object> map = new HashMap<>();

        map.put("err", error);
        map.put("msg", msg);

        return new ModelAndView<>("error.hbs", map);
    }
}