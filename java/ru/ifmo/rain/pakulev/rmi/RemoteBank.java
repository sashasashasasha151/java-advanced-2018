package ru.ifmo.rain.pakulev.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Person> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    public Account createAccount(final String id) throws RemoteException {
        System.out.println("Creating account " + id);
        final Account account = new RemoteAccount(id);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    public Person createPerson(String firstName, String secondName, int passportNumber) throws RemoteException {
        System.out.println("Creating person " + passportNumber);
        final Person person = new RemotePerson(firstName,secondName,passportNumber);
        if (persons.putIfAbsent(passportNumber, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return getPerson(passportNumber);
        }
    }

    public Person getPerson(int passportNumber) throws RemoteException {
        System.out.println("Retrieving person " + passportNumber);
        return persons.get(passportNumber);
    }

}
