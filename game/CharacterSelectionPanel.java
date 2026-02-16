package game;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class CharacterSelectionPanel extends JPanel {
    
    private GamePanel gamePanel;
    private CharacterConfig selectedCharacter = null;
    private Rectangle wizardRect;
    private Rectangle knightRect;
    private Rectangle startButtonRect;
    private boolean wizardHover = false;
    private boolean knightHover = false;
    private boolean startHover = false;
    
    public CharacterSelectionPanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        setLayout(null);
        setBackground(new Color(30, 30, 50));
        setFocusable(true);
        System.out.println("CharacterSelectionPanel created");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // คำนวณตำแหน่ง button
        int buttonWidth = 150;
        int buttonHeight = 150;
        int wizardX = width / 4 - buttonWidth / 2;
        int knightX = 3 * width / 4 - buttonWidth / 2;
        int buttonY = 150;
        
        wizardRect = new Rectangle(wizardX, buttonY, buttonWidth, buttonHeight);
        knightRect = new Rectangle(knightX, buttonY, buttonWidth, buttonHeight);
        
        // Title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String title = "เลือกตัวละคร";
        FontMetrics fm = g2.getFontMetrics();
        int titleX = (width - fm.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 80);
        
        // วาด Wizard button
        drawCharacterButton(g2, wizardRect, "Wizard", wizardHover, selectedCharacter == CharacterConfig.WIZARD);
        
        // วาด Knight button
        drawCharacterButton(g2, knightRect, "Knight", knightHover, selectedCharacter == CharacterConfig.KNIGHT);
        
        // วาด Start button
        startButtonRect = new Rectangle(width / 2 - 100, 450, 200, 60);
        drawStartButton(g2, startButtonRect);
    }
    
    private void drawCharacterButton(Graphics2D g2, Rectangle rect, String name, boolean hover, boolean selected) {
        // พื้นหลัง
        if (selected) {
            g2.setColor(new Color(150, 100, 200));
        } else if (hover) {
            g2.setColor(new Color(120, 100, 160));
        } else {
            g2.setColor(new Color(80, 80, 120));
        }
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        
        // Border
        if (selected) {
            g2.setColor(Color.CYAN);
            g2.setStroke(new BasicStroke(4));
        } else {
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2));
        }
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
        
        // Text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(name)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(name, textX, textY);
    }
    
    private void drawStartButton(Graphics2D g2, Rectangle rect) {
        // พื้นหลัง
        if (selectedCharacter == null) {
            g2.setColor(new Color(100, 100, 100));
        } else if (startHover) {
            g2.setColor(new Color(150, 220, 150));
        } else {
            g2.setColor(new Color(100, 200, 100));
        }
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        
        // Border
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
        
        // Text
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String text = "เริ่มเกม";
        FontMetrics fm = g2.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, textX, textY);
    }
    
    private void handleMouseClick(int x, int y) {
        if (wizardRect != null && wizardRect.contains(x, y)) {
            selectedCharacter = CharacterConfig.WIZARD;
            repaint();
        } else if (knightRect != null && knightRect.contains(x, y)) {
            selectedCharacter = CharacterConfig.KNIGHT;
            repaint();
        } else if (startButtonRect != null && startButtonRect.contains(x, y) && selectedCharacter != null) {
            gamePanel.startGameWithCharacter(selectedCharacter);
        }
    }
    
    private void handleMouseMove(int x, int y) {
        boolean oldWizardHover = wizardHover;
        boolean oldKnightHover = knightHover;
        boolean oldStartHover = startHover;
        
        wizardHover = wizardRect != null && wizardRect.contains(x, y);
        knightHover = knightRect != null && knightRect.contains(x, y);
        startHover = startButtonRect != null && startButtonRect.contains(x, y) && selectedCharacter != null;
        
        if (oldWizardHover != wizardHover || oldKnightHover != knightHover || oldStartHover != startHover) {
            repaint();
        }
    }
    
    public CharacterConfig getSelectedCharacter() {
        return selectedCharacter;
    }
}


