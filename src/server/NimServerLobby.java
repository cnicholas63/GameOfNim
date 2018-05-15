/**
 * This class controls the game lobby and is responsible for launching games 
 * when two players of the same difficulty join - or a single player
 * wants to play the computer.
 * 
 * @author Chris
 */

package server;

import client.NimClientInterface;
import constants.Constants;
import game.GameOfNim;
import static java.lang.Thread.sleep;
import player.ComputerPlayer;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import player.PlayerInterface;


public class NimServerLobby extends UnicastRemoteObject implements NimServerInterface {
    // Inner class for waiting players
    class WaitingPlayer {
        Integer key = 0;
        boolean inGame = false;
        
        WaitingPlayer(int key, boolean inGame) {
            this.key = key;
            this.inGame = inGame;
           
        }
    }
    
    static int port = 1099;
    
    // ConcurrentHashMap for holding players who join the lobby - serializable
    private ConcurrentHashMap<Integer, NimClientInterface> gamers;
    // Synchronized list for waiting players - players who are in the lobby waiting for a game
    private final List<WaitingPlayer> waitingPlayers = Collections.synchronizedList(new ArrayList<>());
    
    
    /**
     * Server lobby initialises game lobby
     * @throws RemoteException 
     */
    public NimServerLobby() throws RemoteException {
        
        // Instantiate gamers list (HashMap) for human players
        gamers = new ConcurrentHashMap<>();
        
        initialiseServerLobby(); // Initialise the lobby
        
        lobbyManager(); // Start the lobby manager
    }
       
    /** 
     * Adds a human player to the game lobby
     * @throws java.rmi.RemoteException
     */
    @Override
    public synchronized void addPlayer(NimClientInterface player) throws RemoteException {
        Random rand = new Random();
        int key;
        
        // Generate a unique code for this player and check it doesn't already exist in the lobby
        do { 
            key = rand.nextInt(); 
        } while(gamers.containsKey(key));
        
        player.setPlayerCode(key); // Set the game code for this player
        
        gamers.put(key, player);  // Add the player to the gamers list
        
        queuePlayer(key); // Add the player to the waiting players queue
        
        // Information messages to server and player consoles, confirming player added to lobby
        
        System.out.println("New player: " + player.getName() + ", Code = " + player.getPlayerCode() + 
                           ", Difficulty: " + (player.getDifficulty() == 1 ? "Easy" : "Hard") + 
                           ", Opponent type: " + (player.getOpponentType() == 1 ? "Human" : "Computer"));
              
        player.serverMessage("\nAwaiting opponent...");
        
        System.out.println("Players in lobby:");
        
        for (Integer p : gamers.keySet()) {
            System.out.println(gamers.get(p).getName());
        }
    }
    
    /**
     * Adds a waiting player to the queue
     * @param playerKey Unique key representing the player in the gamers HashMap 
     * @throws java.rmi.RemoteException 
     */
    @Override
    public void queuePlayer(int playerKey) throws RemoteException {
        waitingPlayers.add(new WaitingPlayer(playerKey, false)); 
    }
            
    /**
     * Removes the required player from the lobby
     * @param playerCode
     * @throws RemoteException
     */
    @Override
    public void leaveLobby(int playerCode) throws RemoteException {
        String name = "";
        
        try {
            name = gamers.get(playerCode).getName();
            gamers.get(playerCode).serverMessage("Leaving server.");
        } catch (RemoteException e) {
            System.out.println("Player " + gamers.get(playerCode).getName() + " has left the server");
        }
       
        gamers.remove(playerCode); // Remove the player from the HashMap
        System.out.println("Player " + name + " " + playerCode + " removed from lobby");
    }
    
    /**
     * The lobbyManager looks after pairing opponents and starting new games
     */
    private void lobbyManager(/*NimClientInterface player*/) {
        NimClientInterface player;
        PlayerInterface opponent;
        GameOfNim game;
        WaitingPlayer queuedPlayer;
        WaitingPlayer queuedOpponent;
        List<WaitingPlayer> removalList = new ArrayList<>();
        int iteration = 0;
        
        System.out.println("Entering Lobby Manager");
        
        while(true) { // Main 'game loop', Lobby tries to start games for waiting players
            
            synchronized(waitingPlayers) { // Iterate through the list of waiting players and if possible, give them a game
                
                // Search through waitingPlayers queue for players waiting for a game if found, try to find them an opponent from the queue 
                System.out.println("Players waiting for a game = " + waitingPlayers.size());
                
                for(int waitingPlayer = 0; waitingPlayer < waitingPlayers.size(); waitingPlayer++) {
                    
                    queuedPlayer = waitingPlayers.get(waitingPlayer); // Get the player from the queue
                    
                    if(queuedPlayer.inGame) { // If this player is already in game skip to next player (loop iteration)
                        continue; // A bit naughty - but save a lot of extra code
                    }
                    
                    player = gamers.get(queuedPlayer.key); // Retrieve the player details from the gamer HashMap
                    
                    // Try to pair waiting players with another appropriate player from the waitingPlayers queue
                    try { 
                        if(player.getOpponentType() == Constants.COMPUTER_PLAYER) { // Instantiate new computer player
                            opponent = new ComputerPlayer("Computer Player", player.getDifficulty());  // Construct computer player based on players chosen difficulty
                            
                            queuedPlayer.inGame = true;                                         // Flag as in-game 
                            removalList.add(queuedPlayer);                                      // Record for removal from waitingPlayers queue
                        }
                        else { // player wants to play a human opponent
                            
                            opponent = null; // No opponent yet
                            
                            // Run through the remainder of the waitingPlayers list to see if there is a suitable opponent available
                            for(int tempOpponent = waitingPlayer + 1; tempOpponent < waitingPlayers.size() && opponent == null; tempOpponent++) {
                                queuedOpponent = waitingPlayers.get(tempOpponent); // Get possible opponent from waiting player queue
                                
                                if(!queuedOpponent.inGame) { // if this player is not already in a game, retrieve details and check difficulty
                                    opponent = gamers.get(queuedOpponent.key); // Retrieve the opponents details from the map

                                    // If the two players are not of the same difficulty or opponent type, no match
                                    if((player.getDifficulty() != opponent.getDifficulty()) || (player.getOpponentType() != opponent.getOpponentType())) { 
                                        opponent = null; // Not a suitable opponent
                                    }
                                    else { // Suitable player and oponent found, flag both players as in game and ready for removal from waitingPLayers queue
                                        queuedPlayer.inGame = true;   // Flag as in-game 
                                        queuedOpponent.inGame = true; // Flag as in-game
                                        
                                        removalList.add(queuedPlayer);   // Record for removal from waitingPlayers queue
                                        removalList.add(queuedOpponent); // Record for removal from waitingPlayers queue
                                    }
                                }
                            }
                        }    
                        
                        if (opponent != null) { // A player and opponent were found in the waitingPlayers queue
                            try {
                                // Message to player
                                player.serverMessage("Opponent found - you are playing " + opponent.getName());

                                // Message to opponent (if human)
                                if (opponent.getIsHuman()) {
                                    ((NimClientInterface) opponent).serverMessage("Opponent found - you are playing " + player.getName());
                                }

                                // Construct game
                                game = new GameOfNim((PlayerInterface) player, (PlayerInterface) opponent, player.getDifficulty(), this);
                                player.setInGameStatus(true);   // Set players status to in game
                                opponent.setInGameStatus(true); // Set the opponents status to in game

                                Thread thread = new Thread(game); // Run game in its own thread
                                thread.start(); // Start (run) the thread

                            } catch (RemoteException e) {
                                System.out.println("Error starting game");
                                System.out.println(e.getStackTrace()[0].toString());
                            }
                        }
                         
                    } catch (Exception e) {
                        
                        System.out.println("Error matching opponents");
                        System.out.println(e.getStackTrace()[0].toString());
                    }
                } // Finished looking for player/opponent matches
                
                // Remove any players from the waitingPlayers queue who have been given a game
                if(!removalList.isEmpty()) {
                    System.out.println("Waiting player: " + waitingPlayers.size() + " in Removal List: " + removalList.size());
                    for(WaitingPlayer p : waitingPlayers) {
                        
                        try {
                            System.out.println(gamers.get(p.key).getName());
                        } catch (RemoteException ex) {
                            Logger.getLogger(NimServerLobby.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    System.out.println("waitingPlayers List size = " + waitingPlayers.size());
                    System.out.println("Clearing Removal List");
                    waitingPlayers.removeAll(removalList);
                    System.out.println("waitingPlayers List size = " + waitingPlayers.size());

                    removalList.clear(); // Clear the removal List
                }
            }
            try { // As the game loops and continually accesses ConcurrentHashmap, pause to stop the thread being greedy
                sleep(1000); // The pause could be mutch shorter, but this stops console output messages runnung away
            } catch(Exception e) {
                System.out.println("Error sleeping:" + e.getMessage());
            }
        }
    }
    
    /**
     * Initialises the server lobby ready to accept client connections
     */
    private void initialiseServerLobby() {
        try {
            System.out.println("Starting: Server initialising...");
            
            // Start RMI registry (saves typing rmiregistry into command line)
            try {
                LocateRegistry.createRegistry(port);
                
                System.out.println("1. RMI registry ready on port " + port);
                System.out.println("----------------------------------");
                        
            } catch(Exception e) {
                System.out.println("Exception starting RMI registry:");
                System.out.println(e.getCause());
            }
            
            Naming.rebind(Constants.SERVERNAME, this); 
            
            ready();
            
        } catch(Exception e) {
            System.err.println("Problem: " + e.getMessage());
        }
    }
    
    /** 
     * Outputs simple message to console
     * @throws RemoteException 
     */
    @Override
    public void ready() throws RemoteException {
        System.out.println("Server awaiting connections...\n");
    }
        
    /**
     * Instantiates lobby
     */
    public static void main(String[] args) {
        try {
            NimServerLobby lobby = new NimServerLobby();
        } catch(RemoteException e) {
            System.err.println("Problem Initialising Game Lobby: " + e.getMessage());
        }
    }
}
    
    
    
    

