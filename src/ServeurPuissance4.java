
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServeurPuissance4 {

    private static final int PORT = 12345;
    private static Map<String, Integer> scores = new HashMap<>();
    private static List<ClientHandler> joueursEnAttente = new ArrayList<>();
    private static List<ClientHandler> joueursRejouant = new ArrayList<>();
    private static List<Partie> partiesEnCours = new ArrayList<>();
    private static Lock lock = new ReentrantLock();

    public static void main(String[] args) throws IOException {
        loadScores();  // Charger les scores depuis un fichier

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Serveur Puissance 4 démarré sur le port " + PORT);

        // Boucle principale du serveur
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);  // Vérification toutes les 1 seconde
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            new Thread(clientHandler).start();
        }
    }

    private static void loadScores() {
        try (BufferedReader br = new BufferedReader(new FileReader("scores.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                scores.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (IOException e) {
            System.out.println("Aucun score persistant trouvé.");
        }
    }

    public static synchronized void saveScores() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("scores.txt"))) {
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                bw.write(entry.getKey() + ":" + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void retournerAuLobby(ClientHandler joueur) {
        // Ajouter le joueur à la liste des joueurs en attente
        joueursEnAttente.add(joueur);
        joueur.getWriter().println("Vous êtes de retour au lobby.");
    }

    public static synchronized void ajouterJoueur(ClientHandler joueur) {
        lock.lock();
        try {
            if (!joueursEnAttente.contains(joueur)) {
                joueursEnAttente.add(joueur);
            }

            if (joueursEnAttente.size() >= 2) {
                ClientHandler joueur1 = joueursEnAttente.remove(0);
                ClientHandler joueur2 = joueursEnAttente.remove(0);

                Partie partie = new Partie(joueur1, joueur2);
                joueur1.setPartie(partie);
                joueur2.setPartie(partie);
                partiesEnCours.add(partie);

                joueur1.getWriter().println("Vous jouez contre " + joueur2.getNomJoueur());
                joueur2.getWriter().println("Vous jouez contre " + joueur1.getNomJoueur());
            }
        } finally {
            lock.unlock();
        }
    }

    public static synchronized void retirerJoueur(ClientHandler joueur) {
        lock.lock();
        try {
            joueursEnAttente.remove(joueur);
        } finally {
            lock.unlock();
        }
    }

    public static synchronized void lancerPartie(ClientHandler joueur) {
        lock.lock();
        try {
            if (!joueursEnAttente.contains(joueur)) {
                joueur.getWriter().println("Vous devez d'abord rejoindre le lobby.");
                return;
            }

            // Vérifie si un autre joueur est disponible
            if (joueursEnAttente.size() >= 2) {
                ClientHandler adversaire = null;
                for (ClientHandler candidat : joueursEnAttente) {
                    if (candidat != joueur) {
                        adversaire = candidat;
                        break;
                    }
                }

                if (adversaire != null) {
                    joueursEnAttente.remove(joueur);
                    joueursEnAttente.remove(adversaire);

                    Partie partie = new Partie(joueur, adversaire);
                    joueur.setPartie(partie);
                    adversaire.setPartie(partie);
                    partiesEnCours.add(partie);

                    joueur.getWriter().println("Vous jouez contre " + adversaire.getNomJoueur() + " !");
                    adversaire.getWriter().println("Vous jouez contre " + joueur.getNomJoueur() + " !");
                } else {
                    joueur.getWriter().println("Aucun adversaire disponible pour le moment.");
                }
            } else {
                joueur.getWriter().println("Attendez qu'un autre joueur rejoigne le lobby.");
            }
        } finally {
            lock.unlock();
        }
    }

    public static synchronized void incrementerScore(String nomJoueur) {
        scores.put(nomJoueur, scores.getOrDefault(nomJoueur, 0) + 1);
        saveScores();
    }

    public static synchronized void retirerPartie(Partie partie) {
        lock.lock();
        try {
            partiesEnCours.remove(partie);
        } finally {
            lock.unlock();
        }
    }
}
