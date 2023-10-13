import java.net.*;
import java.util.Scanner;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WordleClient {
    public static int port;
    public static String host;
    public static String group;
    public static int port_2;
    public static final String config = "client_config.properties";
    public static void main(String[] args) throws IOException {

        readConfig();
        
        //InetAddress addr = InetAddress.getByName(null);
        //System.out.println("addr = " + addr);
        //Socket socket = new Socket(addr, WordleServer.PORT);

        Socket socket = new Socket(host, port);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        Scanner scan = new Scanner(System.in);

        boolean flag = true;

        Integer choice_menu_1 = 0;
        Integer choice_menu_2 = 0;

        try {
            System.out.println("Client connected");
            System.out.println("Socket" + socket);

            
            while (flag) {
                System.out.println("------------------------------------");
                System.out.println("\nREGISTER [1] \nLOGIN [2]\nEXIT [3]\n");
                try {
                    
                    choice_menu_1 = Integer.parseInt(scan.nextLine());
                
                } catch (NumberFormatException e) {
                
                    System.out.println("Input is not a number");
                
                    continue;
                }

                out.writeInt(choice_menu_1);

                if (choice_menu_1 == 1) {
                    
                    System.out.println("Enter username: ");
                    String username = scan.nextLine();
                    
                    out.writeUTF(username);
                    
                    System.out.println("Enter password: ");
                    String password = scan.nextLine();
                    
                    out.writeUTF(password);          
                    
                    int user_exists = in.readInt();

                    if (user_exists == -1) {
                        System.out.println("Username already exists");
                        continue;
                    } else if (user_exists == 0 ) {
                        System.out.println("Registration successful");
                        continue;
                    }
                }
                else if (choice_menu_1 == 2) {
                    
                    System.out.println("Enter username: ");
                    String username = scan.nextLine();

                    out.writeUTF(username);
                    
                    int user_exists = in.readInt();

                    int attempt = 0;

                    if (user_exists == 0) {
                        
                        System.out.println("Enter password: ");
                        String password = scan.nextLine();

                        out.writeUTF(password);

                        int password_correct = in.readInt();

                        if (password_correct == -1) {
                            System.out.println("Password is incorrect");
                            continue;
                        } else {
                            
                            System.out.println("\nLogin successful");
                            Mess_Receiver mess_receiver = new Mess_Receiver(port_2, group);
                            Thread t = new Thread(mess_receiver);
                            t.start();
                            boolean logout = false;

                            while (!logout) {
                                System.out.println("------------------------------------");
                                System.out.println("\nPLAY [1] \nSTATISTICS [2] \nSHARE [3] \nSHOW ME SHARING [4]\nLOGOUT [5]\n");

                                try {
                                    choice_menu_2 = Integer.parseInt(scan.nextLine());
                                    if (choice_menu_2 == 1) {
                                        //Integer player_can_play = in.readInt();
                                        out.writeInt(1);
                                        Integer already_played = in.readInt();

                                        if (already_played == 0) {
                                            System.out.println("----[ GAME STARTING ]----");
                                            attempt = WordleGame(scan, in, out);
                                        } else if (already_played == -1) {
                                            System.out.println("----[ YOU HAVE ALREADY PLAYED THIS WORD ]----");
                                        } else {
                                            System.out.println("----[ ERROR PLAYING GAME ]----");
                                        }

                                    } else if (choice_menu_2 == 2) {
                                        out.writeInt(2);
                                        print_statistics(in);

                                    } else if (choice_menu_2 == 3) {

                                        int player_can_share = in.readInt();

                                        if (player_can_share == 0) {
                                            out.writeInt(attempt);
                                            System.out.println("----[ MESSAGE SHARED ]----");
                                        } 
                                        else {
                                            System.out.println("----[ YOU HAVE NOT PLAYED THE WORD YET  ]----");
                                        }
                                        continue;

                                    } else if (choice_menu_2 == 4) {

                                        mess_receiver.print_mess();
                                        continue;

                                    } else if (choice_menu_2 == 5) {
                                        System.out.println("Logging out...");
                                        out.writeInt(5);
                                        mess_receiver.close_connection();
                                        logout = true;
                                        flag = false;
                                    } else {
                                        System.out.println("[ [2]MENU CHOICE DOES NOT EXIST ]");
                                        continue;
                                    }
                                } catch (Error e) {
                                    System.out.println("[ERROR READING INPUT]");
                                }
                            }
                        }
                    } else if (user_exists == -1) {
                        System.out.println("[USER DOES NOT EXIST]");
                        continue;
                    }

                }
                else if (choice_menu_1 == 3) {
                    System.out.println("[CLOSING GAME]");
                    flag = false;
                }
                else {
                    System.out.println("[ [1]MENU CHOICE DOES NOT EXIST ]");
                    continue;
                }
            }
        }   
        finally {
            System.out.println("[GAME CLOSED]");
            scan.close();
            socket.close();
        }
    }

    public static int WordleGame (Scanner scanner, DataInputStream in, DataOutputStream out) throws IOException {

        boolean guessed = false;
        Integer attempts = 0;

        while (!guessed && attempts < 12) {
            
            System.out.println("----[ ENTER GUESS: ]----");

            if (attempts == 11 ) {
                System.out.println("----[ ONE LAST ATTEMPT REMAINING ]----");
            }

            String guess = scanner.nextLine();

            if (guess.length() != 10 ) {
                System.out.println("----[ GUESS MUST BE 10 CHARACTERS LONG ]----");
                continue;
            } else {
                out.writeUTF(guess);
            }

            Integer i = in.readInt();
            if (i == 0) {
              String hints = in.readUTF();

                if (guess.equals(hints)) {
                    System.out.println("----[ YOU WIN ]----");
                    guessed = true;
                } else {
                    System.out.println(hints);
                }  
            } else if (i == -1) {
                System.out.println("----[ WORD DOES NOT EXIST ]----");
                continue;
            } else {
                System.out.println("----[ ERROR ]----");
                continue;
            }

            attempts++;
        }



        return attempts;
    }

    public static void print_statistics(DataInputStream in) throws IOException {
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

    public static void readConfig() throws FileNotFoundException, IOException {
		InputStream input = new FileInputStream(config);
        Properties prop = new Properties();
        prop.load(input);
        port = Integer.parseInt(prop.getProperty("port"));
        //timer_word = Integer.parseInt(prop.getProperty("timer"));
        host = prop.getProperty("host");
        port_2 = Integer.parseInt(prop.getProperty("port_2"));
        group = prop.getProperty("group");
        input.close();
	}

}