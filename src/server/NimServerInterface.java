/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import client.NimClientInterface;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Chris
 */
public interface NimServerInterface extends Remote {
    
    /**
     * Adds player to the queue
     * @param player the player joining the server
     * @throws java.rmi.RemoteException
     */
    public void addPlayer(NimClientInterface player) throws RemoteException;
    //public void addPlayer(NimClientInterface humanPlayer) throws RemoteException;
    
    /**
     * Adds a waiting player to the queue
     * @param playerKey Unique key representing the player in the gamers HashMap 
     * @throws java.rmi.RemoteException 
     */
    public void queuePlayer(int playerKey) throws RemoteException;
    
    /**
     * Player does not wish to stay in the game
     * Removes the required player from the lobby
     * @param playerCode
     * @throws RemoteException
     */
    public void leaveLobby(int playerCode) throws RemoteException;

    /**
     * Simple testing output to console
     * @throws RemoteException
     */
    public void ready() throws RemoteException;
}
