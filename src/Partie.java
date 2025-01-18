
import java.io.IOException;

public class Partie {

    private final int[][] grille = new int[7][6];  // Grille de 7 colonnes et 6 lignes
    private ClientHandler joueur1;
    private ClientHandler joueur2;
    private int tour;

    public Partie(ClientHandler joueur1, ClientHandler joueur2) {
        this.joueur1 = joueur1;
        this.joueur2 = joueur2;
        this.tour = 0;  // Joueur 1 commence
    }

    // Gérer le coup joué par un joueur
    public synchronized boolean jouer(int colonne, ClientHandler joueur) throws IOException {
        if (!estTourValide(joueur)) {
            return false;
        }

        int ligne = selectionHauteur(grille, colonne);
        if (ligne == -1) {
            envoyerMessage(joueur, "Colonne pleine.");
            return false;
        }

        // Placer le jeton dans la colonne
        grille[colonne - 1][ligne] = (tour % 2 == 0) ? 1 : 2;
        envoyerGrille();

        if (verifierVictoire(grille, (tour % 2 == 0) ? 1 : 2)) {
            envoyerVictoire(joueur);
            return true;
        }

        if (tour >= 42) {  // Égalité
            envoyerMessage(joueur1, "Égalité !");
            envoyerMessage(joueur2, "Égalité !");
            return true;
        }

        // Passer au tour suivant
        tour++;
        return false;
    }

    private boolean estTourValide(ClientHandler joueur) throws IOException {
        if ((tour % 2 == 0 && joueur != joueur1) || (tour % 2 == 1 && joueur != joueur2)) {
            envoyerMessage(joueur, "Ce n'est pas votre tour !");
            return false;
        }
        return true;
    }

    private void envoyerGrille() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int j = 5; j >= 0; j--) {  // Lignes de bas en haut
            for (int i = 0; i < 7; i++) {
                sb.append(grille[i][j]);  // Valeur dans chaque case (0, 1, ou 2)
                if (i < 6) {
                    sb.append(",");  // Séparateur entre les cases d'une ligne
                }
            }
            sb.append(";");  // Séparateur entre les lignes
        }
        String grilleStr = "grille:" + sb.toString();
        envoyerMessage(joueur1, grilleStr);
        envoyerMessage(joueur2, grilleStr);
    }

    private void envoyerMessage(ClientHandler joueur, String message) throws IOException {
        joueur.getWriter().println(message);
    }

    private void envoyerVictoire(ClientHandler joueur) throws IOException {
        envoyerMessage(joueur, "Vous avez gagné !");
        ClientHandler adversaire = (joueur == joueur1) ? joueur2 : joueur1;
        envoyerMessage(adversaire, "Vous avez perdu !");
        ServeurPuissance4.incrementerScore(joueur.getNomJoueur());
    }

    // Fonction pour sélectionner la ligne où le jeton doit être placé
    private int selectionHauteur(int[][] grille, int colonne) {
        for (int i = 0; i < 6; i++) {
            if (grille[colonne - 1][i] == 0) {
                return i;  // Retourner la première ligne vide
            }
        }
        return -1;  // Si la colonne est pleine, retourner -1
    }

    // Fonction pour vérifier la victoire d'un joueur
    private boolean verifierVictoire(int[][] grille, int jeton) {
        return verifierVictoireHorizontale(grille, jeton) || verifierVictoireVerticale(grille, jeton)
                || verifierVictoireDiagonale(grille, jeton);
    }

    private boolean verifierVictoireHorizontale(int[][] grille, int jeton) {
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 4; i++) {
                if (grille[i][j] == jeton && grille[i + 1][j] == jeton && grille[i + 2][j] == jeton && grille[i + 3][j] == jeton) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean verifierVictoireVerticale(int[][] grille, int jeton) {
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 3; j++) {
                if (grille[i][j] == jeton && grille[i][j + 1] == jeton && grille[i][j + 2] == jeton && grille[i][j + 3] == jeton) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean verifierVictoireDiagonale(int[][] grille, int jeton) {
        // Vérification diagonale (gauche à droite)
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                if (grille[i][j] == jeton && grille[i + 1][j + 1] == jeton && grille[i + 2][j + 2] == jeton && grille[i + 3][j + 3] == jeton) {
                    return true;
                }
            }
        }

        // Vérification diagonale (droite à gauche)
        for (int i = 3; i < 7; i++) {
            for (int j = 0; j < 3; j++) {
                if (grille[i][j] == jeton && grille[i - 1][j + 1] == jeton && grille[i - 2][j + 2] == jeton && grille[i - 3][j + 3] == jeton) {
                    return true;
                }
            }
        }

        return false;  // Pas de victoire
    }

    // Gérer la déconnexion d'un joueur
    public void joueurDeconnecte(ClientHandler joueurDeconnecte) {
        ClientHandler autreJoueur = (joueurDeconnecte == joueur1) ? joueur2 : joueur1;

        // Informer l'autre joueur que son adversaire est déconnecté
        try {
            envoyerMessage(autreJoueur, joueurDeconnecte.getNomJoueur() + " s'est déconnecté. Vous avez gagné !");
            ServeurPuissance4.incrementerScore(autreJoueur.getNomJoueur());
            autreJoueur.getWriter().println("Vous allez être déconnecté.");
            autreJoueur.getWriter().flush();
            autreJoueur.getWriter().close();
            autreJoueur.getSocket().close();  // Fermer la socket de l'autre joueur
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthodes pour obtenir les joueurs de la partie
    public ClientHandler getJoueur1() {
        return joueur1;
    }

    public ClientHandler getJoueur2() {
        return joueur2;
    }
}
