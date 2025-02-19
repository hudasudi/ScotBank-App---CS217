package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Account {

    private final String id;
    private final String name;
    private BigDecimal balance;
    private boolean roundUpEnabled;

    public Account(String id, String name, BigDecimal startingBalance, Boolean roundUpEnabled){
        balance = startingBalance;
        this.name = name;
        this.id = id;
        if(roundUpEnabled){balance = balance.setScale(2, RoundingMode.HALF_UP);}
        else{balance = balance.setScale(2, RoundingMode.HALF_DOWN);}
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
