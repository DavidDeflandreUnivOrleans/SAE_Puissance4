
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ServeurPuissance4 {

    private static final int PORT = 12345;
    private static Map<String, Integer> scores = new HashMap<>();
    private static List<ClientHandler> joueursEnAttente = new ArrayList<>();
    private static List<ClientHandler> joueursRejouant = new ArrayList<>();
    private static List<Partie> partiesEnCours = new ArrayList<>();
    private static Lock lock = new ReentrantLock();

    // Variables pour l'interface graphique
    private static JFrame frame;
    private static DefaultListModel<String> waitingListModel;
    private static DefaultTableModel gamesTableModel;

    public static void main(String[] args) throws IOException {
        loadScores();  // Charger les scores depuis un fichier

        // Démarrer l'interface graphique
        createGUI();

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Serveur Puissance 4 démarré sur le port " + PORT);

        // Boucle principale du serveur
        new Thread(() -> {
            while (true) {
                updateInterface();  // Met à jour l'interface régulièrement pour détecter les déconnexions
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

    public static synchronized void joueurVeutRejouer(ClientHandler joueur) {
        joueursRejouant.add(joueur);
        if (joueursRejouant.size() == 2) { // Deux joueurs prêts à rejouer
            Partie nouvellePartie = new Partie(joueursRejouant.get(0), joueursRejouant.get(1));
            joueursRejouant.get(0).setPartie(nouvellePartie);
            joueursRejouant.get(1).setPartie(nouvellePartie);
            joueursRejouant.clear(); // Réinitialiser la liste pour la prochaine partie
        }
    }

    private static void createGUI() {
        frame = new JFrame("Serveur Puissance 4");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        // Liste des joueurs en attente
        waitingListModel = new DefaultListModel<>();
        JList<String> waitingList = new JList<>(waitingListModel);
        JScrollPane waitingScrollPane = new JScrollPane(waitingList);
        waitingScrollPane.setBorder(BorderFactory.createTitledBorder("Joueurs en attente"));
        frame.add(waitingScrollPane);

        // Tableau des parties en cours
        String[] columnNames = {"Joueur 1", "Joueur 2"};
        gamesTableModel = new DefaultTableModel(columnNames, 0);
        JTable gamesTable = new JTable(gamesTableModel);
        JScrollPane gamesScrollPane = new JScrollPane(gamesTable);
        gamesScrollPane.setBorder(BorderFactory.createTitledBorder("Parties en cours"));
        frame.add(gamesScrollPane);

        // Afficher la fenêtre
        frame.setVisible(true);
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

    // Méthode pour renvoyer un joueur au lobby
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
                waitingListModel.addElement(joueur.getNomJoueur());
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

                waitingListModel.removeElement(joueur1.getNomJoueur());
                waitingListModel.removeElement(joueur2.getNomJoueur());
                gamesTableModel.addRow(new Object[]{joueur1.getNomJoueur(), joueur2.getNomJoueur()});
            }
        } finally {
            lock.unlock();
        }
    }

    public static synchronized void retirerJoueur(ClientHandler joueur) {
        lock.lock();
        try {
            if (joueursEnAttente.remove(joueur)) {
                waitingListModel.removeElement(joueur.getNomJoueur());
            }
        } finally {
            lock.unlock();
        }
    }

    // Nouvelle méthode pour lancer une partie
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

                    // Mise à jour de l'interface graphique
                    waitingListModel.removeElement(joueur.getNomJoueur());
                    waitingListModel.removeElement(adversaire.getNomJoueur());
                    gamesTableModel.addRow(new Object[]{joueur.getNomJoueur(), adversaire.getNomJoueur()});

                    // Informer les joueurs
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

    // Met à jour l'interface pour vérifier les déconnexions
    private static void updateInterface() {
        lock.lock();
        try {
            // Vérifier les déconnexions des joueurs en attente
            List<ClientHandler> joueursAEnlever = new ArrayList<>();
            for (ClientHandler joueur : joueursEnAttente) {
                if (!joueur.isConnected()) {
                    joueursAEnlever.add(joueur);
                }
            }
            for (ClientHandler joueur : joueursAEnlever) {
                joueursEnAttente.remove(joueur);
                waitingListModel.removeElement(joueur.getNomJoueur());
            }

            // Vérifier les déconnexions dans les parties en cours
            List<Partie> partiesAEnlever = new ArrayList<>();
            for (Partie partie : partiesEnCours) {
                if (!partie.getJoueur1().isConnected() || !partie.getJoueur2().isConnected()) {
                    partiesAEnlever.add(partie);
                }
            }
            for (Partie partie : partiesAEnlever) {
                retirerPartie(partie);
            }
        } finally {
            lock.unlock();
        }
    }

    // Méthode pour retirer une partie de la liste des parties en cours
    public static synchronized void retirerPartie(Partie partie) {
        lock.lock();
        try {
            partiesEnCours.remove(partie);

            // Rechercher la ligne correspondante dans le tableau et la supprimer
            for (int i = 0; i < gamesTableModel.getRowCount(); i++) {
                if (gamesTableModel.getValueAt(i, 0).equals(partie.getJoueur1().getNomJoueur())
                        && gamesTableModel.getValueAt(i, 1).equals(partie.getJoueur2().getNomJoueur())) {
                    gamesTableModel.removeRow(i);
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
