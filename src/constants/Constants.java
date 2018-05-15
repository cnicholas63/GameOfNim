/**
 * @author Chris Nicholas
 * Class describes difficulty constants
 * 
 */
package constants;

public class Constants {
    
    public static final int EASY = 1; // Indicates easy difficulty
    public static final int HARD = 2; // Indicates hard difficulty
    
    // The following enable validation against difficulty range
    public static final int DIFFICULTY_MIN = EASY;
    public static final int DIFFICULTY_MAX = HARD; 
    
    // Name of server lobby for URL
    public static final String SERVERNAME = "NimServerLobby";
    
    // Player type values
    public static final int HUMAN_PLAYER = 1;
    public static final int COMPUTER_PLAYER = 2;
    
    
}


