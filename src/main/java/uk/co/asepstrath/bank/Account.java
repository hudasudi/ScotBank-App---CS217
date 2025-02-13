package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Account {

    private final String id;
    private final String name;
    private BigDecimal balance;
    private boolean roundUpEnabled;

    public Account(String id, String name, BigDecimal startingBalance, Boolean roundUpEnabled){
        this.id = id;
        this.name = name;
        this.balance = startingBalance;

        if(roundUpEnabled) { balance = balance.setScale(2, RoundingMode.HALF_UP); }
        else{ balance = balance.setScale(2, RoundingMode.HALF_DOWN); }
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.balance = this.balance.setScale(2, RoundingMode.HALF_UP);
    }

    public void withdraw(BigDecimal amount) throws ArithmeticException {
        if(this.balance.subtract(amount).compareTo(BigDecimal.valueOf(0)) >= 0) {
            this.balance = this.balance.subtract(amount);
        } else {
            throw new ArithmeticException();
        }
    }

    public BigDecimal getBalance() {
        System.out.println(balance);

        return balance;
    }

    public String getName() { return name; }

    public String toString() {
        return "Name: " + name + " \nBalance: " + balance;
    }
}
