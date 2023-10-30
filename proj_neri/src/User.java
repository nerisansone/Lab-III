import java.util.HashMap;

//Class that represents a user with all his relevant attributes
public class User {
    String username;
    String password;
    boolean play_this_word;
    int games_played;
    int games_won;
    int victory_rate;
    int victory_streak;
    int max_victory_streak;
    boolean logged;
    HashMap<Integer,Integer> guess_distribution;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.play_this_word = false;
        this.games_played = 0;
        this.games_won = 0;
        this.victory_rate = 0;
        this.victory_streak = 0;
        this.max_victory_streak = 0;
        this.logged = false;
        HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
        map.put(1,0);
        map.put(2,0);
        map.put(3,0);
        map.put(4,0);
        map.put(5,0);
        map.put(6,0);
        map.put(7,0);
        map.put(8,0);
        map.put(9,0);
        map.put(10,0);
        map.put(11,0);
        map.put(12,0);
        this.guess_distribution = map; 

    }

    public String getUsername() {
        return this.username;
    }
}
