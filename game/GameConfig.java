package game;

/**
 * ค่าคงที่ของเกม (จอ แมพ FPS การสปอawn).
 * แก้ค่าที่นี่ที่เดียวเมื่อต้องการปรับความละเอียดหรือขนาดโลก
 */
public final class GameConfig {

    private GameConfig() {}

    // Tile & screen
    public static final int ORIGINAL_TILE_SIZE = 16;
    public static final int SCALE = 3;
    public static final int MAX_SCREEN_COL = 16;
    public static final int MAX_SCREEN_ROW = 12;

    // World
    public static final int MAX_WORLD_COL = 50;
    public static final int MAX_WORLD_ROW = 50;

    // Loop
    public static final int FPS = 60;

    // Spawn
    public static final int SPAWN_MONSTER_COUNT = 15;
    public static final int SPAWN_MARGIN = 2;
    
    // Character
    public static final double CHARACTER_SCALE = 1.7; // ขยายตัวละครให้เท่า monster
    public static final double IDLE_SCALE_MULTIPLIER = 0.55; // idle ต้องขยาย 0.5 ของ CHARACTER_SCALE เพื่อดูเท่า run
    public static final int IDLE_FRAMES = 4; // จำนวน frames ของ idle animation
}
