import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Classe rappresentante il WordleServer
class WordleServer {
    private List<User> registeredUsers;
    private List<String> wordList;
    private String secretWord;
    private int maxAttempts;
    private int currentAttempt;

    public WordleServer(List<String> wordList, int maxAttempts) {
        registeredUsers = new ArrayList<>();
        this.wordList = wordList;
        this.maxAttempts = maxAttempts;
        currentAttempt = 1;
    }

    // Registra un nuovo utente
    public boolean register(String username, String password) {
        if (isUsernameAvailable(username) && !password.isEmpty()) {
            User newUser = new User(username, password);
            registeredUsers.add(newUser);
            return true;
        }
        return false;
    }

    // Controlla se lo username è disponibile
    private boolean isUsernameAvailable(String username) {
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username)) {
                return false;
            }
        }
        return true;
    }

    // Effettua il login dell'utente
    public boolean login(String username, String password) {
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                user.setLoggedIn(true);
                return true;
            }
        }
        return false;
    }

    // Effettua il logout dell'utente
    public void logout(String username) {
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username)) {
                user.setLoggedIn(false);
                return;
            }
        }
    }

    // Avvia il gioco Wordle per l'utente
    public boolean playWORDLE(String username) {
        if (currentAttempt > maxAttempts) {
            return false; // Gioco terminato
        }
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username) && !user.hasPlayed(secretWord)) {
                user.clearAttempts();
                user.setPlaying(true);
                return true;
            }
        }
        return false; // Utente non trovato o ha già giocato
    }

    // Invia una parola proposta dall'utente e restituisce gli indizi
    public String sendWord(String username, String word) {
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username) && user.isPlaying()) {
                user.addAttempt(word);
                if (word.equals(secretWord)) {
                    user.setPlaying(false);
                    return "Parola corretta!"; // Indovinato
                } else {
                    return generateHints(word);
                }
            }
        }
        return ""; // Utente non trovato o non sta giocando
    }

    // Genera gli indizi per la parola proposta
    private String generateHints(String word) {
        // Implementazione personalizzata per generare gli indizi
        return "Indizi per la parola";
    }

    // Ottiene le statistiche aggiornate dell'utente
    public String sendMeStatistics(String username) {
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username)) {
                return user.getStatistics();
            }
        }
        return ""; // Utente non trovato
    }

    // Condivide i risultati del gioco su un gruppo multicast
    public void share(String username) {
        // Implementazione per condividere i risultati su un gruppo multicast
        System.out.println("Risultati condivisi su un gruppo sociale");
    }

    // Mostra le notifiche degli altri utenti
    public void showMeSharing(String username) {
        // Implementazione per mostrare le notifiche degli altri utenti
        System.out.println("Notifiche degli altri utenti");
    }

    // Avvia una nuova partita con una parola segreta casuale
    public void startNewGame() {
        Random random = new Random();
        secretWord = wordList.get(random.nextInt(wordList.size()));
        currentAttempt = 1;
    }

    // Incrementa il numero di tentativi corrente
    public void incrementAttempt() {
        currentAttempt++;
    }
}

// Classe rappresentante l'utente
class User {
    private String username;
    private String password;
    private boolean loggedIn;
    private boolean playing;
    private List<String> attempts;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        loggedIn = false;
        playing = false;
        attempts = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void addAttempt(String word) {
        attempts.add(word);
    }

    public void clearAttempts() {
        attempts.clear();
    }

    public boolean hasPlayed(String secretWord) {
        return attempts.contains(secretWord);
    }

    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Statistiche per l'utente: ").append(username).append("\n");
        sb.append("Partite giocate: ").append(attempts.size()).append("\n");
        sb.append("Tentativi effettuati: ").append(attempts.toString()).append("\n");
        return sb.toString();
    }
}

// Classe rappresentante il WordleClient
class WordleClient {
    private WordleServer server;
    private String username;

    public WordleClient(WordleServer server) {
        this.server = server;
    }

    // Registra un nuovo utente
    public void register(String username, String password) {
        if (server.register(username, password)) {
            System.out.println("Registrazione avvenuta con successo.");
        } else {
            System.out.println("Registrazione fallita. Verifica lo username e la password.");
        }
    }

    // Effettua il login dell'utente
    public void login(String username, String password) {
        if (server.login(username, password)) {
            this.username = username;
            System.out.println("Login avvenuto con successo.");
        } else {
            System.out.println("Login fallito. Verifica lo username e la password.");
        }
    }

    // Effettua il logout dell'utente
    public void logout() {
        server.logout(username);
        System.out.println("Logout effettuato con successo.");
    }

    // Avvia il gioco Wordle
    public void playWORDLE() {
        if (server.playWORDLE(username)) {
            System.out.println("Il gioco è iniziato. Indovina la parola!");
        } else {
            System.out.println("Non puoi iniziare un nuovo gioco. Verifica il numero di tentativi rimasti o se hai già giocato per la parola corrente.");
        }
    }

    // Invia una parola proposta
    public void sendWord(String word) {
        String result = server.sendWord(username, word);
        if (result.equals("Parola corretta!")) {
            System.out.println("Complimenti, hai indovinato la parola!");
        } else {
            System.out.println(result);
        }
    }

    // Ottiene le statistiche dell'utente
    public void sendMeStatistics() {
        String statistics = server.sendMeStatistics(username);
        if (!statistics.isEmpty()) {
            System.out.println(statistics);
        } else {
            System.out.println("Nessuna statistica disponibile per l'utente.");
        }
    }

    // Condivide i risultati del gioco su un gruppo multicast
    public void share() {
        server.share(username);
        System.out.println("Risultati condivisi con successo.");
    }

    // Mostra le notifiche degli altri utenti
    public void showMeSharing() {
        server.showMeSharing(username);
    }
}

public class WordleGame {
    public static void main(String[] args) {
        // Creazione del server e del client
        List<String> wordList = new ArrayList<>();
        wordList.add("parola1");
        wordList.add("parola2");
        wordList.add("parola3");

        WordleServer server = new WordleServer(wordList, 12);
        WordleClient client = new WordleClient(server);

        // Esempi di utilizzo del client
        client.register("username", "password");
        client.login("username", "password");
        client.playWORDLE();
        client.sendWord("proposta");
        client.sendMeStatistics();
        client.share();
        client.showMeSharing();
        client.logout();
    }
}
