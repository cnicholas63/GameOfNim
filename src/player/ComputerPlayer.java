
/**
 * @author Chris Nicholas
 * Class describes computer player behaviour
 * 
 */

package player;

import java.util.Random;
import constants.Constants;
import java.rmi.RemoteException;

public class ComputerPlayer implements PlayerInterface {
    private String name = "Default Player"; // Default players name
    private int playerType; 
    private int difficulty;
    private int opponentType;
    private boolean inGame;  // In game flag
    private int gameCode;    // Game code used in lobby to ensure unique player
    
    Random rand = new Random(); // random number generator
   
    /**
     * Constructor
     * @param name Name of the player
     * @param difficulty difficulty level of player (for AI player)
     */
    public ComputerPlayer(String name, int difficulty) {
        this.name = name;
        this.difficulty = difficulty;
        playerType = Constants.COMPUTER_PLAYER; // This is a computer player
        opponentType = Constants.HUMAN_PLAYER;  // Computer must be playing against a human
        gameCode = -1; // Not used but needed
    }
    
    /**
     * Returns player type
     * @return false = Computer player
     */
    @Override
    public boolean getIsHuman() {
        return false;
    }
    
    /** Returns the chosen opponent type
     *  @return The type of opponent HUMAN or COMPUTER
     */
    @Override
    public int getOpponentType() {
        return opponentType;
    }
            
    // Returns the game difficulty 
    @Override
    public int getDifficulty() {
        return difficulty;
    }
            
    /**
     * Gets the game code for this player
     * @return player's game code
     */
    @Override
    public int getPlayerCode() {
        return gameCode;
    }

    /**
     * Sets the player's game code
     * @param gameCode
     */
    @Override
    public void setPlayerCode(int gameCode) {
        this.gameCode = gameCode;
    }
    
    /**
    * Gets the number of marbles the player wishes to take
    * @param bagSize The number of marbles in the bag
    * @return The number of marbles to take
    */
    @Override
    public int getMarbles(int bagSize) {
        int marbles;
        int marbleMax; // Used to calculate the maximum number of marbles that can be taken
        int pot = 0; // Used to calculate maximum n^2 within the bag
        
        if(bagSize < 2) { // This is an error there should be atleas 2 marbles
            System.out.println("Error - too few marbles remaining");
            return 0;
        }
        
        marbleMax = bagSize / 2; // The maximum number of marbles that can be taken
        
        // Easy difficulty, just take a random amount of marbles
        if(difficulty == Constants.EASY) { 
            return rand.nextInt(marbleMax) + 1; // Take random number of marbles - atleast 1 at most marbleMax
        }
        
        /* 
           Hard game - calculate possible moves
           If possible, we want to leave the number of remaining marbles as n^2-1
           But we cannot take more than half of the marbles
        */
        
        // Find maximum n^2 within bagSize
        for(int count = 2; count <= bagSize; count *= 2) 
            pot = count;

        // Check if the bagSize is already n^2-1 as this will alter the legal move choice
        if(bagSize == (pot * 2 - 1)) {
            marbles = rand.nextInt(marbleMax) + 1; // Take random number of marbles - atleast 1 at most marbleMax
        }
        else { // Take enough marbles to leave the bagSize as n^2-1
            marbles = bagSize - pot + 1;
        }
        
        return marbles;
    }
    
    /**
     * Returns the in game status of the player
     * @return true = in game, false not in game
     */
    @Override
    public boolean getInGameStatus() {
        return inGame;
    }
    
    /**
     * Sets inGame flag
     * @throws RemoteException 
     */
    @Override
    public void setInGameStatus(boolean inGameFlag) throws RemoteException {
        // Empty method - nothing to do for the computer player
    }
    
    /**
     * Returns the players name
     * @return Players name
     */
    @Override
    public String getName() {
        return name;
    }
          
    /**
     * Indicates to the player that they have won the game
     */
    @Override
    public void won() {
        // Empty method - nothing to do for the computer player
    }
    
    /**
     * Indicates to the player that they have lost
     */
    @Override
    public void lost() {
        // Empty method - nothing to do for the computer player
    }
    
    @Override
    public boolean playAgain() {
        return false;
    }
    
}