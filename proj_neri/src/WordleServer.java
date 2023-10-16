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

public class WordleServer {
    public static int port;
    public static int port_2;
    public static String host;
    public static int timer_word = 120000;
    public static final String config = "server_config.properties";
    public static void main(String[] args) throws IOException {
        readConfig();
        
        ServerSocket s = new ServerSocket(port);
        

        File users = new File("users.json");
        ArrayList<User> users_list = new ArrayList<User>();
        if (users.exists()){
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(users));
            Type list_user_type = new TypeToken<ArrayList<User>>() {}.getType();
            users_list = gson.fromJson(reader,list_user_type);
        }
        else{ //altrmineti creo file json vuoto
            users.createNewFile();
        }  
        //RICONTROLLARE     
        File file = new File("words.txt");
        ArrayList<String> words_list = create_dictionary(file);
        //MANCA ROBA
        th_properties properties = new th_properties(null, users_list, words_list);
        Timer timer = new Timer();
        long delay = 0;
        long period = timer_word;

        timer.scheduleAtFixedRate(new Change_Word(properties), delay, period);
        ExecutorService executor = Executors.newCachedThreadPool();

        boolean flag = true;

        try {

            while(flag) {
                Socket socket = s.accept();
                System.out.println("Client connected");
                try {
                    executor.execute(new menuHandler(socket, host, port_2, properties));
                } catch (Exception e) {
                    System.out.println("----[ ERROR CREATING THREAD ]----");
                }
            }
            
            
        } 
        catch(IOException e){
            System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
        }
        s.close();
    }

    public static ArrayList<String> create_dictionary(File f) throws FileNotFoundException{
        Scanner s = new Scanner(f);
        ArrayList<String> dictionary = new ArrayList<String>();
        while (s.hasNext()){
            dictionary.add(s.next());
        }
        s.close();
        return dictionary;
    }

    public static void readConfig() throws FileNotFoundException, IOException {
		InputStream input = new FileInputStream(config);
        Properties prop = new Properties();
        prop.load(input);
        port = Integer.parseInt(prop.getProperty("port"));
        //timer_word = Integer.parseInt(prop.getProperty("timer"));
        host = prop.getProperty("host");
        port_2 = Integer.parseInt(prop.getProperty("port_2"));
        input.close();
	}

}