package ru.ifmo.rain.pakulev.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
    public static void main(final String... args) throws RemoteException {
        if (args == null) {
            System.err.println("Null arguments error");
            return;
        }

        if (args.length != 5) {
            System.err.println("Five arguments expected");
            return;
        }

        for (String arg : args) {
            if (arg == null) {
                System.err.println("Null argument");
                return;
            }
        }

        String firstName;
        String secondName;
        int passportNumber;
        int bill;
        int value;
        try {
            firstName = args[0];
            secondName = args[1];
            passportNumber = Integer.parseInt(args[2]);
            bill = Integer.parseInt(args[3]);
            value = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println(e.toString());
            return;
        }

        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        Person person = bank.getPerson(passportNumber);
        if (person == null) {
            System.out.println("Creating new person");
            person = bank.createPerson(firstName, secondName, passportNumber);
        }

        if(person.getFirstName().equals(firstName) && person.getSecondName().equals(secondName)) {
            System.out.println("Welcome: " + firstName + " " + secondName);
        } else {
            System.err.println("Wrong persons data");
            return;
        }

        Account account = bank.getAccount(Integer.toString(passportNumber) + ":" + Integer.toString(bill));
        if (account == null) {
            System.out.println("Creating bill: " + bill);
            account = bank.createAccount(Integer.toString(passportNumber) + ":" + Integer.toString(bill));
        }

        System.out.println("Bill: " + bill);
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.setAmount(account.getAmount() + value);
        System.out.println("Money: " + account.getAmount());
    }
}
