# Application Puissance 4 - Client-Serveur

## Auteurs

- **Julien BAILLY**
- **David DEFLANDRE**
- **Fahed BOUAMRANI**
- **Amin EL MELLOUKI**

## Description du projet

Cette application est une implémentation du jeu **Puissance 4** en mode client-serveur. Le jeu se déroule dans le terminal et permet à plusieurs joueurs de se connecter à un serveur pour jouer en ligne. Chaque joueur peut voir les autres joueurs en attente, démarrer une partie et interagir via des commandes textuelles.

## Fonctionnalités principales

### Côté Client :

- Connexion à un serveur en précisant l'adresse IP et un nom de joueur.
- Affichage de la liste des joueurs en attente (nom, nombre de victoires et pourcentage de victoires).
- Participation à une partie de Puissance 4 dans une grille de 6 lignes et 7 colonnes.
- Déconnexion du serveur sans provoquer d'erreurs côté serveur.

### Côté Serveur :

- Gestion des connexions des clients et des parties.
- Gestion des tours de jeu et de la détection de victoire (alignement de 4 jetons).
- Envoi de l'état du plateau après chaque coup.
- Gestion des statistiques des joueurs (nombre de victoires sauvegardé dans un fichier texte).
- Persistance des scores entre les sessions du serveur.
- Détection de déconnexion d'un joueur.

## Déroulé d'une partie

1. **Connexion au serveur** : 
   Le client se connecte en précisant l'adresse IP du serveur et un nom de joueur. Si le nom est valide, la connexion est acceptée.
   
2. **Début de la partie** : 
   Dès qu'un joueur est connecté, il est placé dans une file d'attente. Le serveur associe automatiquement les deux premiers joueurs en attente pour démarrer une partie. Le joueur ne peut pas encore choisir un adversaire spécifique.

3. **Jeu de Puissance 4** : 
   Les joueurs jouent à tour de rôle en déposant leurs jetons dans une des colonnes (1 à 7) jusqu'à ce que l'un d'eux aligne 4 jetons ou que la grille soit remplie, entraînant une égalité.

4. **Fin de partie** : 
   Une fois la partie terminée, le joueur est déconnecté du serveur. Il doit se reconnecter s'il souhaite jouer une autre partie. Pour le moment, le score est sauvegardé dans un fichier texte, mais il n'est pas encore attribué à l'adversaire si l'un des joueurs quitte la partie.

5. **Déconnexion** : 
   Le joueur peut quitter le serveur sans provoquer de crash du serveur, mais cela n'entraîne pas encore de mise à jour des scores pour l'adversaire.

## Protocole de Communication

Les échanges entre le client et le serveur se font sous forme de requêtes textuelles. Voici quelques commandes utilisées :

- **connect nomJoueur** : Demande de connexion avec le nom de joueur. Le serveur répond `OK` ou `ERR message` en cas de problème.
- **play nomColonne** : Pendant une partie, dépose un jeton dans la colonne spécifiée (1 à 7). Le serveur valide ou refuse le coup avec un message `OK` ou `ERR message`.
- **exit** : Déconnexion manuelle du client. Cela permet au joueur de quitter le serveur proprement, mais pour l'instant, le score de l'adversaire n'est pas encore mis à jour correctement en cas de déconnexion d'un joueur.
- **disconnect** : Le client est déconnecté automatiquement à la fin d'une partie.

Note : Pour le moment, il n'est pas encore possible de choisir un adversaire ou de relancer une partie après la fin sans devoir se reconnecter.

## Installation

### Prérequis

- Java JDK 8 ou supérieur.
- Accès réseau pour permettre la communication entre le client et le serveur.

### Instructions

1. Clonez le projet sur votre machine.
2. Compilez le code source avec la commande `javac` :
   ```bash
   javac -d bin src/*.java
## Manuel d'utilisation

1. Lancez le serveur :
    ```bash
    java -cp bin ServeurPuissance4
2. Lancez un client en précisant l'IP du serveur et un nom de joueur :
    ```bash
    java -cp bin <IP> <NomJoueur>
3. Début de partie : Une fois connecté, attendez qu'un adversaire se connecte également. Le serveur vous mettra automatiquement en jeu contre le premier joueur en file d'attente.
4. Jouer : Pendant votre tour, entrez le numéro de la colonne où vous souhaitez placer votre jeton.
5. Quitter le serveur

## Justification des choix techniques
- **Architecture Client-Serveur** : Nous avons utilisé des sockets TCP pour permettre la communication fiable entre le client et le serveur.
- **Persistance des données** : Les statistiques des joueurs (nombre de victoires) sont sauvegardées dans un fichier texte pour assurer leur persistance après redémarrage du serveur.
- **Multithreading** : Le serveur est capable de gérer plusieurs parties simultanément grâce à l'utilisation des threads pour chaque partie active.

## Limitations actuelles

- **Choix de l'adversaire** : Le joueur ne peut pas encore choisir un adversaire spécifique, il est automatiquement mis en jeu contre le premier joueur disponible.
- **Relance de partie** : Après la fin d'une partie, le joueur est déconnecté et doit se reconnecter s'il souhaite rejouer.
- **Historique des parties** : L'historique des parties n'est pas encore implémenté.
- **Déconnexion** : Si un joueur quitte une partie en cours, le serveur ne met pas encore à jour correctement les scores pour l'adversaire restant.

## Diagramme des classes

Le diagramme des classes est disponible dans le dossier `documentation`.

## Prochaines étapes

- Implémentation du choix d'un adversaire lors du lancement d'une partie.
- Possibilité de relancer une partie sans déconnexion.
- Ajout de la mise à jour correcte des scores en cas de déconnexion d'un joueur.
- Implémentation de l'historique des parties.

## Ressources

Ce projet s'appuie sur les compétences développées dans les modules :

- R3.05 : Programmation Système
- R3.06 : Architecture des réseaux

## Livrables

- Code source commenté
- Javadoc
- Rapport complet avec manuel d'utilisation, diagramme des classes et détails du protocole

## Modifications effectuées :

1. **Pas de choix d'adversaire** : Le joueur est automatiquement mis en file d'attente, et le premier joueur en attente est pris comme adversaire.
2. **Déconnexion après la partie** : Le joueur est automatiquement déconnecté à la fin de la partie et doit se reconnecter pour rejouer.
3. **Déconnexion propre sans mise à jour des scores** : Le joueur peut quitter le serveur proprement, mais cela n'impacte pas encore correctement les scores de l'adversaire.
4. **Historique des parties non implémenté** : L'historique des parties n'est pas encore en place.

Ce fichier reflète l'état actuel de votre projet et peut être mis à jour au fur et à mesure que vous implémentez les fonctionnalités restantes.
