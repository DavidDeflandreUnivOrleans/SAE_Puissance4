
import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nomJoueur;
    private Partie partie;
    private boolean isConnected = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Connexion du joueur
            nomJoueur = in.readLine();
            System.out.println("Nom reçu du joueur : " + nomJoueur);

            // Ajouter le joueur à la liste des joueurs en attente
            ServeurPuissance4.ajouterJoueur(this);
            out.println("Bienvenue " + nomJoueur + ", en attente d'un adversaire...");

            // Attendre un adversaire
            while (partie == null && isConnected) {
                Thread.sleep(1000);
            }

            // Démarrage du jeu
            if (isConnected) {
                startGame();
            }

        } catch (IOException | InterruptedException e) {
            System.out.println(nomJoueur + " s'est déconnecté.");
            if (partie != null) {
                partie.joueurDeconnecte(this);  // Gérer la déconnexion et déclarer un gagnant
            }
        } finally {
            closeConnection();
        }
    }

    public void setPartie(Partie partie) {
        this.partie = partie;
    }

    public String getNomJoueur() {
        return nomJoueur;
    }

    public PrintWriter getWriter() {
        return out;
    }

    // Nouvelle méthode pour obtenir la socket
    public Socket getSocket() {
        return socket;
    }

    // Méthode pour obtenir la partie associée à ce joueur
    public Partie getPartie() {
        return partie;
    }

    // Méthode pour obtenir le nom du joueur
    public String getJoueur() {
        return nomJoueur;
    }

    private void startGame() throws IOException {
        String message;
        boolean partieTerminee = false;
        try {
            while (!partieTerminee && isConnected) {
                // Lire la colonne choisie par le joueur
                out.println("Choisissez une colonne (1 à 7) :");
                message = in.readLine();

                if (message == null) {
                    out.println("Déconnexion détectée. L'adversaire a gagné.");
                    partie.joueurDeconnecte(this);
                    return;
                }
                try {
                    int colonne = Integer.parseInt(message);
                    partieTerminee = partie.jouer(colonne, this);
                } catch (NumberFormatException e) {
                    out.println("Entrée invalide. Choisissez un nombre entre 1 et 7.");
                }
            }
        } catch (IOException e) {
            if (isConnected) {
                out.println("Une erreur de connexion s'est produite. L'adversaire a gagné.");
                partie.joueurDeconnecte(this);
            }
        } finally {
            out.println("Fin de la partie. Vous allez être déconnecté.");
        }
    }

    public boolean isConnected() {
        return !socket.isClosed() && socket.isConnected();
    }

    // Fermer la connexion proprement
    private void closeConnection() {
        try {
            isConnected = false;
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
