package uk.co.asepstrath.bank.object_tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.asepstrath.bank.Account;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AccountTests {
    private Account a;

    @BeforeEach
    void setUp() {
        a = new Account("ID","NAME",BigDecimal.ZERO,true);
    }

    @Test
    public void createAccount() {
        assertNotNull(a);
    }

    @Test
    public void initiallyZero() {
        assertEquals(a.getBalance(), BigDecimal.valueOf(0.00).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    public void addFifty() {
        a.deposit( BigDecimal.valueOf(20));
        a.deposit(BigDecimal.valueOf(50));
        assertEquals(a.getBalance(), BigDecimal.valueOf(70).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    public void withdrawNormal() {
        a.deposit(BigDecimal.valueOf(40));
        a.withdraw(BigDecimal.valueOf(20), false);
        assertEquals(a.getBalance(),BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    public void overdraft() {
        a.deposit(BigDecimal.valueOf(30));
        assertThrows(ArithmeticException.class, () -> a.withdraw(BigDecimal.valueOf(100), false));
    }

    @Test
    public void superSaving() {
        a.deposit(BigDecimal.valueOf(20));
        for(int i = 0; i < 5; i++){
            a.deposit(BigDecimal.valueOf(10));
        }
        for(int i = 0; i < 3; i++){
            a.withdraw(BigDecimal.valueOf(20), false);
        }
        assertEquals(a.getBalance(),BigDecimal.valueOf(10).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    public void countingPennies() {
        a.deposit(BigDecimal.valueOf(17.56));
        a.deposit(BigDecimal.valueOf(5.45));
        assertEquals(a.getBalance(),BigDecimal.valueOf(23.01).setScale(2, RoundingMode.HALF_UP));
    }


    @Test
    public void roundUpTrue() {
        a = new Account("ID","NAME",BigDecimal.ZERO,true);
        a.deposit(BigDecimal.valueOf(2.55555));
        assertEquals(BigDecimal.valueOf(2.56),a.getBalance());
    }

    @Test
    public void nameTest() {
        assertEquals("NAME", a.getName());
    }

    @Test
    public void toStringTest() {
        assertEquals("Name: NAME \nBalance: 0.00",a.toString());
    }

    @Test
    public void checkIsRoundUpEnabled() {
        assertTrue(a.isRoundUpEnabled());
    }

    @Test
    public void checkWithdrawWithOverdraft() {
        a.deposit(BigDecimal.valueOf(20));
        a.withdraw(BigDecimal.valueOf(20), true);
        assertEquals(0, a.getBalance().compareTo(BigDecimal.ZERO));
    }
}
