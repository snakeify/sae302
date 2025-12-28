package com.mycompany.serveurudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;

public class ServeurUDP {

    static final int PORT = 3333;
    static final int MAX_MESSAGES = 10;
    static final int MAX_UTILISATEURS = 4;
    static final int MAX_AMIS = 4;

    static HashMap<String, String> utilisateurs = new HashMap<>();
    static HashMap<String, LinkedList<String>> messagesUtilisateurs = new HashMap<>();
    static HashMap<String, LinkedList<String>> demandesAmi = new HashMap<>();
    static HashMap<String, LinkedList<String>> amis = new HashMap<>();

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Serveur UDP démarré sur le port " + PORT);

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String messageRecu = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Message reçu : " + messageRecu);

                String reponse = traiterMessage(messageRecu);

                byte[] responseBytes = reponse.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                socket.send(responsePacket);
            }
        } catch (SocketException e) {
            System.err.println("Erreur de socket : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur d'E/S : " + e.getMessage());
        }
    }

    private static String traiterMessage(String message) {
        String[] parties = message.split(",");
        if (parties.length < 2) {
            return "Erreur : format de message incorrect";
        }

        String utilisateur = parties[0].trim();
        String commande = parties[1].trim();

        switch (commande) {
            case "inscription":
                return inscrireUtilisateur(utilisateur, parties);
            case "connexion":
                return connecterUtilisateur(utilisateur, parties);
            case "envoyer_message":
                return envoyerMessage(utilisateur, parties);
            case "lecture":
                return lireMessages(utilisateur);
            case "ajouter_ami":
                return ajouterAmi(utilisateur, parties);
            case "liste_amis":
                return obtenirListeAmis(utilisateur);
            case "voir_demandes_ami":
                return voirDemandesAmi(utilisateur);
            case "repondre_demande_ami":
                return repondreDemandeAmi(utilisateur, parties);
            default:
                return "Commande inconnue : " + commande;
        }
    }

    private static String inscrireUtilisateur(String utilisateur, String[] parties) {
        if (parties.length < 3) return "Erreur : email et mot de passe requis";
        String motDePasse = parties[2];

        if (utilisateurs.containsKey(utilisateur)) return "Erreur : utilisateur déjà existant";
        if (utilisateurs.size() >= MAX_UTILISATEURS) return "Erreur : nombre maximum d'utilisateurs atteint";

        utilisateurs.put(utilisateur, motDePasse);
        messagesUtilisateurs.put(utilisateur, new LinkedList<>());
        demandesAmi.put(utilisateur, new LinkedList<>());
        amis.put(utilisateur, new LinkedList<>());

        return "Utilisateur inscrit avec succès : " + utilisateur;
    }

    private static String connecterUtilisateur(String utilisateur, String[] parties) {
        if (parties.length < 3) return "Erreur : email et mot de passe requis";
        String motDePasse = parties[2];

        if (!utilisateurs.containsKey(utilisateur)) return "Erreur : utilisateur non inscrit";
        if (!utilisateurs.get(utilisateur).equals(motDePasse)) return "Erreur : mot de passe incorrect";

        return "Connexion réussie";
    }

    private static String envoyerMessage(String utilisateur, String[] parties) {
        if (!utilisateurs.containsKey(utilisateur)) return "Erreur : utilisateur non inscrit";
        if (parties.length < 4) return "Erreur : destinataire et contenu requis";

        String destinataire = parties[2].trim();
        String contenu = parties[3].trim();

        if (destinataire.equals("EVERYONE")) {
            if (amis.containsKey(utilisateur)) {
                for (String ami : amis.get(utilisateur)) {
                    LinkedList<String> boiteAmi = messagesUtilisateurs.get(ami);
                    if (boiteAmi.size() >= MAX_MESSAGES) boiteAmi.removeFirst();
                    boiteAmi.add("De " + utilisateur + " (EVERYONE) : " + contenu);
                }
                return "Message envoyé à TOUS les amis.";
            }
            return "Erreur : vous n'avez pas d'amis.";
        }

        if (!utilisateurs.containsKey(destinataire)) return "Erreur : destinataire introuvable";
        if (!amis.containsKey(utilisateur) || !amis.get(utilisateur).contains(destinataire)) {
            return "Erreur : vous devez être ami avec " + destinataire + " pour envoyer un message";
        }

        LinkedList<String> boiteDestinataire = messagesUtilisateurs.get(destinataire);
        if (boiteDestinataire.size() >= MAX_MESSAGES) boiteDestinataire.removeFirst();
        boiteDestinataire.add("De " + utilisateur + " : " + contenu);

        return "Message envoyé à " + destinataire;
    }

    private static String lireMessages(String utilisateur) {
        if (!messagesUtilisateurs.containsKey(utilisateur)) return "Erreur : utilisateur non trouvé";

        LinkedList<String> boite = messagesUtilisateurs.get(utilisateur);
        if (boite.isEmpty()) return "Aucun message pour " + utilisateur;

        StringBuilder sb = new StringBuilder("Messages pour " + utilisateur + " :\n");
        for (String msg : boite) sb.append(msg).append("\n");

        return sb.toString();
    }

    private static String ajouterAmi(String utilisateur, String[] parties) {
        if (parties.length < 3) return "Erreur : email de l'ami requis";
        String amiEmail = parties[2].trim();

        if (!utilisateurs.containsKey(amiEmail)) return "Erreur : utilisateur introuvable";
        if (utilisateur.equals(amiEmail)) return "Erreur : vous ne pouvez pas vous ajouter vous-même.";
        if (amis.get(utilisateur).contains(amiEmail)) return "Erreur : vous êtes déjà amis avec " + amiEmail;

        demandesAmi.get(amiEmail).add(utilisateur);
        return "Demande d'ami envoyée à " + amiEmail;
    }

    private static String obtenirListeAmis(String utilisateur) {
        LinkedList<String> liste = amis.get(utilisateur);
        if (liste.isEmpty()) return "Vous n'avez aucun ami dans votre liste.";

        StringBuilder sb = new StringBuilder("Liste d'amis pour " + utilisateur + " :\n");
        for (String ami : liste) sb.append(ami).append("\n");

        return sb.toString();
    }

    private static String voirDemandesAmi(String utilisateur) {
        LinkedList<String> demandes = demandesAmi.get(utilisateur);
        if (demandes.isEmpty()) return "Aucune demande d'ami en attente.";

        StringBuilder sb = new StringBuilder("Demandes d'ami pour " + utilisateur + " :\n");
        for (String d : demandes) sb.append(d).append("\n");

        return sb.toString();
    }

    private static String repondreDemandeAmi(String utilisateur, String[] parties) {
        if (parties.length < 4) return "Erreur : email de l'expéditeur et réponse requis";

        String demandeur = parties[2].trim();
        String reponse = parties[3].trim().toLowerCase();

        if (!demandesAmi.get(utilisateur).contains(demandeur)) {
            return "Erreur : aucune demande d'ami de " + demandeur;
        }

        if (reponse.equals("accepter")) {
            if (amis.get(utilisateur).size() >= MAX_AMIS) return "Erreur : vous avez atteint la limite maximale d'amis.";
            if (amis.get(demandeur).size() >= MAX_AMIS) return "Erreur : " + demandeur + " a atteint la limite maximale d'amis.";

            amis.get(utilisateur).add(demandeur);
            amis.get(demandeur).add(utilisateur);
            demandesAmi.get(utilisateur).remove(demandeur);

            messagesUtilisateurs.get(demandeur).add("Votre demande d'ami a été acceptée par " + utilisateur + ".");
            return "Demande d'ami de " + demandeur + " acceptée.";
        } else if (reponse.equals("refuser")) {
            demandesAmi.get(utilisateur).remove(demandeur);
            return "Demande d'ami de " + demandeur + " refusée.";
        } else {
            return "Erreur : réponse invalide. Utilisez 'accepter' ou 'refuser'.";
        }
    }
}
