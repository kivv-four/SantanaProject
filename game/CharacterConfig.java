package game;

import java.awt.Rectangle;

public class CharacterConfig {
    public final String name;
    public final String spritePath;
    public final double runScale;
    public final double idleScaleMultiplier;
    public final int idleFrames;
    public final int runFrames;
    public final Rectangle solidArea;
    public final int maxLife;
    public final int speed;
    public final int strength;
    
    public CharacterConfig(String name, String spritePath, double runScale, double idleScaleMultiplier,
                          int idleFrames, int runFrames, Rectangle solidArea,
                          int maxLife, int speed, int strength) {
        this.name = name;
        this.spritePath = spritePath;
        this.runScale = runScale;
        this.idleScaleMultiplier = idleScaleMultiplier;
        this.idleFrames = idleFrames;
        this.runFrames = runFrames;
        this.solidArea = solidArea;
        this.maxLife = maxLife;
        this.speed = speed;
        this.strength = strength;
    }
    
    // ตัวละครแบบ Wizard
    public static final CharacterConfig WIZARD = new CharacterConfig(
        "Wizard",
        "resource/player/wizard/",
        1.7,           // runScale
        0.5,           // idleScaleMultiplier
        4,             // idleFrames
        6,             // runFrames
        new Rectangle(7, 22, 34, 43),  // solidArea
        100,           // maxLife
        5,             // speed
        5              // strength
    );
    
    // ตัวละครแบบ Knight
    public static final CharacterConfig KNIGHT = new CharacterConfig(
        "Knight",
        "resource/player/knight/",
        1.7,           // runScale
        0.5,           // idleScaleMultiplier
        4,             // idleFrames
        6,             // runFrames
        new Rectangle(7, 22, 34, 43),  // solidArea (เหมือน wizard)
        100,           // maxLife
        5,             // speed
        5              // strength
    );
}
