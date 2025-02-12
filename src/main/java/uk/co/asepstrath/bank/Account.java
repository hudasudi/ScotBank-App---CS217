package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Account {

    private BigDecimal balance;
    private final String name;


    public Account(String name, BigDecimal startValue){
        balance = startValue;
        this.name = name;
        balance = balance.setScale(2, RoundingMode.HALF_UP);
    }

    public Account(String name, double startValue){
        balance = BigDecimal.valueOf(startValue);
        this.name = name;
        balance = balance.setScale(2, RoundingMode.HALF_UP);
    }

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
        balance = balance.setScale(2, RoundingMode.HALF_UP);
    }

    public void withdraw(BigDecimal amount) throws ArithmeticException{
        if(balance.subtract(amount).compareTo(BigDecimal.valueOf(0)) >= 0 ){
            balance = balance.subtract(amount);
        } else{
            throw new ArithmeticException();
        }
    }

    public BigDecimal getBalance() {

        System.out.println(balance);
        return balance;
    }

    public String getName(){return name;}

    public String toString(){
        return "Name: " + name + " \nBalance: " + balance;
    }

}
