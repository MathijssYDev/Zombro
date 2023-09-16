package zombro.UI.menu;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;

import zombro.Game;
import zombro.UI.UI;

public class homepage extends JPanel {
    Game game;
    Image backgroundImage;

    public homepage(Game game) {
        Dimension btnSize = new Dimension(200, 50);
        this.game = game;

        try {
            InputStream imgUrl = getClass().getResourceAsStream("/zombro/resources/menu/background.png");
            backgroundImage = ImageIO.read(imgUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setPreferredSize(new Dimension(350, 600));
        mainPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left
        gbc.insets = new Insets(10, 10, 10, 10);

        JButton joinButton = new JButton("Join Game");
        joinButton.setForeground(Color.WHITE);
        joinButton.setBackground(new Color(0, 0, 0, 200)); // Set black background color
        joinButton.setBorderPainted(false);
        joinButton.setFocusPainted(false);
        joinButton.setPreferredSize(btnSize);
        joinButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                joinButton.setBackground(new Color(0, 0, 0, 255)); // Set black background color
                joinButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                mainPanel.revalidate();
                mainPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                joinButton.setBackground(new Color(0, 0, 0, 200)); // Set black background color
                joinButton.setCursor(Cursor.getDefaultCursor());
                joinButton.setPreferredSize(btnSize);
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        joinButton.addActionListener(e -> {
            // Handle join game logic
        });

        mainPanel.add(joinButton, gbc);

        gbc.gridy++;

        JButton createButton = new JButton("Create Game");
        createButton.setForeground(Color.WHITE);
        createButton.setBackground(new Color(0, 0, 0, 200)); // Set black background color
        createButton.setBorderPainted(false);
        createButton.setFocusPainted(false);
        createButton.setPreferredSize(btnSize);
        createButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                createButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                createButton.setBackground(new Color(0, 0, 0, 255)); // Set black background color
                mainPanel.revalidate();
                mainPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                createButton.setBackground(new Color(0, 0, 0, 200)); // Set black background color
                createButton.setCursor(Cursor.getDefaultCursor());
                createButton.setPreferredSize(btnSize);
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        createButton.addActionListener(e -> {
            // Handle create game logic
        });

        mainPanel.add(createButton, gbc);

        add(mainPanel, new BorderLayout().WEST);

        setPreferredSize(new Dimension(1920, 1080));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, null);
    }
}
