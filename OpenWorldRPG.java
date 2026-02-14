import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

// ==========================================
// 1. MAIN CLASS
// ==========================================
public class OpenWorldRPG {
    public static void main(String[] args) {
        JFrame window = new JFrame("Java RPG V3: Smooth Animations");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}

// ==========================================
// 2. GAME ENGINE
// ==========================================
class GamePanel extends JPanel implements Runnable {
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale; // 48x48
    
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;

    int FPS = 60;
    public long gameFrame = 0; // ตัวนับเฟรมรวมของเกม (ใช้ทำอนิเมชั่น)

    TileManager tileM = new TileManager(this);
    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    public CollisionChecker cChecker = new CollisionChecker(this);
    
    public Player player = new Player(this, keyH);
    public ArrayList<Entity> monsters = new ArrayList<>();
    public ArrayList<Particle> particles = new ArrayList<>();
    public ArrayList<FloatingText> floatingTexts = new ArrayList<>();

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        spawnMonsters();
    }
    
    public void spawnMonsters() {
        monsters.clear();
        for(int i=0; i<15; i++) {
            monsters.add(new GreenSlime(this));
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

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
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        gameFrame++; // เพิ่มค่าทุกเฟรม
        player.update();
        
        Iterator<Entity> itMonster = monsters.iterator();
        while(itMonster.hasNext()){
            Entity m = itMonster.next();
            m.update();
            if(!m.alive) {
                player.gainExp(15);
                itMonster.remove();
            }
        }
        if(monsters.isEmpty()) spawnMonsters();

        Iterator<Particle> itPart = particles.iterator();
        while(itPart.hasNext()){
            Particle p = itPart.next();
            p.update();
            if(!p.alive) itPart.remove();
        }

        Iterator<FloatingText> itText = floatingTexts.iterator();
        while(itText.hasNext()){
            FloatingText t = itText.next();
            t.update();
            if(!t.alive) itText.remove();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // เปิด Anti-aliasing ให้ภาพเนียนขึ้น (ขอบไม่แตก)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        tileM.draw(g2);
        for(Particle p : particles) p.draw(g2);
        for(Entity m : monsters) m.draw(g2);
        player.draw(g2);
        for(FloatingText t : floatingTexts) t.draw(g2);
        drawUI(g2);

        g2.dispose();
    }
    
    public void drawUI(Graphics2D g2) {
        int x = screenWidth/2 - 150;
        int y = screenHeight - 40;
        g2.setColor(Color.WHITE);
        g2.drawRect(x, y, 300, 15);
        double expScale = (double)300 / player.nextLevelExp;
        g2.setColor(new Color(255, 215, 0));
        g2.fillRect(x+1, y+1, (int)(player.exp * expScale), 13);
        
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("Lv." + player.level, x-50, y+12);
        g2.drawString("HP: " + player.life + "/" + player.maxLife, 20, 40);
        
        if(player.leveledUpCounter > 0) {
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            g2.setColor(new Color(255, 255, 0, Math.min(255, player.leveledUpCounter * 5))); // Fade out effect
            g2.drawString("LEVEL UP!", screenWidth/2 - 100, screenHeight/2 - 50);
            player.leveledUpCounter--;
        }
    }
}

// ==========================================
// 3. INPUT
// ==========================================
class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, leftPressed, rightPressed, attackPressed;
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) upPressed = true;
        if (code == KeyEvent.VK_S) downPressed = true;
        if (code == KeyEvent.VK_A) leftPressed = true;
        if (code == KeyEvent.VK_D) rightPressed = true;
        if (code == KeyEvent.VK_SPACE) attackPressed = true;
    }
    @Override public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) upPressed = false;
        if (code == KeyEvent.VK_S) downPressed = false;
        if (code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_D) rightPressed = false;
        if (code == KeyEvent.VK_SPACE) attackPressed = false;
    }
}

// ==========================================
// 4. ENTITY & EFFECTS
// ==========================================
abstract class Entity {
    GamePanel gp;
    public int worldX, worldY, speed;
    public String direction = "down";
    public Rectangle solidArea; 
    public int solidAreaDefaultX, solidAreaDefaultY;
    public boolean collisionOn = false;
    public int maxLife, life;
    public boolean alive = true;
    public boolean invincible = false;
    public int invincibleCounter = 0;
    
    public Entity(GamePanel gp) { this.gp = gp; }
    public abstract void update();
    public abstract void draw(Graphics2D g2);
}

class Particle {
    GamePanel gp;
    int x, y;
    double xv, yv;
    Color color;
    int size;
    int life;
    
    public Particle(GamePanel gp, int x, int y, Color color) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.color = color;
        this.size = 8;
        this.life = 25;
        Random rand = new Random();
        this.xv = (rand.nextDouble() * 4) - 2; 
        this.yv = (rand.nextDouble() * 4) - 2; 
    }
    public void update() {
        x += xv;
        y += yv;
        life--;
        if(life < 0) this.alive = false;
    }
    public boolean alive = true;
    public void draw(Graphics2D g2) {
        int screenX = x - gp.player.worldX + gp.player.screenX;
        int screenY = y - gp.player.worldY + gp.player.screenY;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150)); // โปร่งแสง
        g2.fillOval(screenX, screenY, size, size);
    }
}

class FloatingText {
    GamePanel gp;
    String text;
    int x, y; 
    Color color;
    boolean alive = true;
    int counter = 0;
    
    public FloatingText(GamePanel gp, String text, int x, int y, Color color) {
        this.gp = gp;
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }
    public void update() {
        counter++;
        y -= 1; 
        if(counter > 50) alive = false;
    }
    public void draw(Graphics2D g2) {
        int screenX = x - gp.player.worldX + gp.player.screenX;
        int screenY = y - gp.player.worldY + gp.player.screenY;
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(Color.BLACK);
        g2.drawString(text, screenX+2, screenY+2);
        g2.setColor(color);
        g2.drawString(text, screenX, screenY);
    }
}

// ==========================================
// 5. PLAYER (HERO) - With SWORD ANIMATION
// ==========================================
class Player extends Entity {
    KeyHandler keyH;
    public final int screenX, screenY;
    private boolean attacking = false;
    private int attackTimer = 0;
    public int level = 1, strength = 5, exp = 0, nextLevelExp = 50, leveledUpCounter = 0;
    private boolean moving = false;

    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);
        this.keyH = keyH;
        screenX = gp.screenWidth/2 - (gp.tileSize/2);
        screenY = gp.screenHeight/2 - (gp.tileSize/2);
        worldX = gp.tileSize * 25; 
        worldY = gp.tileSize * 25;
        speed = 5;
        solidArea = new Rectangle(12, 16, 24, 32); 
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        maxLife = 100;
        life = maxLife;
    }

    public void gainExp(int amount) {
        exp += amount;
        if(exp >= nextLevelExp) {
            level++;
            exp = 0;
            nextLevelExp *= 1.5;
            maxLife += 20;
            life = maxLife;
            strength += 2;
            leveledUpCounter = 120;
            gp.floatingTexts.add(new FloatingText(gp, "LEVEL UP!", worldX, worldY - 40, Color.GREEN));
        }
    }

    @Override
    public void update() {
        if (keyH.attackPressed) {
            attacking = true;
            if(attackTimer == 0) attackEntities(); // Hit only once per press
        }
        
        if (attacking) {
            attackTimer++;
            if (attackTimer > 20) { // Animation duration
                attacking = false;
                attackTimer = 0;
            }
        }

        moving = false;
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            moving = true;
            if (keyH.upPressed) direction = "up";
            if (keyH.downPressed) direction = "down";
            if (keyH.leftPressed) direction = "left";
            if (keyH.rightPressed) direction = "right";

            collisionOn = false;
            gp.cChecker.checkTile(this);
            if (!collisionOn) {
                switch (direction) {
                    case "up": worldY -= speed; break;
                    case "down": worldY += speed; break;
                    case "left": worldX -= speed; break;
                    case "right": worldX += speed; break;
                }
            }
        }
        
        if(invincible) {
            invincibleCounter++;
            if(invincibleCounter > 60) {
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }
    
    private void attackEntities() {
        Rectangle ar = new Rectangle(worldX, worldY, gp.tileSize, gp.tileSize);
        switch(direction) {
            case "up": ar.y -= gp.tileSize; break;
            case "down": ar.y += gp.tileSize; break;
            case "left": ar.x -= gp.tileSize; break;
            case "right": ar.x += gp.tileSize; break;
        }
        
        for(Entity m : gp.monsters) {
            if(m != null && !m.invincible) {
                Rectangle mr = new Rectangle(m.worldX + m.solidArea.x, m.worldY + m.solidArea.y, m.solidArea.width, m.solidArea.height);
                if(ar.intersects(mr)) {
                    int dmg = strength + new Random().nextInt(5);
                    boolean isCrit = new Random().nextInt(100) < 20;
                    if(isCrit) dmg *= 2;
                    m.life -= dmg;
                    m.invincible = true;
                    m.invincibleCounter = -10;
                    generateParticles(m, new Color(100, 255, 100)); // Slime blood
                    gp.floatingTexts.add(new FloatingText(gp, String.valueOf(dmg), m.worldX, m.worldY, isCrit ? Color.YELLOW : Color.WHITE));
                    
                    // Knockback logic
                    int recoil = 40;
                    switch(direction) {
                        case "up": m.worldY -= recoil; break;
                        case "down": m.worldY += recoil; break;
                        case "left": m.worldX -= recoil; break;
                        case "right": m.worldX += recoil; break;
                    }
                }
            }
        }
    }
    
    public void generateParticles(Entity target, Color color) {
        for(int i=0; i<8; i++) {
            gp.particles.add(new Particle(gp, target.worldX, target.worldY, color));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // 1. Bobbing Animation (ตัวละครเด้งขึ้นลงเวลาเดิน)
        int bobY = 0;
        if(moving) {
            bobY = (int)(Math.sin(gp.gameFrame * 0.2) * 3); // ใช้ sine wave คำนวณการเด้ง
        }

        // Draw Player Body
        g2.setColor(Color.CYAN);
        g2.fillRect(screenX, screenY + bobY, gp.tileSize, gp.tileSize);
        
        // Cape
        g2.setColor(new Color(0, 0, 150));
        if(direction.equals("up")) g2.fillRect(screenX+10, screenY+10 + bobY, 28, 30);

        // Eyes
        g2.setColor(Color.BLACK);
        if(direction.equals("right") || direction.equals("down")) g2.fillRect(screenX + 30, screenY + 10 + bobY, 6, 6);
        if(direction.equals("left") || direction.equals("down")) g2.fillRect(screenX + 10, screenY + 10 + bobY, 6, 6);
        
        // 2. Sword Swing Animation (หมุนดาบ)
        if(attacking) {
            drawSword(g2, bobY);
        }
    }

    private void drawSword(Graphics2D g2, int bobY) {
        // บันทึกตำแหน่งการหมุนเดิมไว้ก่อน
        AffineTransform oldTransform = g2.getTransform();
        
        int swordCenterX = screenX + gp.tileSize/2;
        int swordCenterY = screenY + gp.tileSize/2 + bobY;
        
        double startAngle = 0;
        double swingRange = 120; // องศาการเหวี่ยง
        double currentSwing = (double)attackTimer / 20 * swingRange; // คำนวณองศาตามเวลา

        // ตั้งค่าจุดหมุนตามทิศทาง
        switch(direction) {
            case "up": startAngle = -45 - 90; break;
            case "down": startAngle = -45 + 90; break;
            case "left": startAngle = -45 + 180; break;
            case "right": startAngle = -45; break;
        }

        // สั่งหมุนภาพ (Rotate)
        g2.rotate(Math.toRadians(startAngle + currentSwing), swordCenterX, swordCenterY);
        
        // วาดดาบ (วาดในมุมปกติ แล้วคำสั่ง rotate ข้างบนจะหมุนมันเอง)
        g2.setColor(Color.WHITE);
        g2.fillRect(swordCenterX, swordCenterY - 40, 10, 40); // ใบดาบ
        g2.setColor(new Color(139, 69, 19));
        g2.fillRect(swordCenterX, swordCenterY, 10, 15); // ด้ามจับ

        // คืนค่าการหมุนกลับเป็นปกติ (ไม่งั้นแมพจะหมุนตาม)
        g2.setTransform(oldTransform);
    }
}

// ==========================================
// 6. MONSTER - With SQUASH & STRETCH ANIMATION
// ==========================================
class GreenSlime extends Entity {
    private int actionLockCounter = 0;
    private double animOffset; // สุ่มให้สไลม์แต่ละตัวขยับไม่พร้อมกัน

    public GreenSlime(GamePanel gp) {
        super(gp);
        speed = 2;
        maxLife = 30;
        life = maxLife;
        Random rand = new Random();
        worldX = rand.nextInt(50) * gp.tileSize;
        worldY = rand.nextInt(50) * gp.tileSize;
        solidArea = new Rectangle(3, 18, 42, 30);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        animOffset = rand.nextDouble() * 10;
    }

    @Override
    public void update() {
        if(life <= 0) { alive = false; return; }
        
        if(invincible) {
            invincibleCounter++;
            if(invincibleCounter > 10) invincible = false;
        } else {
            actionLockCounter++;
            if(actionLockCounter == 120) {
                Random random = new Random();
                int i = random.nextInt(100) + 1;
                if(i <= 25) direction = "up";
                if(i > 25 && i <= 50) direction = "down";
                if(i > 50 && i <= 75) direction = "left";
                if(i > 75 && i <= 100) direction = "right";
                actionLockCounter = 0;
            }
            
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
        }
        
        int distanceX = Math.abs(worldX - gp.player.worldX);
        int distanceY = Math.abs(worldY - gp.player.worldY);
        if(distanceX < gp.tileSize && distanceY < gp.tileSize && !gp.player.invincible) {
            gp.player.life -= 10;
            gp.player.invincible = true;
            gp.floatingTexts.add(new FloatingText(gp, "-10", gp.player.worldX, gp.player.worldY, Color.RED));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if(!alive) return;
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        if(worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
           worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
           worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
           worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            
            // Slime Breathing Animation (หายใจเข้าออก)
            // ใช้ Math.sin สร้างคลื่น -1 ถึง 1
            double breathing = Math.sin((gp.gameFrame * 0.1) + animOffset); 
            int scaleX = (int)(breathing * 3);
            int scaleY = (int)(breathing * 2);

            g2.setColor(invincible ? Color.WHITE : new Color(50, 200, 50));
            // วาดวงกลมโดยบวก/ลบขนาด เพื่อให้ดูยืดหยุ่น
            g2.fillOval(screenX - scaleX, screenY + 10 + scaleY, gp.tileSize + (scaleX*2), gp.tileSize - 10 - (scaleY*2));
            
            // Eyes
            g2.setColor(Color.BLACK);
            g2.fillOval(screenX + 10, screenY + 20, 8, 8);
            g2.fillOval(screenX + 30, screenY + 20, 8, 8);
            
            // HP Bar
            double hpScale = (double)gp.tileSize/maxLife;
            g2.setColor(Color.RED);
            g2.fillRect(screenX, screenY - 5, (int)(life * hpScale), 5);
        }
    }
}

// ==========================================
// 7. TILE & COLLISION (คงเดิม)
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
        tiles[0] = new Tile(); tiles[0].color = new Color(34, 139, 34);
        tiles[1] = new Tile(); tiles[1].color = Color.GRAY; tiles[1].collision = true;
        tiles[2] = new Tile(); tiles[2].color = new Color(0, 150, 255); tiles[2].collision = true;
        tiles[3] = new Tile(); tiles[3].color = new Color(139, 69, 19);
    }
    public void loadMap() {
        Random r = new Random();
        for(int col = 0; col < gp.maxWorldCol; col++) {
            for(int row = 0; row < gp.maxWorldRow; row++) {
                int num = r.nextInt(100);
                if(num < 80) mapTileNum[col][row] = 0;
                else if(num < 90) mapTileNum[col][row] = 1;
                else mapTileNum[col][row] = 2;
                if(col > 20 && col < 30 && row > 20 && row < 30) mapTileNum[col][row] = 3;
            }
        }
    }
    public void draw(Graphics2D g2) {
        int worldCol = 0; int worldRow = 0;
        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[worldCol][worldRow];
            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;
            if(worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
               worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
               worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
               worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
                g2.setColor(tiles[tileNum].color);
                g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
            }
            worldCol++;
            if (worldCol == gp.maxWorldCol) { worldCol = 0; worldRow++; }
        }
    }
}
class CollisionChecker {
    GamePanel gp;
    public CollisionChecker(GamePanel gp) { this.gp = gp; }
    public void checkTile(Entity entity) {
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
                    if (gp.tileM.tiles[tileNum1].collision || gp.tileM.tiles[tileNum2].collision) entity.collisionOn = true;
                    break;
                case "down":
                    entityBottomRow = (entityBottomWorldY + entity.speed) / gp.tileSize;
                    tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                    tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                    if (gp.tileM.tiles[tileNum1].collision || gp.tileM.tiles[tileNum2].collision) entity.collisionOn = true;
                    break;
                case "left":
                    entityLeftCol = (entityLeftWorldX - entity.speed) / gp.tileSize;
                    tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                    if (gp.tileM.tiles[tileNum1].collision || gp.tileM.tiles[tileNum2].collision) entity.collisionOn = true;
                    break;
                case "right":
                    entityRightCol = (entityRightWorldX + entity.speed) / gp.tileSize;
                    tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                    tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                    if (gp.tileM.tiles[tileNum1].collision || gp.tileM.tiles[tileNum2].collision) entity.collisionOn = true;
                    break;
            }
        } catch(Exception e) { entity.collisionOn = true; }
    }
}