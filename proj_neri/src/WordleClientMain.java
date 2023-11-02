import java.net.*;
import java.util.Scanner;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class WordleClientMain {
    public static int port;
    public static String host;
    public static String group;
    public static int port_2;
    public static final String config = "client_config.properties";
    public static void main(String[] args) throws IOException {

        readConfig(); // read config file

        Socket socket = null; // initialize socket

        try {
            socket = new Socket(host, port); // create socket
        } catch (SocketException e) {
            System.out.println("----[ SERVER NOT REACHABLE ]----");
            System.exit(0);
        }
        DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // create output stream
        DataInputStream in = new DataInputStream(socket.getInputStream()); // create input stream 
        Scanner scan = new Scanner(System.in); // create scanner for the CLI
 
        boolean flag = true;

        Integer choice_menu_1 = 0;
        Integer choice_menu_2 = 0;

        try {
            System.out.println("----[ WELCOME TO WORDLE ]----");
            //System.out.println("Socket" + socket);

            try {
                while (flag) {
                System.out.println("------------------------------------");
                System.out.println("\nREGISTER [1] \nLOGIN [2]\nEXIT [3]\n");
                try {
                    
                    choice_menu_1 = Integer.parseInt(scan.nextLine()); // read choice first menu
                
                } catch (NumberFormatException e) { // catch exception if input is not a number
                
                    System.out.println("----[ ERROR INPUT IS NOT A NUMBER ]----");
                
                    continue;
                }

                out.writeInt(choice_menu_1); // send first choice to server

                if (choice_menu_1 == 1) { // register
                    
                    System.out.println("----[ ENTER USERNAME: ]----");
                    String username = scan.nextLine(); // read username

                    while (username.trim().isEmpty()) { // check if username is empty
                        System.out.println("----[ USERNAME CAN NOT BE EMPTY ]----");
                        System.out.println("---[ INSERT A DIFFERENT USERNAME: ]---");
                        username = scan.nextLine();
                    }
                    
                    out.writeUTF(username); // send username to server
                    if (in.readInt() == -1) { // check if username already exists
                        System.out.println("----[ USERNAME ALREADY EXISTS ]----");
                        continue;
                    }

                    System.out.println("----[ ENTER PASSWORD: ]----");
                    String password = scan.nextLine(); // read password

                    while (password.trim().isEmpty()) { // check if password is empty
                        System.out.println("----[ PASSWORD CAN NOT BE EMPTY ]----");
                        System.out.println("----[ INSERT A DIFFERENT PASSWORD: ]---");

                        password = scan.nextLine();
                    }
                    
                    out.writeUTF(password);          
                    
                    int user_exists = in.readInt();

                    /* if (user_exists == -1) { // check if username already exists
                        System.out.println("----[ USERNAME ALREADY EXISTS ]----");
                        continue;
                    } else */ if (user_exists == 0 ) {
                        System.out.println("----[ REGISTRATION SUCCESSFUL ]----");
                        continue;
                    }
                }
                else if (choice_menu_1 == 2) { // login
                    
                    System.out.println("----[ ENTER USERNAME ]----");
                    String username = scan.nextLine(); // read username

                    out.writeUTF(username); // send username to server
                    
                    int user_exists = in.readInt(); // check if username exists

                    int attempt = 1; // number of attempts initialized otherwise it gives error

                    if (user_exists == 0) { // if username exists
                        
                        System.out.println("----[ ENTER PASSWORD ]----");
                        String password = scan.nextLine(); // read password

                        out.writeUTF(password); // send password to server

                        int password_correct = in.readInt(); // check if password is correct

                        if (password_correct == -1) { // if password is not correct
                            System.out.println("----[ WRONG PASSWORD OR USER ALREADY LOGGED ]----");
                            continue;
                        } else { // if password IS correct
                            
                            System.out.println("----[ LOGIN SUCCESSFUL ]----");
                            Mess_Receiver mess_receiver = new Mess_Receiver(port_2, group); // create new thread for multicast
                            Thread t = new Thread(mess_receiver);
                            t.start();
                            boolean logout = false;

                            while (!logout) { // while user is logged in
                                System.out.println("------------------------------------");
                                System.out.println("\nPLAY [1] \nSTATISTICS [2] \nSHARE [3] \nSHOW ME SHARING [4]\nLOGOUT [5]\n");

                                try {
                                    choice_menu_2 = Integer.parseInt(scan.nextLine());
                                    if (choice_menu_2 == 1) { // play
                                        //Integer player_can_play = in.readInt();
                                        out.writeInt(1); // send choice to server
                                        Integer already_played = in.readInt(); // check if word has already been played

                                        if (already_played == 0) { // if word has not been played
                                            System.out.println("----[ GAME STARTING ]----");

                                            try{ // try to play game
                                                attempt = WordleGame(scan, in, out);
                                            } catch (Exception e) {
                                                System.out.println("----[ ERROR PLAYING GAME]----");
                                                flag = false;
                                                logout = true;
                                            }
                                        } else if (already_played == -1) { // if word has already been played
                                            System.out.println("----[ YOU HAVE ALREADY PLAYED THIS WORD ]----");
                                        } else {
                                            System.out.println("----[ ERROR PLAYING GAME ]----");
                                        }

                                    } else if (choice_menu_2 == 2) { // statistics 
                                        out.writeInt(2);
                                        print_statistics(in);

                                    } else if (choice_menu_2 == 3) { // share
                                        out.writeInt(3);
                                        int player_can_share = in.readInt();

                                        if (player_can_share == 0) {
                                            out.writeInt(attempt); 
                                            System.out.println("----[ MESSAGE SHARED ]----");
                                        } 
                                        else {
                                            System.out.println("----[ YOU HAVE NOT PLAYED THE WORD YET  ]----");
                                        }
                                        continue;

                                    } else if (choice_menu_2 == 4) { // show me sharing

                                        mess_receiver.print_mess();
                                        continue;

                                    } else if (choice_menu_2 == 5) { // logout
                                        System.out.println("----[ LOGGING OUT ]----");
                                        out.writeInt(5);
                                        mess_receiver.close_connection();
                                        logout = true;
                                    } else {
                                        System.out.println("----[ MENU CHOICE DOES NOT EXIST ]----");
                                        continue;
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("----[ ERROR READING INPUT ]----");
                                }
                            }
                        }
                    } else if (user_exists == -1) {
                        System.out.println("----[ USER DOES NOT EXIST ]----");
                        continue;
                    }

                }
                else if (choice_menu_1 == 3) { // exit
                    System.out.println("----[ CLOSING GAME ]----");
                    flag = false;
                }
                else { // if choice is not 1, 2 or 3
                    System.out.println("----[ MENU CHOICE DOES NOT EXIST ]----");
                    continue;
                }
            }
            } catch (SocketException e) { // catch exception if server is not reachable
                System.out.println("----[ SERVER NOT REACHABLE ]----");
            }
        }   
        finally { // close socket and scanner after the try block
            System.out.println("----[ GAME CLOSED ]----");
            scan.close();
            socket.close();
            System.exit(0);
        }
    }

    public static int WordleGame (Scanner scanner, DataInputStream in, DataOutputStream out) throws IOException {

        boolean guessed = false;
        Integer attempts = 0;

        while (!guessed && attempts < 12) { // while word has not been guessed and attempts are less than 12
            
            System.out.println("----[ ENTER GUESS: ]----");

            if (attempts == 11 ) { // if last attempt
                System.out.println("----[ ONE LAST ATTEMPT REMAINING ]----");
            }

            String guess = scanner.nextLine(); // read guess
            out.writeUTF(guess); // send guess to server
            //Integer word_is_correct = 0; // check if word is correct
            
            Integer word_is_correct = in.readInt(); // read if word is correct
            if (word_is_correct == -1) { // if word is not correct
                System.out.println("----[ GUESS MUST BE 10 CHARACTERS LONG ]----");
                continue;
            }

            Integer i = in.readInt(); // read if the guess is valid
            if (i == 0) {
              String hints = in.readUTF(); // receive the "hinted" word

                if (guess.equals(hints)) { // if the guess is equal to the word
                    System.out.println("----[ YOU WIN ]----");
                    guessed = true;
                } else {
                    System.out.println(hints); // print the "hinted" word
                }  
            } else if (i == -1) { // if the guess is not valid
                System.out.println("----[ WORD DOES NOT EXIST ]----");
                continue;
            } else { 
                System.out.println("----[ ERROR ]----");
                continue;
            }

            attempts++;
        }

        if (attempts >= 12) {
            System.out.println("----[ YOU LOSE ]----");
        }
        return attempts;
    }

    public static void print_statistics(DataInputStream in) throws IOException { // print statistics
        int games_played = in.readInt();
        System.out.println("Games played : "+games_played);
        int games_won = in.readInt();
        System.out.println("Games won : "+games_won);
        int victory_rate = in.readInt();
        System.out.println("Victory rate : "+victory_rate);
        int victory_streak = in.readInt();
        System.out.println("Victory streak : "+victory_streak);
        int max_victory_streak = in.readInt();
        System.out.println("Record victory streak : "+max_victory_streak);
        System.out.println("Guess Distribution :");

        for (int i = 1; i < 13; i++) {
            int guess_distribution = in.readInt();
            System.out.println("Number of words guessed with "+i+" attempts : "+guess_distribution);
        }
    }

    public static void readConfig() throws FileNotFoundException, IOException { // read config file
		InputStream input = new FileInputStream(config);
        Properties prop = new Properties();
        prop.load(input);
        port = Integer.parseInt(prop.getProperty("port"));
        host = prop.getProperty("host");
        port_2 = Integer.parseInt(prop.getProperty("port_2"));
        group = prop.getProperty("group");
        input.close();
	}

}