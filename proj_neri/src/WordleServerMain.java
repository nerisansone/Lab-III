import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.reflect.*;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class WordleServerMain {
    public static int port;
    public static int port_2;
    public static String host;
    public static int timer_word = 120000;
    public static final String config = "server_config.properties";
    public static void main(String[] args) throws IOException {
        readConfig(); // read config file
        
        ServerSocket s = new ServerSocket(port); // create server socket
        

        File users = new File("users.json"); // initiate file for users
        ArrayList<User> users_list = new ArrayList<User>(); // create list of users
        if (users.exists()){  // if users file exists, read it
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(users));
            Type list_user_type = new TypeToken<ArrayList<User>>() {}.getType();
            users_list = gson.fromJson(reader,list_user_type);
        }
        else{ //otherwise create it
            users.createNewFile();
        }  

        File file = new File("words.txt"); // initiate file for words
        ArrayList<String> words_list = create_dictionary(file); // create list of words
        th_properties properties = new th_properties(null, users_list, words_list); // create properties object that will be given to every thread
        Timer timer = new Timer(); 
        long period = timer_word;

        timer.scheduleAtFixedRate(new Change_Word(properties), 0, period);  // schedule the timer that will change the word every 2 minutes
        ExecutorService executor = Executors.newCachedThreadPool(); // create thread pool

        boolean flag = true;

        try {

            while(flag) { // accept connections and create threads
                Socket socket = s.accept();
                System.out.println("----[ NEW CONNECTION ]----");
                try {
                    executor.execute(new menuHandler(socket, host, port_2, properties));
                } catch (Exception e) {
                    System.out.println("----[ ERROR CREATING THREAD ]----");
                }
            }
            
            
        } 
        catch(IOException e){
            System.out.println("----[ ERROR ACCEPTING CONNECTION ]----");
        }
        s.close();
    }

    public static ArrayList<String> create_dictionary(File f) throws FileNotFoundException{ // create dictionary from file
        Scanner s = new Scanner(f);
        ArrayList<String> dictionary = new ArrayList<String>();
        while (s.hasNext()){
            dictionary.add(s.next());
        }
        s.close();
        return dictionary;
    }

    public static void readConfig() throws FileNotFoundException, IOException { // read config file
		InputStream input = new FileInputStream(config);
        Properties prop = new Properties();
        prop.load(input);
        port = Integer.parseInt(prop.getProperty("port"));
        host = prop.getProperty("host");
        port_2 = Integer.parseInt(prop.getProperty("port_2"));
        input.close();
	}

}