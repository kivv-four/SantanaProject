package game;

import java.awt.*;
import javax.swing.*;

public class CharacterSelectionWindow extends JFrame {
    
    private CharacterSelectionPanel selectionPanel;
    
    public CharacterSelectionWindow(GamePanel gamePanel) {
        setTitle("Java RPG - เลือกตัวละคร");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        selectionPanel = new CharacterSelectionPanel(gamePanel);
        selectionPanel.setPreferredSize(new Dimension(gamePanel.screenWidth, gamePanel.screenHeight));
        add(selectionPanel);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public void closeWindow() {
        setVisible(false);
        dispose();
    }
}
