package ru.ifmo.rain.pakulev.rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote {
    String getFirstName() throws RemoteException;

    String getSecondName() throws RemoteException;

    int getPasportNumber() throws RemoteException;
}
