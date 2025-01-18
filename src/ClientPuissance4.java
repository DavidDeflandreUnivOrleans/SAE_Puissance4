import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientPuissance4 {

    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ClientPuissance4 <IP> <NomJoueur>");
            return;
        }

        String serverAddress = args[0];
        String nomJoueur = args[1];

        try {
            socket = new Socket(serverAddress, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(nomJoueur);

            String response = in.readLine();
            if (response.startsWith("ERR")) {
                System.out.println("Erreur : " + response);
                return;
            }
            System.out.println("Connexion au serveur réussie : " + response);

            while ((response = in.readLine()) != null) {
                System.out.println("Message du serveur : " + response);
                if (response.startsWith("Tour")) {
                    System.out.println("C'est votre tour de jouer.");
                } else if (response.startsWith("Vous avez gagné")) {
                    System.out.println("Félicitations ! Vous avez gagné !");
                } else if (response.startsWith("Vous avez perdu")) {
                    System.out.println("Dommage ! Vous avez perdu.");
                } else if (response.startsWith("Égalité")) {
                    System.out.println("La partie est terminée avec une égalité.");
                } else if (response.startsWith("grille:")) {
                    String grilleData = response.substring(7);
                    afficherGrille(grilleData);
                }

                if (response.startsWith("Tour")) {
                    System.out.print("Entrez une colonne (1 à 7) : ");
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String colonne = br.readLine();
                    out.println(colonne);
                }
            }

        } catch (IOException e) {
            System.out.println("Erreur de connexion au serveur : " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private static void afficherGrille(String grille) {
        String[] lignes = grille.split(";");
        for (String ligne : lignes) {
            System.out.println(ligne.replace(",", " | "));
        }
        System.out.println();
    }

    private static void closeConnection() {
        try {
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
            System.out.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }

        System.out.println("Connexion fermée.");
    }
}