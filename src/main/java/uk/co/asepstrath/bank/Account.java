package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Account {
    private final String id;
    private final String name;
    private BigDecimal balance;
    private final boolean roundUpEnabled;

    /** A basic class for Account information
     * @param id The Account UUID
     * @param name The Account holders name
     * @param startingBalance The Account's starting balance
     * @param roundUpEnabled Whether we round an Account's balance up
    */
    public Account(String id, String name, BigDecimal startingBalance, Boolean roundUpEnabled) {
        this.id = id;
        this.name = name;
        this.balance = startingBalance;
        this.roundUpEnabled = roundUpEnabled;

        this.balance = (this.roundUpEnabled)
                ? this.balance.setScale(2, RoundingMode.HALF_UP)
                : this.balance.setScale(2, RoundingMode.HALF_DOWN);
    }

    /** Deposit money into an Account
     * @param amount The amount to deposit
    */
    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.balance = this.balance.setScale(2, RoundingMode.HALF_UP);
    }

    /** Withdraw an amount from the Account
     * @param amount The amount to withdraw
     * @throws ArithmeticException Thrown during exceptional arithmetic conditions
    */
    public void withdraw(BigDecimal amount, boolean is_overdraft) throws ArithmeticException {
        if(is_overdraft) {
            this.balance = this.balance.subtract(amount);
        }

        else {
            if(this.balance.subtract(amount).compareTo(BigDecimal.valueOf(0)) >= 0) {
                this.balance = this.balance.subtract(amount);
            }

            else {
                throw new ArithmeticException();
            }
        }
    }

    /** Get an accounts balance
     * @return The accounts balance
    */
    public BigDecimal getBalance() {
        this.balance = this.balance.setScale(2, RoundingMode.HALF_DOWN);

        return this.balance;
    }

    /** Get an Account holder's name
     * @return The Account holder's name
    */
    public String getName() { return this.name; }

    public String getID() { return this.id; }

    public boolean isRoundUpEnabled() { return this.roundUpEnabled; }

    /** Stringify an Account's information
     * @return A String holding Account information
    */
    public String toString() {
        this.balance = this.balance.setScale(2,RoundingMode.HALF_DOWN);
        return "Name: " + this.name + " \nBalance: " + this.balance;
    }
}