
import java.util.Queue;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * The {@code DebuffEntity} class is a special type of {@code MovingEntity} that
 * holds a {@code FlappyBirdDebuff} which grants a disadvantage and challenge to
 * a {@code FlappyBird} in the Flappy Bird Game Engine. The {@code DebuffEntity}
 * moves based on its debuff type and does not necessarily move exclusively up
 * or down
 *
 * @author dylan
 */
public class DebuffEntity extends MovingEntity {

    private final FlappyBirdDebuff debuff;
    private double entitySpeed;
    private boolean isMovingLeft;

    /**
     * Creates a new {@code DebuffEntity} with a {@code FlappyBirdDebuff} to
     * hold on to, a speed that it will move left or right, a specified position
     * and dimension
     *
     * @param debuff the {@code FlappyBirdDebuff} that the {@code DebuffEntity}
     * will contain
     * @param entitySpeed the speed that the {@code DebuffEntity} will move on
     * each update
     * @param isMovingLeft true if the {@code DebuffEntity} is moving left
     * @param startTime the time in the Game Engine that this
     * {@code DebuffEntity} is created
     * @param posX the left-most position of the {@code DebuffEntity}
     * @param posY the bottom-most position of the {@code DebuffEntity}
     * @param width the width of the {@code DebuffEntity}
     * @param height the height of the {@code DebuffEntity}
     * @throws IllegalArgumentException the entity speed is less than 0
     */
    public DebuffEntity(FlappyBirdDebuff debuff, double entitySpeed, boolean isMovingLeft,
            long startTime, double posX, double posY, double width, double height) {
        super(startTime, posX, posY, width, height);
        this.debuff = debuff;
        this.entitySpeed = entitySpeed;
        this.isMovingLeft = isMovingLeft;
    }

    /**
     * Creates a new {@code DebuffEntity} with a {@code FlappyBirdDebuff} to
     * hold on to, a speed that it will move left or right, a specified
     * position, dimension, and an image to render on this {@code DebuffEntity}
     *
     * @param debuff the {@code FlappyBirdDebuff} that the {@code DebuffEntity}
     * will contain
     * @param entitySpeed the speed that the {@code DebuffEntity} will move on
     * each update
     * @param isMovingLeft true if the {@code DebuffEntity} is moving left
     * @param startTime the time in the Game Engine that this
     * {@code DebuffEntity} is created
     * @param posX the left-most position of the {@code DebuffEntity}
     * @param posY the bottom-most position of the {@code DebuffEntity}
     * @param width the width of the {@code DebuffEntity}
     * @param height the height of the {@code DebuffEntity}
     * @param imageFilePath a string representing the file path to the image
     * that will be rendered on the {@code DebuffEntity}
     * @throws IllegalArgumentException the entity speed is less than 0
     */
    public DebuffEntity(FlappyBirdDebuff debuff, double entitySpeed, boolean isMovingLeft,
            long startTime, double posX, double posY, double width, double height, String imageFilePath) {
        super(startTime, posX, posY, width, height, imageFilePath);
        this.debuff = debuff;
        this.entitySpeed = entitySpeed;
        this.isMovingLeft = isMovingLeft;
    }

    /**
     * Creates a new {@code DebuffEntity} with a {@code FlappyBirdDebuff} to
     * hold on to, a speed that it will move left or right, frames to be
     * rendered on each movement, a specified position, and dimension
     *
     * @param debuff the {@code FlappyBirdDebuff} that the {@code DebuffEntity}
     * will contain
     * @param entitySpeed the speed that the {@code DebuffEntity} will move on
     * each update
     * @param isMovingLeft true if the {@code DebuffEntity} is moving left
     * @param startTime the time in the Game Engine that this
     * {@code DebuffEntity} is created
     * @param framesFilePaths an array of strings in order representing the file
     * paths of each frame that will be rendered when the {@code DebuffEntity}
     * is moving in any direction
     * @param posX the left-most position of the {@code DebuffEntity}
     * @param posY the bottom-most position of the {@code DebuffEntity}
     * @param width the width of the {@code DebuffEntity}
     * @param height the height of the {@code DebuffEntity}
     * @throws IllegalArgumentException the entity speed is less than 0
     * @deprecated replaced by using a .gif file to load in the image
     * constructor. This constructor should only be used when a .gif image is
     * absent and multiple images must be used to create an animation.
     */
    public DebuffEntity(FlappyBirdDebuff debuff, double entitySpeed, boolean isMovingLeft,
            long startTime, String[] framesFilePaths, double posX, double posY, double width, double height) {
        super(startTime, framesFilePaths, null, null, null, posX, posY, width, height);
        this.debuff = debuff;
        this.entitySpeed = entitySpeed;
        this.isMovingLeft = isMovingLeft;
    }

    /**
     * Gets the {@code FlappyBirdDebuff} that this {@code DebuffEntity} contains
     *
     * @return the {@code FlappyBirdDebuff} on this {@code DebuffEntity}
     */
    public FlappyBirdDebuff getDebuff() {
        return debuff;
    }

    public double getEntitySpeed() {
        return entitySpeed;
    }

    public void setEntitySpeed(double entitySpeed) {
        this.entitySpeed = entitySpeed;
    }

    public boolean isIsMovingLeft() {
        return isMovingLeft;
    }

    public void setIsMovingLeft(boolean isMovingLeft) {
        this.isMovingLeft = isMovingLeft;
    }

    /**
     * Gets the sequential order of the frames that are being rendered on this
     * {@code ItemEntity}'s movement.
     *
     * @return a {@code Queue} of the frames that the {@code ItemEntity} is
     * using on its right movement
     * @deprecated should only be used if multiple images are loaded on this
     * {@code ItemEntity}
     */
    public Queue<Frame> getFrames() {
        return upFrames;
    }

    /**
     * Creates any children debuffs that are loaded on this {@code DebuffEntity}
     *
     * @return an array of children {@code DebuffEntity}
     */
    public DebuffEntity[] createChildrenDebuffs() {
        switch (debuff) {
            case RAIN_CLOUD_DEBUFF:
                return new DebuffEntity[]{new DebuffEntity(FlappyBirdDebuff.RAIN_DEBUFF,
                    entitySpeed, isMovingLeft, getCurrentTime(), posX + getWidth() / 4, FlappyBirdGame.SCREEN_HEIGHT,
                    getWidth() / 2, FlappyBirdGame.SCREEN_HEIGHT - posY, "assets\\default\\rain.gif")};
            case BIRD_SWARM_DEBUFF:
                DebuffEntity[] birdSwarm = new DebuffEntity[4];
                for (int i = 0; i < birdSwarm.length; i++) {
                    double childPosX = posX + getWidth() * 2 * (i + 1);
                    double childPosY = posY + getHeight() * (i % 2 == 0 ? -1.5 : 1.5) * (i + 1);
                    birdSwarm[i] = new DebuffEntity(FlappyBirdDebuff.BIRD_DEBUFF, entitySpeed,
                            isMovingLeft, getCurrentTime(), childPosX, childPosY, getWidth(), getHeight(), "assets\\default\\bird_debuff.gif");
                }
                return birdSwarm;
            case STORM_CLOUD_DEBUFF:
                DebuffEntity[] lightningBolts = new DebuffEntity[4];
                for (int i = 0; i < lightningBolts.length; i++) {
                    double lightningBoltSpeed = entitySpeed * Math.random() * 2;
                    lightningBolts[i] = new DebuffEntity(FlappyBirdDebuff.LIGHTNING_DEBUFF, lightningBoltSpeed,
                            i < lightningBolts.length / 2, getCurrentTime(),
                            posX + getWidth() / 4 * (i + 1), posY, 60, 60, "assets\\default\\lightning_bolt.png");
                }
                return lightningBolts;
            default:
                break;
        }
        return null;
    }

    /**
     * Renders this {@code DebuffEntity} on the Flappy Bird Game Canvas. If no
     * image or frames are loaded on to this {@code DebuffEntity}, then a purple
     * rectangular graphic will be rendered instead
     *
     * @param gc The {@code GraphicsContext} of the {@code Canvas} that the
     * {@code DebuffEntity} will be rendered on
     */
    @Override
    public void render(GraphicsContext gc) {
        if (upFrames != null) {
            Frame frame = upFrames.poll();
            gc.drawImage(frame.getImage(), posX, posY);
            upFrames.offer(frame);
        } else if (getImage() != null) {
            gc.drawImage(getImage(), posX, posY);
        } else {
            gc.setFill(Color.DARKORCHID);
            gc.fillRect(posX, posY, getWidth(), getHeight());
        }
    }

    /**
     * Updates this {@code DebuffEntity}'s position based on it's speed, whether
     * it is moving left or right, and the type of {@code FlappyBirdDebuff}
     * loaded on to this {@code DebuffEntity}. In many cases, the velocity of
     * this {@code DebuffEntity} will be updated
     */
    @Override
    public void update() {
        if (debuff == FlappyBirdDebuff.LIGHTNING_DEBUFF) {
            velocityX = isMovingLeft ? entitySpeed * -1 : 0;
            posX += velocityX;
            velocityY = entitySpeed * -1;
            posY -= velocityY;
        }
        else {
            velocityX = isMovingLeft ? entitySpeed * -1 : entitySpeed;
            posX += velocityX;
        }
    }

}
