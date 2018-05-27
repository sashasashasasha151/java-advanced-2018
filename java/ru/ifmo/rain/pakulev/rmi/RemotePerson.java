package ru.ifmo.rain.pakulev.rmi;

import java.rmi.RemoteException;

public class RemotePerson implements Person {
    private String firstName;
    private String secondName;
    private int pasportNumber;

    public RemotePerson(String firstName, String secondName, int pasportNumber) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.pasportNumber = pasportNumber;
    }

    public synchronized String getFirstName() throws RemoteException {
        return firstName;
    }

    public synchronized String getSecondName() throws RemoteException {
        return secondName;
    }

    public synchronized int getPasportNumber() throws RemoteException {
        return pasportNumber;
    }
}
