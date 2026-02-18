package game;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * Main menu UI with Play and Exit buttons
 */
public class MainMenuPanel extends JPanel {
    
    private GamePanel gamePanel;
    private Rectangle playButton, exitButton;
    private boolean playHovered = false, exitHovered = false;
    
    public MainMenuPanel(GamePanel gp) {
        this.gamePanel = gp;
        setPreferredSize(new Dimension(gp.screenWidth, gp.screenHeight));
        setBackground(new Color(20, 40, 60));
        setFocusable(true);
        
        int buttonWidth = 200;
        int buttonHeight = 60;
        int centerX = gp.screenWidth / 2 - buttonWidth / 2;
        playButton = new Rectangle(centerX, gp.screenHeight / 2 - 100, buttonWidth, buttonHeight);
        exitButton = new Rectangle(centerX, gp.screenHeight / 2 + 20, buttonWidth, buttonHeight);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (playButton.contains(e.getPoint())) {
                    System.out.println("[MainMenuPanel] Play clicked");
                    showCharacterSelection();
                } else if (exitButton.contains(e.getPoint())) {
                    System.out.println("[MainMenuPanel] Exit clicked");
                    System.exit(0);
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                playHovered = playButton.contains(e.getPoint());
                exitHovered = exitButton.contains(e.getPoint());
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                playHovered = false;
                exitHovered = false;
                repaint();
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                playHovered = playButton.contains(e.getPoint());
                exitHovered = exitButton.contains(e.getPoint());
                repaint();
            }
        });
    }
    
    private void showCharacterSelection() {
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (currentFrame != null) currentFrame.dispose();
        
        CharacterSelectionWindow selectionWindow = new CharacterSelectionWindow(gamePanel);
        selectionWindow.setVisible(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Title
        g2.setFont(new Font("Arial", Font.BOLD, 80));
        g2.setColor(new Color(255, 200, 0));
        String title = "JAVA RPG";
        FontMetrics fm = g2.getFontMetrics();
        int titleX = (gamePanel.screenWidth - fm.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 150);
        
        // Play button
        g2.setColor(playHovered ? new Color(100, 200, 255) : new Color(50, 150, 200));
        g2.fillRect(playButton.x, playButton.y, playButton.width, playButton.height);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(playButton.x, playButton.y, playButton.width, playButton.height);
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        fm = g2.getFontMetrics();
        String playText = "PLAY";
        int playTextX = playButton.x + (playButton.width - fm.stringWidth(playText)) / 2;
        int playTextY = playButton.y + ((playButton.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(playText, playTextX, playTextY);
        
        // Exit button
        g2.setColor(exitHovered ? new Color(255, 100, 100) : new Color(200, 50, 50));
        g2.fillRect(exitButton.x, exitButton.y, exitButton.width, exitButton.height);
        g2.setColor(Color.WHITE);
        g2.drawRect(exitButton.x, exitButton.y, exitButton.width, exitButton.height);
        String exitText = "EXIT";
        int exitTextX = exitButton.x + (exitButton.width - fm.stringWidth(exitText)) / 2;
        int exitTextY = exitButton.y + ((exitButton.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(exitText, exitTextX, exitTextY);
        
        g2.dispose();
    }
}
