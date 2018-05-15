/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import player.PlayerInterface;

/**
 *
 * @author Chris
 */
public interface NimClientInterface extends Remote, PlayerInterface {
    
    /**
     * Displays message from server
     * @param message String containing message to display
     */
    public void serverMessage(String message) throws RemoteException;
    
    /**
     * Simple joined server confirmation message
     */
    public void joinedServer() throws RemoteException; 
}
