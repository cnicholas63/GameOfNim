/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import java.rmi.RemoteException;

/**
 *
 * @author Chris
 */
public interface PlayerInterface {

    /**
     * Returns the player's name
     * @return player's name
     * @throws java.rmi.RemoteException
     */
    public String getName() throws RemoteException;
    
    /**
     * Returns player type
     * @return true = Human player
     * @throws java.rmi.RemoteException
     */
    public boolean getIsHuman() throws RemoteException;
    
    /**
     * Returns the game difficulty 
     * @return player difficulty
     * @throws java.rmi.RemoteException
     */
    public int getDifficulty() throws RemoteException;
    
    /** Returns the chosen opponent type
     * 
     * @return The type of opponent HUMAN or COMPUTER
     * @throws RemoteException 
     */
    public int getOpponentType() throws RemoteException;
        
    /**
     * Sets inGame flag
     * 
     * @param inGameFlag sets the in game status, True = in game, False not in game
     * @throws RemoteException 
     */
    public void setInGameStatus(boolean inGameFlag) throws RemoteException;
    
    /**
     * Returns the in game status of the player
     * @return true = in game, false not in game
     * @throws java.rmi.RemoteException
     */
    public boolean getInGameStatus() throws RemoteException;

    /**
     * Sets the player's code - unique ID for the player
     * @param playerCode
     * @throws java.rmi.RemoteException
     */
    public void setPlayerCode(int playerCode) throws RemoteException;
    
    /**
     * Gets the game code for this player
     * @return player's unique code
     * @throws java.rmi.RemoteException
     */
    public int getPlayerCode() throws RemoteException;

    /**
    * Gets the number of marbles the player wishes to take
    * @param bagSize The number of marbles in the bag
    * @return The number of marbles to take
     * @throws java.rmi.RemoteException
    */
    public int getMarbles(int bagSize) throws RemoteException;
    
    /**
     * Indicates to the player that they have won the game
     * @throws java.rmi.RemoteException
     */
    public void won() throws RemoteException;
    
    /**
     * Indicates to the player that they have lost
     * @throws java.rmi.RemoteException
     */
    public void lost() throws RemoteException;
    
    /**
     * Ask the player if they would like to play again
     * @return True = Yes, False = No
     * @throws java.rmi.RemoteException
     */
    public boolean playAgain() throws RemoteException;
}

