package game;

import javax.swing.*;

/**
 * Main menu window that hosts the MainMenuPanel
 */
public class MainMenuWindow extends JFrame {
    
    public MainMenuWindow(GamePanel gamePanel) {
        setTitle("Java RPG - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        MainMenuPanel menuPanel = new MainMenuPanel(gamePanel);
        add(menuPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
