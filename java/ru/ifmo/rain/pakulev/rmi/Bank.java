package ru.ifmo.rain.pakulev.rmi;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    Account createAccount(String id) throws RemoteException;

    Account getAccount(String id) throws RemoteException;

    Person createPerson(String firstName, String secondName, int pasportNumber) throws RemoteException;

    Person getPerson(int passportNumber) throws RemoteException;
}
