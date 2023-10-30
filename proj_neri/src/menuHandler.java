import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;


public class menuHandler implements Runnable {
    Socket socket;
    String host;
    int port;
    th_properties properties;

    public menuHandler(Socket socket, String Host, int Port, th_properties properties) {
        this.socket = socket;
        this.host = Host;
        this.port = Port;
        this.properties = properties;
    }

    public void run () {

        boolean flag;
        User new_user = null;
        User login = null;
        String secret_word = "";

        try { //open the streams 
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            flag = true;
            while (flag) {
                int scelta = in.readInt();
                if ( scelta == 1) { // register
                    String username = in.readUTF(); // receive the registration username
                    
                    if (checkUser(username, properties.users_list)) { // checks if it is valid
                        out.writeInt(-1);
                        System.out.println("----[ USERNAME ALREADY EXISTS ]----]");
                        
                    }
                    else {
                        out.writeInt(0); // username is available
                        String password = in.readUTF();
                        //System.out.println("Username: " + username + " Password: " + password);
                        out.writeInt(0); // saying to the client that evrerything went through fine
                        new_user = new User(username, password); // create a new user
                        properties.users_list.add(new_user); // add the new user to the list
                        update_users_list(properties.users_list); // update the json file
                        continue;
                    }
                }
                else if ( scelta == 2) { // login
                    String username = in.readUTF(); // receive the login username
                    
                    if (checkUser(username, properties.users_list)) { // checks if it is valid
                        
                        login = findUserByUsername(properties.users_list, username); // retrieve chosen user's properties

                        //System.out.println("User " + login.username +" " + login.password);

                        out.writeInt(0); // communicate that username is valid

                        String password = in.readUTF(); // receive the login password

                        if (login.password.equals(password)) { // checks if the password is correct

                            if (login.logged == true) { // checks if the user is already logged
                                out.writeInt(-1); // already logged
                                System.out.println("----[ USER ALREADY LOGGED ]----");
                                continue;   
                            }
                            out.writeInt(0); // password is correct & not logged
                            System.out.println("----[ LOGIN SUCCESSFUL ]----");
                            login.logged = true;
                            
                            boolean logout = false;

                            while (!logout) { // after the succesful login the user can choose what to do
                                
                                int scelta2 = in.readInt();
            
                                if (scelta2 == 1) { // play game
                                
                                    if (login.play_this_word == false) { // checks if the user has already played this word
                                        login.play_this_word = true; 
                                        out.writeInt(0); // the game can start
                                        secret_word = properties.word;
                                        WordleGame(login, properties.word, properties.words_list, in, out); // the game starts
                                    } else {
                                        out.writeInt(-1); // already played this word
                                    }

                                } else if (scelta2 == 2) { // statistics
                                    System.out.println("----[ STATISTICS ]----");
                                    print_statistics(login, out); // print the statistics

                                } else if (scelta2 == 3) { // send message
                                    
                                    if (login.play_this_word == true) {

                                        out.writeInt(0); // the user can share
                                        int attempts = in.readInt(); // receive the number of attempts
                                        mess_sender(port, host, attempts, login.username, secret_word); // send the message

                                    } else {
                                        out.writeInt(-1); // not played this word yet
                                        continue;
                                    }

                                } else if (scelta2 == 4) {

                                    continue; // no need to implement the show me sharing option server side

                                } else if (scelta2 == 5) { // logout
                                    System.out.println("----[ LOGOUT SUCCESSFULL ]----");
                                    login.logged = false;
                                    logout = true;
                                }
                            }
                        } else {
                            out.writeInt(-1); //password is incorrect
                            System.out.println("----[ WRONG PASSWORD   ]----]");
                            continue;
                        }

                    } else {
                        System.out.println("----[ USERNAME DOES NOT EXIST ]----]");
                        out.writeInt(-1); // username does not exist
                        continue;
                    }
                } else if(scelta == 3) { // exit
                    System.out.println("----[ CLIENT CLOSED ]----");   
                    break;
                                        
                }
            }
            in.close();
            out.close(); 
        } catch (Exception e) { // handling unexpected client disconnection
            System.out.println("----[ CLIENT DISCONNECTED ]----");
            login.logged = false;
        }
    }   


    public static synchronized boolean checkUser(String username, ArrayList<User> users_list) { 
        boolean result = false; // function that checks if the username belongs to the users_list
        Iterator<User> iterator = users_list.iterator();

        while (iterator.hasNext()) {
            User user_to_check = iterator.next();
            if (user_to_check.username.equals(username)) {
                result = true;
            }
        } 
        return result;
    }

    public synchronized void update_users_list (ArrayList<User> users_list) throws IOException { // function that updates the json file
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File users_file = new File("users.json");
        FileWriter fw = new FileWriter(users_file);
        String s_json = gson.toJson(users_list);
        fw.write(s_json);
        fw.close();
    }

    public synchronized User findUserByUsername(ArrayList<User> userList, String username) {
        for (User user : userList) {
            if (user.getUsername().equals(username)) {
                return user; // Return the user if the username matches
            }
        }
        return null; // Return null if the username is not found
    }

    // function that handles the game
    public static void WordleGame(User user, String word, ArrayList<String> words_list, DataInputStream in, DataOutputStream out) throws IOException {
        Integer attempts = 1; // need to be initialized to 1
        boolean guessed = false;

        while (guessed == false && attempts <= 12) {

            String guess = in.readUTF(); // receive the guess
            
            if (guess.length() == 10 ) { // checks if the guess is valid
                out.writeInt(0);
                
                if(words_list.contains(guess)) { // checks if the guess is in the dictionary

                    String[] hints = new String[10]; // build the the string of hints that is represented as an array

                    for (Integer i = 0; i < 10; i++) {

                        if (guess.charAt(i) == word.charAt(i)) {
                            hints[i] = "" + word.charAt(i); // letter belongs to the word and is in the right position
                        } else if (word.contains(String.valueOf(guess.charAt(i)))) {
                            hints[i] = "?"; // letter belongs to the word but in the wrong position
                        }
                            else {
                            hints[i] = "_"; // letter does not belong to the word
                        }
                    }

                    String hints_string = String.join("", hints);
                    
                    out.writeInt(0); // everything is fine
                    
                    out.writeUTF(hints_string); // send the hints

                    if (word.equals(guess)) { // checks if the guess is correct
                        System.out.println("----[ YOU WIN ]----");
                        user.games_won++;
                        user.victory_streak++;
                        if (user.victory_streak > user.max_victory_streak) {
                            user.max_victory_streak = user.victory_streak;
                        }
                        System.out.println(attempts);
                        up_guess_distribution(user, attempts);
                        guessed = true;
                    }

                } else {
                    out.writeInt(-1); // guess is not in the dictionary
                    continue;
                }
            } else {
                out.writeInt(-1); // guess is not valid
                continue;
            }
            attempts++;
        }

        if (guessed == false) { // checks if the user lost
            System.out.println("----[ YOU LOSE ]----");
            user.victory_rate = (int) (((float)user.games_won/(float)user.games_played)*100);
            user.victory_streak = 0;
        }

        user.games_played++; // update the user's statistics
        System.out.println("Won" + user.games_won);
        System.out.println(user.games_played);
        user.victory_rate = (int) (((float)user.games_won/(float)user.games_played)*100);
    }

    public static void up_guess_distribution(User user,int attempt){ // function that updates the guess distribution
        int old_value = user.guess_distribution.get(attempt);
        user.guess_distribution.replace(attempt,old_value,old_value+1);
    }

    public static void print_statistics(User user,DataOutputStream out) throws IOException{ // function that sends the statistics to the client
        out.writeInt(user.games_played);
        out.writeInt(user.games_won);
        out.writeInt(user.victory_rate);
        out.writeInt(user.victory_streak);
        out.writeInt(user.max_victory_streak);
        for(int i=1;i<=12;i++){
            out.writeInt(user.guess_distribution.get(i));
        }
    }

    public static void mess_sender(int port, String host, int attempts,String username, String secret_word) throws UnknownHostException { // function that sends the message
        InetAddress ia = InetAddress.getByName(host);
        String mess;
        
        try (DatagramSocket ds = new DatagramSocket(0)) { // open the UDP connection
             
            if (attempts >= 12) { //send the message according to the number of attempts
                mess = username+" did not guess the word: "+secret_word;
            } else {
                mess = username+" guessed the word: "+secret_word+" in "+attempts+" attempts";
            }
            byte[] data = mess.getBytes("US-ASCII");
            //put string in datagram packet and send it 
            DatagramPacket dp = new DatagramPacket(data, data.length, ia, port);
            ds.send(dp);
            //datagram will be closed after the end of the try-catch statement 
        }
        catch(SocketException e){
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
