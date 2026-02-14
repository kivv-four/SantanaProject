import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

// ==========================================
// 1. MAIN CLASS & CONFIG
// ==========================================
public class OpenWorldRPG {
    public static void main(String[] args) {
        JFrame window = new JFrame("Java RPG: The Lost Kingdom");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Java RPG - Stardew/Ragnarok Style");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack(); // ปรับขนาดหน้าต่างให้พอดีกับ GamePanel

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread(); // เริ่ม Game Loop
    }
}

// ==========================================
// 2. GAME ENGINE (หัวใจหลักของเกม)
// ==========================================
class GamePanel extends JPanel implements Runnable {
    // การตั้งค่าหน้าจอ
    final int originalTileSize = 16; // ขนาดพื้นฐาน 16x16
    final int scale = 3; // ขยายภาพ 3 เท่า (ให้เหมือน Pixel Art ยุคเก่า)
    public final int tileSize = originalTileSize * scale; // 48x48 pixel
    
    // ขนาดหน้าจอ (Screen Size)
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;  // 768 px
    public final int screenHeight = tileSize * maxScreenRow; // 576 px

    // การตั้งค่าโลกในเกม (World Setting)
    public final int maxWorldCol = 50; // แมพกว้าง 50 บล็อก
    public final int maxWorldRow = 50; // แมพสูง 50 บล็อก

    // FPS
    int FPS = 60;

    // ระบบต่างๆ
    TileManager tileM = new TileManager(this);
    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    public CollisionChecker cChecker = new CollisionChecker(this);
    
    // Entity และ Object
    public Player player = new Player(this, keyH);
    public ArrayList<Entity> monsters = new ArrayList<>();

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true); // ป้องกันภาพกระพริบ
        this.addKeyListener(keyH);
        this.setFocusable(true);
        
        setupGame();
    }

    public void setupGame() {
        // สร้างมอนสเตอร์สไลม์ 10 ตัว กระจายตามแมพ
        for(int i=0; i<10; i++) {
            monsters.add(new GreenSlime(this));
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // GAME LOOP: Pattern มาตรฐานของเกม 2D
    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update(); // คำนวณตรรกะ
                repaint(); // วาดภาพ
                delta--;
            }
        }
    }

    public void update() {
        player.update();
        
        // Update Monsters
        for (int i = 0; i < monsters.size(); i++) {
            if(monsters.get(i) != null) {
                monsters.get(i).update();
                if(!monsters.get(i).alive) {
                    monsters.remove(i); // ลบศพมอนสเตอร์
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. วาดพื้นหลัง (Tiles)
        tileM.draw(g2);

        // 2. วาดมอนสเตอร์
        for(Entity m : monsters) {
            m.draw(g2);
        }

        // 3. วาดผู้เล่น
        player.draw(g2);

        // 4. วาด UI (เลือด)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("HP: " + player.life, 20, 40);
        g2.drawString("Monsters Left: " + monsters.size(), 20, 70);

        g2.dispose();
    }
}

// ==========================================
// 3. INPUT SYSTEM
// ==========================================
class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, leftPressed, rightPressed, attackPressed;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) upPressed = true;
        if (code == KeyEvent.VK_S) downPressed = true;
        if (code == KeyEvent.VK_A) leftPressed = true;
        if (code == KeyEvent.VK_D) rightPressed = true;
        if (code == KeyEvent.VK_SPACE) attackPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) upPressed = false;
        if (code == KeyEvent.VK_S) downPressed = false;
        if (code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_D) rightPressed = false;
        if (code == KeyEvent.VK_SPACE) attackPressed = false;
    }
}

// ==========================================
// 4. ENTITY (OOP PARENT CLASS)
// ==========================================
abstract class Entity {
    GamePanel gp;
    public int worldX, worldY; // ตำแหน่งจริงในแมพใหญ่
    public int speed;
    public String direction = "down";
    
    // Hitbox
    public Rectangle solidArea; 
    public int solidAreaDefaultX, solidAreaDefaultY;
    public boolean collisionOn = false;

    // Stats
    public int maxLife;
    public int life;
    public boolean alive = true;
    public boolean invincible = false;
    public int invincibleCounter = 0;
    
    // Animation
    public int spriteCounter = 0;
    public boolean spriteNum = true; // สลับขาซ้ายขวา

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public abstract void update();
    public abstract void draw(Graphics2D g2);
}

// ==========================================
// 5. PLAYER CLASS
// ==========================================
class Player extends Entity {
    KeyHandler keyH;
    public final int screenX;
    public final int screenY;
    private boolean attacking = false;
    private int attackTimer = 0;

    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);
        this.keyH = keyH;

        // ตำแหน่งผู้เล่นกลางจอเสมอ (Camera Follow Logic)
        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);

        // จุดเกิดใน World
        worldX = gp.tileSize * 25; 
        worldY = gp.tileSize * 25;
        speed = 4;

        // Hitbox (เล็กลงกว่าตัวนิดหน่อยเพื่อให้เดินลอดช่องแคบได้)
        solidArea = new Rectangle(8, 16, 32, 32); 
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        
        maxLife = 100;
        life = maxLife;
    }

    @Override
    public void update() {
        // Attack Logic
        if (keyH.attackPressed) {
            attacking = true;
            attackEntities();
        }
        if (attacking) {
            attackTimer++;
            if (attackTimer > 20) { // โจมตี 20 เฟรม
                attacking = false;
                attackTimer = 0;
            }
        }

        // Movement Logic
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            if (keyH.upPressed) direction = "up";
            if (keyH.downPressed) direction = "down";
            if (keyH.leftPressed) direction = "left";
            if (keyH.rightPressed) direction = "right";

            // เช็คการชนกำแพง
            collisionOn = false;
            gp.cChecker.checkTile(this);

            // ถ้าไม่ชน ก็เดินได้
            if (!collisionOn) {
                switch (direction) {
                    case "up": worldY -= speed; break;
                    case "down": worldY += speed; break;
                    case "left": worldX -= speed; break;
                    case "right": worldX += speed; break;
                }
            }
            
            // Animation เดิน
            spriteCounter++;
            if(spriteCounter > 12) {
                spriteNum = !spriteNum;
                spriteCounter = 0;
            }
        }
        
        // Invincible timer (หลังโดนชน)
        if(invincible) {
            invincibleCounter++;
            if(invincibleCounter > 60) {
                invincible = false;
                invincibleCounter = 0;
            }
        }
        
        if (life <= 0) {
            System.out.println("GAME OVER");
            // รีเซ็ตเกมแบบง่ายๆ
            life = maxLife;
            worldX = gp.tileSize * 25;
            worldY = gp.tileSize * 25;
        }
    }
    
    private void attackEntities() {
        // สร้าง Hitbox ของดาบ
        Rectangle ar = new Rectangle(worldX, worldY, gp.tileSize, gp.tileSize);
        switch(direction) {
            case "up": ar.y -= gp.tileSize; break;
            case "down": ar.y += gp.tileSize; break;
            case "left": ar.x -= gp.tileSize; break;
            case "right": ar.x += gp.tileSize; break;
        }
        
        // เช็คว่าดาบโดนมอนตัวไหนไหม
        for(Entity m : gp.monsters) {
            if(m != null) {
                // สร้าง Hitbox มอน
                Rectangle mr = new Rectangle(m.worldX + m.solidArea.x, m.worldY + m.solidArea.y, m.solidArea.width, m.solidArea.height);
                if(ar.intersects(mr)) {
                    m.life -= 10;
                    System.out.println("Hit Monster! HP: " + m.life);
                    // ผลักมอนถอยหลัง
                    switch(direction) {
                        case "up": m.worldY -= 20; break;
                        case "down": m.worldY += 20; break;
                        case "left": m.worldX -= 20; break;
                        case "right": m.worldX += 20; break;
                    }
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // วาดตัวละคร (สี่เหลี่ยมสีฟ้า)
        g2.setColor(Color.CYAN);
        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
        
        // วาดตา
        g2.setColor(Color.BLACK);
        if(direction.equals("right") || direction.equals("down")) g2.fillRect(screenX + 30, screenY + 10, 6, 6);
        if(direction.equals("left") || direction.equals("down")) g2.fillRect(screenX + 10, screenY + 10, 6, 6);
        
        // เอฟเฟกต์ฟันดาบ
        if(attacking) {
            g2.setColor(Color.WHITE);
            int ax = screenX;
            int ay = screenY;
            switch(direction) {
                case "up": ay -= gp.tileSize; break;
                case "down": ay += gp.tileSize; break;
                case "left": ax -= gp.tileSize; break;
                case "right": ax += gp.tileSize; break;
            }
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(ax, ay, gp.tileSize, gp.tileSize);
        }
    }
}

// ==========================================
// 6. MONSTER CLASS
// ==========================================
class GreenSlime extends Entity {
    private int actionLockCounter = 0;

    public GreenSlime(GamePanel gp) {
        super(gp);
        speed = 2;
        maxLife = 30;
        life = maxLife;
        
        // สุ่มจุดเกิด
        Random rand = new Random();
        worldX = rand.nextInt(50) * gp.tileSize;
        worldY = rand.nextInt(50) * gp.tileSize;
        
        solidArea = new Rectangle(3, 18, 42, 30);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    @Override
    public void update() {
        if(life <= 0) {
            alive = false;
            return;
        }

        actionLockCounter++;
        
        // AI ง่ายๆ: ทุกๆ 2 วินาที (120 เฟรม) ให้คิดว่าจะเดินไปทางไหน
        if(actionLockCounter == 120) {
            Random random = new Random();
            int i = random.nextInt(100) + 1;
            
            if(i <= 25) direction = "up";
            if(i > 25 && i <= 50) direction = "down";
            if(i > 50 && i <= 75) direction = "left";
            if(i > 75 && i <= 100) direction = "right";
            
            actionLockCounter = 0;
        }
        
        // เช็คการชน
        collisionOn = false;
        gp.cChecker.checkTile(this);
        
        if(!collisionOn) {
             switch (direction) {
                case "up": worldY -= speed; break;
                case "down": worldY += speed; break;
                case "left": worldX -= speed; break;
                case "right": worldX += speed; break;
            }
        }
        
        // ถ้าชนผู้เล่น
        int distanceX = Math.abs(worldX - gp.player.worldX);
        int distanceY = Math.abs(worldY - gp.player.worldY);
        if(distanceX < gp.tileSize && distanceY < gp.tileSize && !gp.player.invincible) {
            gp.player.life -= 10;
            gp.player.invincible = true;
            System.out.println("Ouch! Player HP: " + gp.player.life);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if(!alive) return;
        
        // คำนวณตำแหน่งบนจอ (World -> Screen)
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // วาดเฉพาะที่อยู่ในจอ (Performance optimization)
        if(worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
           worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
           worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
           worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            
            g2.setColor(Color.GREEN);
            g2.fillOval(screenX, screenY + 10, gp.tileSize, gp.tileSize - 10);
            
            // HP Bar
            double hpScale = (double)gp.tileSize/maxLife;
            g2.setColor(Color.RED);
            g2.fillRect(screenX, screenY - 10, (int)(life * hpScale), 5);
        }
    }
}

// ==========================================
// 7. TILE & MAP SYSTEM
// ==========================================
class Tile {
    public Color color;
    public boolean collision = false;
}

class TileManager {
    GamePanel gp;
    Tile[] tiles;
    int mapTileNum[][];

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tiles = new Tile[10];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap();
    }

    public void getTileImage() {
        // 0 = หญ้า
        tiles[0] = new Tile();
        tiles[0].color = new Color(34, 139, 34); // Forest Green

        // 1 = กำแพง/หิน
        tiles[1] = new Tile();
        tiles[1].color = Color.GRAY;
        tiles[1].collision = true;

        // 2 = น้ำ
        tiles[2] = new Tile();
        tiles[2].color = Color.BLUE;
        tiles[2].collision = true;
        
        // 3 = ดิน
        tiles[3] = new Tile();
        tiles[3].color = new Color(139, 69, 19);
    }

    public void loadMap() {
        // สร้างแมพแบบสุ่มๆ (Procedural Generation แบบง่าย)
        Random r = new Random();
        for(int col = 0; col < gp.maxWorldCol; col++) {
            for(int row = 0; row < gp.maxWorldRow; row++) {
                int num = r.nextInt(100);
                if(num < 80) mapTileNum[col][row] = 0; // 80% เป็นหญ้า
                else if(num < 90) mapTileNum[col][row] = 1; // 10% เป็นหิน
                else mapTileNum[col][row] = 2; // 10% เป็นน้ำ
                
                // เคลียร์พื้นที่ตรงกลางให้เดินได้แน่นอน
                if(col > 20 && col < 30 && row > 20 && row < 30) {
                    mapTileNum[col][row] = 3; // ดิน
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            
            // Logic หัวใจของ Camera: วาด Tile โดยอิงตำแหน่งผู้เล่น
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            // วาดเฉพาะในจอ
            if(worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
               worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
               worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
               worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
                
                g2.setColor(tiles[tileNum].color);
                g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                
                // ตกแต่ง: วาดเส้นตารางจางๆ
                g2.setColor(new Color(0,0,0, 20));
                g2.drawRect(screenX, screenY, gp.tileSize, gp.tileSize);
            }

            worldCol++;
            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }
}

// ==========================================
// 8. COLLISION CHECKER
// ==========================================
class CollisionChecker {
    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Entity entity) {
        // คำนวณพิกัดขอบของ Entity
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;

        int entityLeftCol = entityLeftWorldX / gp.tileSize;
        int entityRightCol = entityRightWorldX / gp.tileSize;
        int entityTopRow = entityTopWorldY / gp.tileSize;
        int entityBottomRow = entityBottomWorldY / gp.tileSize;

        int tileNum1, tileNum2;

        try {
            switch (entity.direction) {
                case "up":
                    entityTopRow = (entityTopWorldY - entity.speed) / gp.tileSize;
                    tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                    if (gp.tileM.tiles[tileNum1].collision || gp.tileM.tiles[tileNum2].collision) {
                        entity.collisionOn = true;
                    }
                    break;
                case "down":
                    entityBottomRow = (entityBottomWorldY + entity.speed) / gp.tileSize;
                    tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                    tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                    if (gp.tileM.tiles[tileNum1].collision || gp.tileM.tiles[tileNum2].collision) {
                        entity.collisionOn = true;
                    }
                    break;
                case "left":
                    entityLeftCol = (entityLeftWorldX - entity.speed) / gp.tileSize;
                    tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                    if (gp.tileM.tiles[tileNum1].collision || gp.tileM.tiles[tileNum2].collision) {
                        entity.collisionOn = true;
                    }
                    break;
                case "right":
                    entityRightCol = (entityRightWorldX + entity.speed) / gp.tileSize;
                    tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                    tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                    if (gp.tileM.tiles[tileNum1].collision || gp.tileM.tiles[tileNum2].collision) {
                        entity.collisionOn = true;
                    }
                    break;
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            // ถ้าเดินตกขอบโลก ให้ชนกำแพง
            entity.collisionOn = true;
        }
    }
}