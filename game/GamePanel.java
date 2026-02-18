package game;

import effect.FloatingText;
import effect.Particle;
import entity.Entity;
import entity.GreenSlime;
import entity.Player;
import input.KeyHandler;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import world.CollisionChecker;
import world.TileManager;

public class GamePanel extends JPanel implements Runnable {

    // Screen (จาก GameConfig)
    public final int tileSize = GameConfig.ORIGINAL_TILE_SIZE * GameConfig.SCALE;
    public final int maxScreenCol = GameConfig.MAX_SCREEN_COL;
    public final int maxScreenRow = GameConfig.MAX_SCREEN_ROW;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // World
    public final int maxWorldCol = GameConfig.MAX_WORLD_COL;
    public final int maxWorldRow = GameConfig.MAX_WORLD_ROW;

    private static final int FPS = GameConfig.FPS;
    public long gameFrame = 0;
    public boolean gameOver = false;
    public boolean paused = false;

    // Systems
    public TileManager tileM = new TileManager(this);
    public KeyHandler keyH = new KeyHandler();
    public CollisionChecker cChecker = new CollisionChecker(this);
    private Thread gameThread;

    // Entities & effects
    public Player player;
    public ArrayList<Entity> monsters = new ArrayList<>();
    public ArrayList<Particle> particles = new ArrayList<>();
    public ArrayList<FloatingText> floatingTexts = new ArrayList<>();
    
    // Character selection
    private CharacterConfig selectedCharacterConfig;
    private boolean gameStarted = false;
    private JFrame gameWindow;

    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.black);
        setDoubleBuffered(true);
        addKeyListener(keyH);
        setFocusable(true);
    }

    /** สร้างมอนสเตอร์สุ่มบนแมพ (เว้นขอบ) จนครบ SPAWN_MONSTER_COUNT บน tile ที่ไม่ชนได้ */
    public void spawnMonsters() {
        monsters.clear();
        int spawned = 0;
        java.util.Random random = new java.util.Random();
        int margin = GameConfig.SPAWN_MARGIN;

        while (spawned < GameConfig.SPAWN_MONSTER_COUNT) {
            int col = random.nextInt(maxWorldCol - margin * 2) + margin;
            int row = random.nextInt(maxWorldRow - margin * 2) + margin;
            int tileNum = tileM.mapTileNum[col][row];
            if (!tileM.tiles[tileNum].collision) {
                GreenSlime m = new GreenSlime(this);
                m.worldX = col * tileSize;
                m.worldY = row * tileSize;
                monsters.add(m);
                spawned++;
            }
        }
    }

    /** กลับไปยังเมนูหลัก (ปิด game window และสร้าง main menu ใหม่) */
    public void returnToMainMenu() {
        System.out.println("[GamePanel] Returning to main menu");
        if (gameWindow != null) {
            gameWindow.dispose();
            gameWindow = null;
        }
        gameStarted = false;
        gameOver = false;
        paused = false;
        gameFrame = 0;
        if (gameThread != null) {
            gameThread = null;
        }
        // Create and show main menu
        SwingUtilities.invokeLater(() -> new MainMenuWindow(this));
    }
    
    public void setGameWindow(JFrame window) {
        this.gameWindow = window;
    }
    public void resetGame() {
        player = new Player(this, keyH, selectedCharacterConfig);
        monsters.clear();
        particles.clear();
        floatingTexts.clear();
        spawnMonsters();
        gameOver = false;
        gameFrame = 0;
        keyH.restartPressed = false;
    }
    
    /** เรียกโดย CharacterSelectionPanel เมื่อผู้เล่นเลือกตัวละคร */
    public void startGameWithCharacter(CharacterConfig config) {
        System.out.println("Starting game with character: " + config.name);
        selectedCharacterConfig = config;
        
        // สร้าง player ด้วย config ที่เลือก
        player = new Player(this, keyH, config);
        spawnMonsters();
        gameStarted = true;
        
        // สร้าง game window
        gameWindow = new JFrame("Java RPG V3: " + config.name);
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setResizable(false);
        gameWindow.add(this);
        gameWindow.pack();
        gameWindow.setLocationRelativeTo(null);
        gameWindow.setVisible(true);
        // Ensure the game panel has keyboard focus for input
        this.requestFocusInWindow();
        
        // เริ่มเกม
        startGameThread();
        System.out.println("Game started: " + gameStarted);
    }

    /** เริ่มเกมลูปใน thread แยก */
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    /** อัปเดตหนึ่งเฟรม: ถ้า Game Over รอ R; ถ้า Paused ให้รอ Space หรือ ESC; ไม่เช่นนั้นอัปเดตผู้เล่น มอนสเตอร์ เอฟเฟกต์ และสปอawn ถ้าหมด */
    public void update() {
        // Handle pause toggle
        if (keyH.pausePressed && !gameOver) {
            paused = !paused;
            keyH.pausePressed = false; // Consume key
            System.out.println("[GamePanel] Pause toggled: " + paused);
        }
        
        // Handle exit from pause
        if (paused && keyH.exitPressed) {
            keyH.exitPressed = false;
            System.out.println("[GamePanel] Exiting to main menu from pause");
            returnToMainMenu();
        }
        
        if (gameOver) {
            if (keyH.restartPressed) resetGame();
            return;
        }
        
        if (paused) {
            return; // Game is paused, skip update
        }
        
        gameFrame++;
        player.update();
        if (player.life <= 0) {
            if (player.deathFinished) gameOver = true;
        }
        updateEntities(monsters);
        updateParticles();
        updateFloatingTexts();
        if (monsters.isEmpty()) spawnMonsters();
    }

    /** อัปเดต entity ใน list; ถ้าตายให้ exp (GreenSlime) แล้วลบออก */
    private void updateEntities(ArrayList<Entity> list) {
        Iterator<Entity> it = list.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            e.update();
            if (!e.alive) {
                if (e instanceof GreenSlime) player.gainExp(15);
                it.remove();
            }
        }
    }

    private void updateParticles() {
        particles.removeIf(p -> { p.update(); return !p.alive; });
    }

    private void updateFloatingTexts() {
        floatingTexts.removeIf(t -> { t.update(); return !t.alive; });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // ถ้ายังไม่เริ่มเกม ให้แสดง character selection panel เท่านั้น
        if (!gameStarted) {
            return;
        }
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        tileM.draw(g2);
        for (Particle p : particles) p.draw(g2);
        for (Entity m : monsters) m.draw(g2);
        player.draw(g2);
        for (FloatingText t : floatingTexts) t.draw(g2);
        drawUI(g2);
        g2.dispose();
    }

    /** วาด UI: HP, EXP bar, Level, LEVEL UP popup และหน้าจอ Game Over */
    public void drawUI(Graphics2D g2) {
        int x = screenWidth / 2 - 150;
        int y = screenHeight - 40;

        g2.setColor(Color.WHITE);
        g2.drawRect(x, y, 300, 15);
        double expScale = 300.0 / player.nextLevelExp;
        g2.setColor(new Color(255, 215, 0));
        g2.fillRect(x + 1, y + 1, (int) (player.exp * expScale), 13);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("Lv." + player.level, x - 50, y + 12);
        g2.drawString("HP: " + player.life + "/" + player.maxLife, 20, 40);

        if (player.leveledUpCounter > 0) {
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            g2.setColor(new Color(255, 255, 0, Math.min(255, player.leveledUpCounter * 5)));
            g2.drawString("LEVEL UP!", screenWidth / 2 - 100, screenHeight / 2 - 50);
            player.leveledUpCounter--;
        }

        if (paused) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            g2.setColor(new Color(255, 200, 0));
            g2.drawString("PAUSED", screenWidth / 2 - 130, screenHeight / 2 - 50);
            g2.setFont(new Font("Arial", Font.BOLD, 25));
            g2.setColor(Color.WHITE);
            g2.drawString("Press ESC to Resume", screenWidth / 2 - 120, screenHeight / 2 + 20);
            g2.drawString("Press Q to Exit", screenWidth / 2 - 80, screenHeight / 2 + 70);
        }

        if (gameOver) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            g2.setColor(Color.RED);
            g2.drawString("GAME OVER", screenWidth / 2 - 165, screenHeight / 2);
            g2.setFont(new Font("Arial", Font.BOLD, 25));
            g2.setColor(Color.WHITE);
            g2.drawString("Press 'R' to Restart", screenWidth / 2 - 110, screenHeight / 2 + 60);
        }
    }
}
