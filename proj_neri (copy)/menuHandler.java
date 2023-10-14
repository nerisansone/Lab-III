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

        try {
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            flag = true;
            while (flag) {
                int scelta = in.readInt();
                System.out.println("Scelta_1: " + scelta);
                if ( scelta == 1) { // register
                    String username = in.readUTF();
                    
                    if (checkUser(username, properties.users_list)) {
                        out.writeInt(-1);
                        System.out.println("----[ USERNAME ALREADY EXISTS ]----]");
                        
                    }
                    else {
                        out.writeInt(0); // username is available
                        String password = in.readUTF();
                        System.out.println("Username: " + username + " Password: " + password);
                        out.writeInt(0); // comunico che username e password sono corretti
                        new_user = new User(username, password);
                        properties.users_list.add(new_user);
                        update_users_list(properties.users_list);
                        continue;
                    }
                }
                else if ( scelta == 2) { // login
                    String username = in.readUTF();
                    
                    if (checkUser(username, properties.users_list)) {
                        
                        User login = findUserByUsername(properties.users_list, username);

                        System.out.println("User " + login.username +" " + login.password);

                        out.writeInt(0); // username exists

                        String password = in.readUTF();

                        if (login.password.equals(password)) {
                            out.writeInt(0); // password is correct
                            System.out.println("----[ LOGIN SUCCESSFUL ]----");
                            
                            boolean logout = false;

                            while (!logout) {
                                
                                
                                int scelta2 = in.readInt();
                                System.out.println("Scelta_2: " + scelta2);
            
                                if (scelta2 == 1) {
                                
                                    if (login.play_this_word == false) {
                                        login.play_this_word = true;
                                        out.writeInt(0); // start game
                                        WordleGame(login, properties.word, properties.words_list, in, out);
                                    } else {
                                        out.writeInt(-1); // already played this word
                                    }

                                } else if (scelta2 == 2) {
                                    System.out.println("----[ STATISTICS ]----");
                                    print_statistics(login, out);

                                } else if (scelta2 == 3) {
                                    
                                    if (login.play_this_word == true) {
                                        out.writeInt(0);

                                        int attempts = in.readInt();

                                        mess_sender(port, host, attempts, login.username);
                                    } else {
                                        out.writeInt(-1); // not played this word yet
                                        continue;
                                    }

                                } else if (scelta2 == 4) {

                                    continue;


                                } else if (scelta2 == 5) {
                                    logout = true;
                                    flag = false;

                                }
                            }
                        } else {
                            out.writeInt(-1); //password is incorrect
                            System.out.println("----[ WRONG PASSWORD ]----]");
                            continue;
                        }

                    } else {
                        System.out.println("----[ USERNAME DOES NOT EXIST ]----]");
                        out.writeInt(-1); // username does not exist
                        continue;
                    }
                } else if(scelta == 3) {
                    System.out.println("----[ CLIENT CLOSED ]----");    
                    break;
                                        
                }
            }
            in.close();
            out.close(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }   


    public static synchronized boolean checkUser(String username, ArrayList<User> users_list) {
        boolean result = false;
        Iterator<User> iterator = users_list.iterator();

        while (iterator.hasNext()) {
            User user_to_check = iterator.next();
            if (user_to_check.username.equals(username)) {
                result = true;
            }
        } 
        return result;
    }

    public synchronized void update_users_list (ArrayList<User> users_list) throws IOException {
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

    public static void WordleGame(User user, String word, ArrayList<String> words_list, DataInputStream in, DataOutputStream out) throws IOException {
        Integer attempts = 1;
        boolean guessed = false;

        while (guessed == false && attempts <= 12) {

            String guess = in.readUTF();
            
            if (guess.length() == 10 ) {
                
                if(words_list.contains(guess)) {

                    String[] hints = new String[10];

                    for (Integer i = 0; i < 10; i++) {

                        if (guess.charAt(i) == word.charAt(i)) {
                            hints[i] = "" + word.charAt(i);
                        } else if (word.contains(String.valueOf(guess.charAt(i)))) {
                            hints[i] = "?";
                        }
                            else {
                            hints[i] = "_";
                        }
                    }

                    String hints_string = String.join("", hints);
                    
                    out.writeInt(0); // everything is fine
                    out.writeUTF(hints_string);

                    if (word.equals(guess)) {
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
                    out.writeInt(-1);
                    continue;
                }
            } else {
                out.writeUTF("----[ LENGTH ERROR ]----");
                continue;
            }
            attempts++; //FINISCI
        }

        if (guessed == false) {
            System.out.println("----[ YOU LOSE ]----");
            user.victory_rate = (int) (((float)user.games_won/(float)user.games_played)*100);
            user.victory_streak = 0;
        }

        System.out.println("Faccio stat");
        user.games_played++;
        System.out.println("Won" + user.games_won);
        System.out.println(user.games_played);
        user.victory_rate = (int) (((float)user.games_won/(float)user.games_played)*100);
    }

    public static void up_guess_distribution(User user,int attempt){
        int old_value = user.guess_distribution.get(attempt);
        user.guess_distribution.replace(attempt,old_value,old_value+1);
    }

    public static void print_statistics(User user,DataOutputStream out) throws IOException{
        out.writeInt(user.games_played);
        out.writeInt(user.games_won);
        out.writeInt(user.victory_rate);
        out.writeInt(user.victory_streak);
        out.writeInt(user.max_victory_streak);
        for(int i=1;i<=12;i++){
            out.writeInt(user.guess_distribution.get(i));
        }
    }

    public static void mess_sender(int port, String host, int n_tentativi,String username) throws UnknownHostException {
        InetAddress ia = InetAddress.getByName(host);
        
        try (DatagramSocket ds = new DatagramSocket(0)) {
            String mess;
            
            if(n_tentativi==0){ // 0 tentativi significa che l'utente non ha indovinato la parola
                mess = username+" didn't guess this word";
            }
            else{
                mess = username+" guessed this word in "+n_tentativi+" attempts";
            }
            byte[] data = mess.getBytes("US-ASCII");
            //metto stringa in un un pacchetto e la invio 
            DatagramPacket dp = new DatagramPacket(data, data.length, ia, port);
            ds.send(dp);
            //non chiudo il datagramsocket tanto ci pensa il try 
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
