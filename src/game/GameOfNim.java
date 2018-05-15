/**
 * GameOfNim class holds game logic - controlling moves, deciding when game is won or lost
 */
package game;

import client.NimClientInterface;
import constants.Constants;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import player.PlayerInterface;
import server.NimServerInterface;
import server.NimServerLobby;


/**
 * Game logic, controls interactions between two players
 * @author Chris
 */
public class GameOfNim implements Runnable {
    private Random rand = new Random(); // Used to simulate coin toss
    private int player;                 // Holds the current player (0 or 1)
    private int marbles;                // The number of marbles in the bag
    private boolean won = false;        // Game won flag
    private int difficulty;             // Difficulty level for the game
    private PlayerInterface[] players = new PlayerInterface[2]; // Array for players
    NimServerInterface lobby;
    ConcurrentHashMap<Integer, NimClientInterface> queue;

    /**
     * Constructs the game based on difficulty
     * @param p1 Player 1
     * @param p2 Player 2
     * @param difficulty Difficulty - EASY or HARD
     * @param lobby Reference back to the lobby. Used when asking players if they wish to play again.
     */
    public GameOfNim(PlayerInterface p1, PlayerInterface p2, int difficulty, NimServerLobby lobby) {
        players[0] = p1; // Initialise first player
        players[1] = p2; // Initialise second player   
        this.difficulty = difficulty; // Set difficulty
        this.lobby = lobby; // Reference to the lobby

        // Decide which player will start the game - coin toss
        player = rand.nextInt(2); // returns value between 0 and 1 inclusive
        
        // Initialise the number of marbles at the start
        // Needs to be atleast 5 to give the game the possibility of several moves
        marbles = rand.nextInt(difficulty == Constants.EASY ? 19 : 99) + 5; 
        
        try {
            System.out.println("Game on: " + p1.getName() + " v " + p2.getName());
        } catch (RemoteException ex) {
            Logger.getLogger(GameOfNim.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (int t = 0; t < 2; t++) {
            try {
                if (players[t].getIsHuman()) {
                    ((NimClientInterface) players[t]).serverMessage("\nStarting new game. There are " + marbles + " marbles");
                    ((NimClientInterface) players[t]).serverMessage("First player is: " + players[player].getName());
                }
            } catch (RemoteException e) {
                System.out.println("Error sending output to player console: " + e.getMessage());
            }
        }
    }
    
    /**
     * Runs the game engine in its own thread. 
     * This enables multiple games to run concurrently
     */
    @Override
    public void run() {
        int marblesTaken; // The number of marbles taken during a player's turn
        System.out.println("In thread");
        
        // Main game loop. Uses x = 1 - x to track the current player
        do { 
            try {
                marblesTaken = players[player].getMarbles(marbles); // Get marbles choice from player
                marbles -= marblesTaken;
                
                // Output message
                if(players[1 - player].getIsHuman()) // If opponent is human, output the number of marbles taken
                    ((NimClientInterface)players[1 - player]).serverMessage("\n" + players[player].getName() + 
                            " has taken " + marblesTaken + " marbles, leaving " + marbles + " marbles");
                
                if(marbles < 1) { // Error, too many marbles taken in a turn
                    System.out.println("Error taking marbles");
                }
                
            } catch (RemoteException ex) {
                Logger.getLogger(GameOfNim.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(marbles == 1) { // Game has finished, the opponent is left with the last marble
                
                try { // Current player has won the game
                    if(players[player].getIsHuman()) { // Only if human
                        players[player].won();  // Inform player of win
                    } 
                    
                    player = 1 - player;    // Switch player 
                    
                    if(players[player].getIsHuman()) { // Only if human
                        players[player].lost(); // Inform player of loss
                    }
                } catch (RemoteException ex) {
                    Logger.getLogger(GameOfNim.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                won = true; // Set won flag
            }
             
            player = 1 - player; // Switch players - if game is won then this will set back to winner
            
        } while(!won); // Keep looping until the game has been won
        
        
        // Game has ended, see if players wish to play again and clean up before exiting
        for (player = 0; player < 2; player++) {
            try {
                if(players[player].getIsHuman()) {
                    if (players[player].playAgain()) { // If the player wants to play again, tell the lobby
                        players[player].setInGameStatus(false); // Indicate no longer in a game

                        lobby.queuePlayer(players[player].getPlayerCode()); // Add this player to the waitingPlayers queue
                    }
                    else { // If the player doesn't want to play again, tell the lobby to remove them
                        // Remove the player from the queue - the hashMap, player will no longer be registered in the server
                        lobby.leaveLobby(players[player].getPlayerCode());         
                    }
                }
               
            } catch (RemoteException e) {
                System.out.println("Error with game ending: " + e.getMessage());
            }
        }
    }
}
