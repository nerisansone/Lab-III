import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TimerTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Change_Word extends TimerTask{

    th_properties info;
    public Change_Word(th_properties info){
        this.info = info;
    }

    public void run() {
        //changes secret word and sets play_this_word to false for all users
        info.word = extract_word(info.words_list);

        now_can_play_again(info.users_list);
        try {
            //update the lists with the new datas
            backup_server_users(info.users_list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("New secret word : " + info.word);
    }

    //randomly choses a word from the dictionary
    //casting to ArrayList in order make easier the random choice
    public String extract_word(ArrayList<String> dictionary){
        String[] diz = dictionary.toArray(new String[0]);
        Random random = new Random();
        int i = random.nextInt(diz.length);
        return diz[i];
    }

    //sets play_this_word to false for all users
    public void now_can_play_again(ArrayList<User> users_list){
        Iterator<User> iter = users_list.iterator();
        while(iter.hasNext()){
            User user = iter.next();
            user.play_this_word = false;
        }
    }

    //updates the users.json file 
    public synchronized void backup_server_users(ArrayList<User> users_list) throws IOException{
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File users_file = new File("users.json");
        FileWriter fw = new FileWriter(users_file);
        String s_json = gson.toJson(users_list);
        fw.write(s_json);
        fw.close();
    }    
}
