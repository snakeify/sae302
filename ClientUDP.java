package com.mycompany.clientudp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import javax.swing.border.TitledBorder;

public class ClientUDP {
    static final int PORT = 3333;
    private static DatagramSocket socket;
    private static String currentUser = "";
    private static JFrame frame;
    private static JTextArea messageArea;
    private static JButton loginButton, signupButton, sendMessageButton, readMessagesButton, logoutButton, addFriendButton, friendListButton, manageRequestsButton;

    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color DARK_PANEL = new Color(45, 45, 45);
    private static final Color ACCENT_COLOR = new Color(98, 114, 123);
    private static final Color ACCENT_HOVER = new Color(120, 140, 150);
    private static final Color ACCENT_DISABLED = new Color(55, 60, 65);
    private static final Color TEXT_COLOR = new Color(220, 220, 240);
    private static final Color TEXT_DISABLED = new Color(150, 150, 150);

    public static void main(String[] args) {
        try {
            socket = new DatagramSocket();
            SwingUtilities.invokeLater(ClientUDP::showWelcomePage);
        } catch (SocketException e) {
            JOptionPane.showMessageDialog(null, "Erreur socket : " + e.getMessage());
        }
    }

    private static void showWelcomePage() {
        frame = new JFrame("DARK MANGO");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(DARK_BG);

        JLabel titleLabel = new JLabel("DARK MANGO");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(ACCENT_COLOR);

        JLabel subtitleLabel = new JLabel("Créé par ANTONIO Catarino, BOUDIF Tarek, Paul-Emile NGUYEN-TAN-HON et HADDADI Ilyas");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(TEXT_COLOR);

        JButton startButton = createModernButton("Entrer");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> createAndShowGUI());

        welcomePanel.add(Box.createVerticalStrut(100));
        welcomePanel.add(titleLabel);
        welcomePanel.add(Box.createVerticalStrut(20));
        welcomePanel.add(subtitleLabel);
        welcomePanel.add(Box.createVerticalStrut(50));
        welcomePanel.add(startButton);

        frame.add(welcomePanel);
        frame.setVisible(true);
    }

    private static void createAndShowGUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());
        frame.setSize(800, 600);
        frame.getContentPane().setBackground(DARK_BG);

        messageArea = new JTextArea(15, 30);
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageArea.setBackground(DARK_PANEL);
        messageArea.setForeground(TEXT_COLOR);
        messageArea.setCaretColor(TEXT_COLOR);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(DARK_BG);
        TitledBorder titleBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ACCENT_COLOR), "Messages");
        titleBorder.setTitleColor(TEXT_COLOR);
        titleBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        messagePanel.setBorder(titleBorder);
        messagePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        buttonsPanel.setBackground(DARK_BG);

        loginButton = createModernButton("Se connecter");
        signupButton = createModernButton("S'inscrire");
        sendMessageButton = createModernButton("Envoyer un message");
        readMessagesButton = createModernButton("Lire les messages");
        logoutButton = createModernButton("Se déconnecter");
        addFriendButton = createModernButton("Ajouter un ami");
        friendListButton = createModernButton("Liste d'amis");
        manageRequestsButton = createModernButton("Gérer les demandes");

        buttonsPanel.add(loginButton);
        buttonsPanel.add(signupButton);
        buttonsPanel.add(sendMessageButton);
        buttonsPanel.add(readMessagesButton);
        buttonsPanel.add(addFriendButton);
        buttonsPanel.add(friendListButton);
        buttonsPanel.add(manageRequestsButton);
        buttonsPanel.add(logoutButton);

        disableButtons();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.EAST);

        frame.add(mainPanel);
        frame.revalidate();
        frame.repaint();

        loginButton.addActionListener(e -> showUserDialog("login"));
        signupButton.addActionListener(e -> showUserDialog("inscription"));
        sendMessageButton.addActionListener(e -> showSendMessageDialog());
        readMessagesButton.addActionListener(e -> readMessages());
        logoutButton.addActionListener(e -> logout());
        addFriendButton.addActionListener(e -> addFriend());
        friendListButton.addActionListener(e -> viewFriendList());
        manageRequestsButton.addActionListener(e -> manageFriendRequests());
    }

    private static JButton createModernButton(String text) {
        JButton button = new JButton(text) {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : ACCENT_DISABLED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(ACCENT_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(ACCENT_COLOR);
            }
        });

        return button;
    }

    private static void disableButtons() {
        sendMessageButton.setEnabled(false);
        readMessagesButton.setEnabled(false);
        logoutButton.setEnabled(false);
        addFriendButton.setEnabled(false);
        friendListButton.setEnabled(false);
        manageRequestsButton.setEnabled(false);
    }


    

    private static void showUserDialog(String action) {
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] fields = { "Email :", emailField, "Mot de passe :", passwordField };

        String title = action.equals("login") ? "Se connecter" : "S'inscrire";
        int option = JOptionPane.showConfirmDialog(frame, fields, title, JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                if (action.equals("inscription") && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                    JOptionPane.showMessageDialog(frame, "Format de l'email invalide !");
                    return;
                }
                if (action.equals("inscription")) {
                    sendDataToServer(email + ",inscription," + password);
                } else {
                    String response = sendDataToServerAndReceive(email + ",connexion," + password);
                    if (response.contains("Connexion réussie")) {
                        currentUser = email;
                        enableButtons();
                        messageArea.append("Connecté en tant que : " + currentUser + "\n");
                    } else {
                        JOptionPane.showMessageDialog(frame, response);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Veuillez remplir tous les champs !");
            }
        }
    }
    
    private static void enableButtons() {
        sendMessageButton.setEnabled(true);
        readMessagesButton.setEnabled(true);
        logoutButton.setEnabled(true);
        addFriendButton.setEnabled(true);
        friendListButton.setEnabled(true);
        manageRequestsButton.setEnabled(true);
    }

    private static void showSendMessageDialog() {
        JTextField recipientField = new JTextField();
        JTextField messageField = new JTextField();
        Object[] fields = { "Destinataire :", recipientField, "Message :", messageField };

        int option = JOptionPane.showConfirmDialog(frame, fields, "Envoyer un message", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String recipient = recipientField.getText().trim();
            String message = messageField.getText().trim();
            if (!recipient.isEmpty() && !message.isEmpty()) {
                sendDataToServer(currentUser + ",envoyer_message," + recipient + "," + message);
            } else {
                JOptionPane.showMessageDialog(frame, "Veuillez remplir tous les champs !");
            }
        }
    }

    private static void sendDataToServer(String data) {
        try {
            InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
            byte[] sendData = data.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, PORT);
            socket.send(sendPacket);

            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            SwingUtilities.invokeLater(() -> messageArea.append("Serveur : " + response + "\n"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Erreur de communication : " + e.getMessage());
        }
    }

    private static String sendDataToServerAndReceive(String data) {
        try {
            InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
            byte[] sendData = data.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, PORT);
            socket.send(sendPacket);

            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket);

            return new String(receivePacket.getData(), 0, receivePacket.getLength());
        } catch (IOException e) {
            return "Erreur de communication : " + e.getMessage();
        }
    }

    private static void readMessages() {
        String messages = sendDataToServerAndReceive(currentUser + ",lecture");
        messageArea.append(messages + "\n");
    }

    private static void addFriend() {
        String friendEmail = JOptionPane.showInputDialog(frame, "Entrez l'email de l'ami à ajouter :");
        if (friendEmail != null && !friendEmail.isEmpty()) {
            sendDataToServer(currentUser + ",ajouter_ami," + friendEmail);
        }
    }

    private static void viewFriendList() {
        if (!currentUser.isEmpty()) {
            sendDataToServer(currentUser + ",liste_amis");
        } else {
            JOptionPane.showMessageDialog(frame, "Veuillez vous connecter d'abord !");
        }
    }

    private static void manageFriendRequests() {
        if (!currentUser.isEmpty()) {
            String requests = sendDataToServerAndReceive(currentUser + ",voir_demandes_ami");
            if (requests.isEmpty() || requests.contains("aucune")) {
                JOptionPane.showMessageDialog(frame, "Aucune demande d'ami.");
                return;
            }
            JTextArea requestArea = new JTextArea(requests);
            requestArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(requestArea);

            int option = JOptionPane.showConfirmDialog(frame, scrollPane, "Demandes d'amis", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                respondToFriendRequests();
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Veuillez vous connecter d'abord !");
        }
    }

    private static void respondToFriendRequests() {
        JTextField senderField = new JTextField();
        String[] options = {"Accepter", "Refuser"};
        JComboBox<String> responseBox = new JComboBox<>(options);

        Object[] fields = { "Email de l'expéditeur :", senderField, "Réponse :", responseBox };
        int option = JOptionPane.showConfirmDialog(frame, fields, "Répondre à une demande d'ami", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String senderEmail = senderField.getText().trim();
            String response = responseBox.getSelectedItem().toString().toLowerCase();

            if (!senderEmail.isEmpty()) {
                sendDataToServer(currentUser + ",repondre_demande_ami," + senderEmail + "," + response);
            } else {
                JOptionPane.showMessageDialog(frame, "Champs vide !");
            }
        }
    }

    private static void logout() {
        currentUser = "";
        messageArea.setText("");
        disableButtons();
    }
}
