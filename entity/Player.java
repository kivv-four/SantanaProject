package entity;

import effect.FloatingText;
import effect.Particle;
import game.CharacterConfig;
import game.GamePanel;
import input.KeyHandler;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;

public class Player extends Entity {

    private final KeyHandler keyH;
    public final int screenX, screenY;
    private boolean attacking = false;
    private int attackTimer = 0;
    public int level = 1, strength = 5, exp = 0, nextLevelExp = 50, leveledUpCounter = 0;
    private boolean moving = false;
    private String lastHorizontalDirection = "right"; // เก็บทิศทางซ้าย/ขวาล่าสุด
    
    // Animation sprites
    private final Map<String, BufferedImage[]> spriteFrames = new HashMap<>();
    private final Map<String, BufferedImage[]> idleFrames = new HashMap<>();
    private int animationFrameCounter = 0;
    private final int FRAME_DURATION = 10; // 10 game frames ต่อ 1 sprite frame
    private CharacterConfig characterConfig;

    public Player(GamePanel gp, KeyHandler keyH) {
        this(gp, keyH, CharacterConfig.WIZARD);
    }
    
    public Player(GamePanel gp, KeyHandler keyH, CharacterConfig config) {
        super(gp);
        this.keyH = keyH;
        this.characterConfig = config;
        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);
        worldX = gp.tileSize * 25;
        worldY = gp.tileSize * 25;
        speed = config.speed;
        solidArea = new Rectangle(config.solidArea.x, config.solidArea.y, config.solidArea.width, config.solidArea.height);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        maxLife = config.maxLife;
        life = maxLife;
        loadSprites();
    }
    
    /** โหลดรูปภาพ sprite จาก folder */
    private void loadSprites() {
        try {
            // ลองหลาย path ที่อาจจะเป็น
            String[] possiblePaths = {
                characterConfig.spritePath + "run/",
                "SantanaProject/" + characterConfig.spritePath + "run/",
                "../" + characterConfig.spritePath + "run/",
                "./" + characterConfig.spritePath + "run/"
            };
            
            String basePath = null;
            for (String path : possiblePaths) {
                if (new File(path + "run_left_0.png").exists() || new File(path + "run_left_0.PNG").exists()) {
                    basePath = path;
                    break;
                }
            }
            
            if (basePath == null) {
                System.err.println("ไม่พบโฟลเดอร์ " + characterConfig.spritePath + "run/ ที่: " + new java.io.File(".").getAbsolutePath());
                spriteFrames.put("left", null);
                spriteFrames.put("right", null);
                spriteFrames.put("up", null);
                spriteFrames.put("down", null);
                idleFrames.put("idle", null);
                return;
            }
            
            System.out.println("พบรูปภาพของ " + characterConfig.name + " ที่: " + basePath);
            
            // โหลด run frames ตามจำนวนใน config
            BufferedImage[] leftFrames = new BufferedImage[characterConfig.runFrames];
            for (int i = 0; i < characterConfig.runFrames; i++) {
                String filePath = basePath + "run_left_" + i + ".png";
                File file = new File(filePath);
                if (!file.exists()) {
                    filePath = basePath + "run_left_" + i + ".PNG";
                }
                leftFrames[i] = ImageIO.read(new File(filePath));
            }
            spriteFrames.put("left", leftFrames);
            
            // โหลด run frames ขวา
            BufferedImage[] rightFrames = new BufferedImage[characterConfig.runFrames];
            for (int i = 0; i < characterConfig.runFrames; i++) {
                String filePath = basePath + "run_right_" + i + ".png";
                File file = new File(filePath);
                if (!file.exists()) {
                    filePath = basePath + "run_right_" + i + ".PNG";
                }
                rightFrames[i] = ImageIO.read(new File(filePath));
            }
            spriteFrames.put("right", rightFrames);
            
            // เดินขึ้นและลงใช้รูปเดียวกับซ้าย
            spriteFrames.put("up", leftFrames);
            spriteFrames.put("down", leftFrames);
            
            // โหลด idle animation
            String[] idlePossiblePaths = {
                characterConfig.spritePath + "idle/",
                "SantanaProject/" + characterConfig.spritePath + "idle/",
                "../" + characterConfig.spritePath + "idle/",
                "./" + characterConfig.spritePath + "idle/"
            };
            
            String idleBasePath = null;
            for (String path : idlePossiblePaths) {
                if (new File(path + "Idle_left_0.png").exists() || new File(path + "Idle_left_0.PNG").exists()) {
                    idleBasePath = path;
                    break;
                }
            }
            
            if (idleBasePath != null) {
                // โหลด idle ซ้าย ตามจำนวนใน config
                BufferedImage[] idleLefts = new BufferedImage[characterConfig.idleFrames];
                for (int i = 0; i < characterConfig.idleFrames; i++) {
                    String filePath = idleBasePath + "Idle_left_" + i + ".png";
                    File file = new File(filePath);
                    if (!file.exists()) {
                        filePath = idleBasePath + "Idle_left_" + i + ".PNG";
                    }
                    if (file.exists()) {
                        idleLefts[i] = ImageIO.read(new File(filePath));
                    } else {
                        idleLefts[i] = leftFrames[0]; // Fallback
                    }
                }
                idleFrames.put("idle_left", idleLefts);
                
                // โหลด idle ขวา
                BufferedImage[] idleRights = new BufferedImage[characterConfig.idleFrames];
                for (int i = 0; i < characterConfig.idleFrames; i++) {
                    String filePath = idleBasePath + "Idle_right_" + i + ".png";
                    File file = new File(filePath);
                    if (!file.exists()) {
                        filePath = idleBasePath + "Idle_right_" + i + ".PNG";
                    }
                    if (file.exists()) {
                        idleRights[i] = ImageIO.read(new File(filePath));
                    } else {
                        idleRights[i] = rightFrames[0]; // Fallback
                    }
                }
                idleFrames.put("idle_right", idleRights);
                
                System.out.println("โหลด idle animation สำเร็จ!");
            } else {
                System.out.println("ไม่พบ idle animation, ใช้ run frame แทน");
                idleFrames.put("idle_left", leftFrames);
                idleFrames.put("idle_right", rightFrames);
            }
            
            System.out.println("โหลดรูปภาพสำเร็จ!");
        } catch (Exception e) {
            System.err.println("ผิดพลาดในการโหลดรูปภาพ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** เพิ่ม exp; ถ้าครบ nextLevelExp จะ level up (เพิ่ม maxLife, strength, แสดง LEVEL UP) */
    public void gainExp(int amount) {
        exp += amount;
        if (exp >= nextLevelExp) {
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
        if (life <= 0) return;

        if (keyH.attackPressed && !attacking) {
            attacking = true;
            attackTimer = 0;
            attackEntities();
        }
        if (attacking) {
            attackTimer++;
            if (attackTimer > 20) {
                attacking = false;
                attackTimer = 0;
            }
        }

        moving = false;
        if (keyH.upPressed && keyH.lastKey.equals("up")) { direction = "up"; moving = true; }
        else if (keyH.downPressed && keyH.lastKey.equals("down")) { direction = "down"; moving = true; }
        else if (keyH.leftPressed && keyH.lastKey.equals("left")) { direction = "left"; moving = true; lastHorizontalDirection = "left"; }
        else if (keyH.rightPressed && keyH.lastKey.equals("right")) { direction = "right"; moving = true; lastHorizontalDirection = "right"; }
        else if (keyH.upPressed) { direction = "up"; moving = true; }
        else if (keyH.downPressed) { direction = "down"; moving = true; }
        else if (keyH.leftPressed) { direction = "left"; moving = true; lastHorizontalDirection = "left"; }
        else if (keyH.rightPressed) { direction = "right"; moving = true; lastHorizontalDirection = "right"; }

        if (moving) {
            collisionOn = false;
            gp.cChecker.checkTile(this);
            if (!collisionOn) {
                switch (direction) {
                    case "up" -> worldY -= speed;
                    case "down" -> worldY += speed;
                    case "left" -> worldX -= speed;
                    case "right" -> worldX += speed;
                }
            }
            if (worldY < 0) worldY = 0;
            if (worldX < 0) worldX = 0;
            int maxX = (gp.maxWorldCol * gp.tileSize) - gp.tileSize;
            int maxY = (gp.maxWorldRow * gp.tileSize) - gp.tileSize;
            if (worldX > maxX) worldX = maxX;
            if (worldY > maxY) worldY = maxY;
            
            // อัพเดต animation frame เมื่อเคลื่อนที่
            animationFrameCounter++;
        } else {
            // เมื่อยืนนิ่ง - animation idle ยังไหวต่อ
            animationFrameCounter++;
        }

        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 60) {
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }

    /** สร้าง hitbox โจมตีตามทิศทาง เช็ค intersect กับมอนสเตอร์ โดนแล้วเรียก damageMonster */
    private void attackEntities() {
        int attackAreaSize = gp.tileSize;
        Rectangle ar = new Rectangle(worldX, worldY, attackAreaSize, attackAreaSize);
        switch (direction) {
            case "up" -> ar.y -= gp.tileSize;
            case "down" -> ar.y += gp.tileSize;
            case "left" -> ar.x -= gp.tileSize;
            case "right" -> ar.x += gp.tileSize;
        }
        for (Entity m : gp.monsters) {
            if (m != null && m.alive && !m.invincible) {
                Rectangle mr = new Rectangle(
                    m.worldX + m.solidArea.x, m.worldY + m.solidArea.y,
                    m.solidArea.width, m.solidArea.height);
                if (ar.intersects(mr)) damageMonster(m);
            }
        }
    }

    /** คำนวณดาเมจ (strength + สุ่ม), 20% crit, ลด HP ตั้ง invincible สร้าง particle + floating text แล้ว applyRecoil */
    private void damageMonster(Entity m) {
        int dmg = strength + new Random().nextInt(5);
        boolean isCrit = new Random().nextInt(100) < 20;
        if (isCrit) dmg *= 2;
        m.life -= dmg;
        m.invincible = true;
        m.invincibleCounter = 0;
        generateParticles(m, new Color(100, 255, 100));
        gp.floatingTexts.add(new FloatingText(gp, String.valueOf(dmg), m.worldX, m.worldY, isCrit ? Color.YELLOW : Color.WHITE));
        applyRecoil(m);
    }

    /** ผลัก entity ไปทางทิศทางที่ผู้เล่นหัน โดยเช็ค tile collision ทีละ step */
    private void applyRecoil(Entity m) {
        int recoilDistance = 48;
        int step = 2;
        String originalDir = m.direction;
        m.direction = this.direction;
        for (int i = 0; i < recoilDistance; i += step) {
            m.collisionOn = false;
            gp.cChecker.checkTile(m);
            if (!m.collisionOn) {
                switch (m.direction) {
                    case "up" -> m.worldY -= step;
                    case "down" -> m.worldY += step;
                    case "left" -> m.worldX -= step;
                    case "right" -> m.worldX += step;
                }
            } else break;
        }
        m.direction = originalDir;
    }

    /** สร้าง particle จำนวน 8 ตัวที่ตำแหน่ง target */
    public void generateParticles(Entity target, Color color) {
        for (int i = 0; i < 8; i++)
            gp.particles.add(new Particle(gp, target.worldX, target.worldY, color));
    }

    @Override
    public void draw(Graphics2D g2) {
        // คำนวณ sprite frame ปัจจุบัน
        int currentFrame;
        int bobY = moving ? (int) (Math.sin(gp.gameFrame * 0.2) * 3) : 0;
        
        // เลือกประเภท animation
        BufferedImage[] frames;
        
        if (!moving) {
            // ยืนนิ่ง - ใช้ idle animation ตามทิศทาง (วิ่ง slow)
            int idleFrameDuration = FRAME_DURATION * 2; // idle ช้า 2 เท่า
            currentFrame = (animationFrameCounter / idleFrameDuration) % characterConfig.idleFrames;
            if (lastHorizontalDirection.equals("left")) {
                frames = idleFrames.get("idle_left");
            } else {
                frames = idleFrames.get("idle_right");
            }
        } else {
            // เดิน - animation เร็ว
            currentFrame = (animationFrameCounter / FRAME_DURATION) % characterConfig.runFrames;
            if (direction.equals("up") || direction.equals("down")) {
                // เดินขึ้น/ลง - ใช้รูปตาม lastHorizontalDirection
                frames = spriteFrames.get(lastHorizontalDirection);
            } else {
                // เดินซ้าย/ขวา
                frames = spriteFrames.get(direction);
            }
        }
        
        if (frames != null && frames[currentFrame] != null) {
            // วาดรูปภาพ sprite - bottom align ให้เท่ากันเสมอ
            int runScaledSize = (int) (gp.tileSize * characterConfig.runScale);
            int idleScaledSize = (int) (gp.tileSize * characterConfig.runScale * characterConfig.idleScaleMultiplier);
            
            int scaledSize = moving ? runScaledSize : idleScaledSize;
            int offsetX = (gp.tileSize - scaledSize) / 2;
            
            // Calculate base Y (bottom align point) - ใช้ run size เป็นอ้างอิง
            int baseY = screenY + (gp.tileSize - runScaledSize) / 2 + runScaledSize;
            // Draw Y = base - ความสูงของ sprite ที่จะวาด
            int drawY = baseY - scaledSize + bobY;
            
            g2.drawImage(frames[currentFrame], screenX + offsetX, drawY, scaledSize, scaledSize, null);
        } else {
            // Fallback: วาดเป็นสี่เหลี่ยมถ้าโหลดรูปไม่ได้
            g2.setColor(Color.CYAN);
            g2.fillRect(screenX, screenY + bobY, gp.tileSize, gp.tileSize);
            g2.setColor(new Color(0, 0, 150));
            if (direction.equals("up")) g2.fillRect(screenX + 10, screenY + 10 + bobY, 28, 30);
            g2.setColor(Color.BLACK);
            if (direction.equals("right") || direction.equals("down")) g2.fillRect(screenX + 30, screenY + 10 + bobY, 6, 6);
            if (direction.equals("left") || direction.equals("down")) g2.fillRect(screenX + 10, screenY + 10 + bobY, 6, 6);
        }
        
        // DEBUG: วาด hitbox
        g2.setColor(new Color(0, 255, 0, 100)); // สีเขียวโปร่งใส
        int hitboxScreenX = screenX + solidArea.x;
        int hitboxScreenY = screenY + solidArea.y + bobY;
        g2.fillRect(hitboxScreenX, hitboxScreenY, solidArea.width, solidArea.height);
        g2.setColor(Color.GREEN);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(hitboxScreenX, hitboxScreenY, solidArea.width, solidArea.height);
        
        if (attacking) drawSword(g2, bobY);
    }

    /** วาดดาบหมุนตาม attackTimer และทิศทาง */
    private void drawSword(Graphics2D g2, int bobY) {
        AffineTransform oldTransform = g2.getTransform();
        int swordCenterX = screenX + gp.tileSize / 2;
        int swordCenterY = screenY + gp.tileSize / 2 + bobY;
        double startAngle = 0;
        double swingRange = 120;
        double currentSwing = (double) (attackTimer + 1) / 20 * swingRange;
        switch (direction) {
            case "up" -> startAngle = -45 - 90;
            case "down" -> startAngle = -45 + 90;
            case "left" -> startAngle = -45 + 180;
            case "right" -> startAngle = -45;
        }
        g2.rotate(Math.toRadians(startAngle + currentSwing), swordCenterX, swordCenterY);
        g2.setColor(Color.WHITE);
        g2.fillRect(swordCenterX, swordCenterY - 40, 10, 40);
        g2.setColor(new Color(139, 69, 19));
        g2.fillRect(swordCenterX, swordCenterY, 10, 15);
        g2.setTransform(oldTransform);
    }
}
