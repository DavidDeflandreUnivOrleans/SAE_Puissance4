
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientPuissance4 {

    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static JFrame frame;  // Fenêtre principale
    private static JLabel[][] gridLabels;  // Représentation visuelle du plateau
    private static JPanel gridPanel;  // Panneau contenant la grille
    private static JTextField columnField;  // Champ de saisie de la colonne

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ClientPuissance4 <IP> <NomJoueur>");
            return;
        }

        String serverAddress = args[0];
        String nomJoueur = args[1];

        try {
            // Connexion au serveur
            socket = new Socket(serverAddress, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Envoi du nom du joueur au serveur
            out.println(nomJoueur);

            // Lire la première réponse (accueil ou erreur)
            String response = in.readLine();
            if (response.startsWith("ERR")) {
                System.out.println("Erreur : " + response);
                return;
            }
            System.out.println("Connexion au serveur réussie : " + response);

            // Démarrer l'interface graphique
            createGUI(nomJoueur);

            // Ecouter les messages du serveur
            while ((response = in.readLine()) != null) {
                System.out.println("Message du serveur : " + response);
                if (response.startsWith("Tour")) {
                    JOptionPane.showMessageDialog(frame, "C'est votre tour de jouer.", "Tour de jeu", JOptionPane.INFORMATION_MESSAGE);
                } else if (response.startsWith("Vous avez gagné")) {
                    JOptionPane.showMessageDialog(frame, "Félicitations ! Vous avez gagné !", "Victoire", JOptionPane.INFORMATION_MESSAGE);
                } else if (response.startsWith("Vous avez perdu")) {
                    JOptionPane.showMessageDialog(frame, "Dommage ! Vous avez perdu.", "Défaite", JOptionPane.INFORMATION_MESSAGE);
                } else if (response.startsWith("Égalité")) {
                    JOptionPane.showMessageDialog(frame, "La partie est terminée avec une égalité.", "Égalité", JOptionPane.INFORMATION_MESSAGE);
                } else if (response.startsWith("grille:")) {
                    // Mise à jour du plateau graphique
                    String grilleData = response.substring(7);  // Retirer "grille:"
                    updateGrid(grilleData);
                }
            }

        } catch (IOException e) {
            System.out.println("Erreur de connexion au serveur : " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    // Création de l'interface graphique pour le jeu
    private static void createGUI(String nomJoueur) {
        frame = new JFrame(nomJoueur);  // Titre de la fenêtre est le nom du joueur
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLayout(new BorderLayout());

        // Panneau pour les boutons de colonne
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 7));

        // Ajouter un bouton pour chaque colonne
        for (int col = 1; col <= 7; col++) {
            JButton columnButton = new JButton(String.valueOf(col));
            int columnNumber = col; // Pour l'utiliser dans l'ActionListener

            columnButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    out.println(columnNumber); // Envoie le numéro de colonne au serveur
                }
            });

            buttonPanel.add(columnButton); // Ajouter le bouton au panneau
        }

        // Ajouter le panneau des boutons en haut de la fenêtre
        frame.add(buttonPanel, BorderLayout.NORTH);

        // Création du panneau pour la grille de jeu
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(6, 7));  // 6 lignes, 7 colonnes
        gridLabels = new JLabel[6][7];

        // Initialisation des cellules de la grille
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                gridLabels[i][j] = new JLabel(" ", SwingConstants.CENTER);
                gridLabels[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                gridLabels[i][j].setOpaque(true);
                gridLabels[i][j].setBackground(Color.WHITE);  // Fond blanc pour les cellules vides
                gridPanel.add(gridLabels[i][j]);
            }
        }

        // Ajouter la grille à la fenêtre
        frame.add(gridPanel, BorderLayout.CENTER);

        // Panneau pour entrer un coup
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(1, 2));

        JLabel label = new JLabel("Entrez une colonne (1 à 7): ");
        columnField = new JTextField();

        JButton playButton = new JButton("Jouer");

        // Action du bouton "Jouer" qui envoie le coup au serveur
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String columnText = columnField.getText();
                try {
                    int colonne = Integer.parseInt(columnText);  // Conversion en entier
                    if (colonne >= 1 && colonne <= 7) {
                        out.println(colonne);  // Envoi de la colonne choisie au serveur
                    } else {
                        JOptionPane.showMessageDialog(frame, "Veuillez entrer une colonne valide (1 à 7).");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Veuillez entrer un nombre valide.");
                }
                columnField.setText("");  // Effacer le champ après l'entrée
            }
        });

        // Ajout des composants au panneau d'entrée
        inputPanel.add(label);
        inputPanel.add(columnField);
        inputPanel.add(playButton);

        // Ajouter le panneau d'entrée en bas de la fenêtre
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Afficher la fenêtre
        frame.setVisible(true);
    }

    // Mise à jour de la grille à partir du message reçu du serveur
    private static void updateGrid(String grille) {
        String[] lignes = grille.split(";");  // Séparer les lignes par ';'
        for (int i = 0; i < 6; i++) {
            String[] cellules = lignes[i].split(",");  // Séparer les colonnes par ','
            for (int j = 0; j < 7; j++) {
                String valeur = cellules[j].trim();  // Retirer les espaces
                if (valeur.equals("1")) {
                    gridLabels[i][j].setBackground(Color.RED);  // Jeton du joueur 1
                } else if (valeur.equals("2")) {
                    gridLabels[i][j].setBackground(Color.YELLOW);  // Jeton du joueur 2
                } else {
                    gridLabels[i][j].setBackground(Color.WHITE);  // Cellule vide
                }
            }
        }
    }

    // Fermeture de la connexion et de l'interface graphique
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

        if (frame != null) {
            frame.dispose();  // Fermer la fenêtre graphique
        }

        System.out.println("Connexion fermée.");
    }
}
