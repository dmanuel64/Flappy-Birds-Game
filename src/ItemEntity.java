
import java.util.Queue;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * The {@code ItemEntity} class is a special type of {@code MovingEntity} that
 * holds a {@code FlappyBirdItem} which offers an advantage to a
 * {@code FlappyBird} in the Flappy Bird Game Engine. The {@code ItemEntity}
 * moves based on its item type and does not necessarily move exclusively up or
 * down
 *
 * @author dylan
 */
public class ItemEntity extends MovingEntity {

    private final FlappyBirdItem item;
    private double entitySpeed;
    private boolean isMovingLeft;

    /**
     * Creates a new {@code ItemEntity} with a {@code FlappyBirdItem} to hold on
     * to, a speed that it will move left or right, a specified position and
     * dimension
     *
     * @param item the {@code FlappyBirdItem} that the {@code ItemEntity} will
     * contain
     * @param entitySpeed the speed that the {@code ItemEntity} will move on
     * each update
     * @param isMovingLeft true if the {@code ItemEntity} is moving left
     * @param startTime the time in the Game Engine that this {@code ItemEntity}
     * is created
     * @param posX the left-most position of the {@code ItemEntity}
     * @param posY the bottom-most position of the {@code ItemEntity}
     * @param width the width of the {@code ItemEntity}
     * @param height the height of the {@code ItemEntity}
     * @throws IllegalArgumentException the entity speed is less than 0
     */
    public ItemEntity(FlappyBirdItem item, double entitySpeed, boolean isMovingLeft,
            long startTime, double posX, double posY, double width, double height) {
        super(startTime, posX, posY, width, height);
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
        this.item = item;
        this.entitySpeed = entitySpeed;
        this.isMovingLeft = isMovingLeft;
    }

    /**
     * Creates a new {@code ItemEntity} with a {@code FlappyBirdItem} to hold on
     * to, a speed that it will move left or right, a specified position,
     * dimension, and an image to render on this {@code ItemEntity}
     *
     * @param item the {@code FlappyBirdItem} that the {@code ItemEntity} will
     * contain
     * @param entitySpeed the speed that the {@code ItemEntity} will move on
     * each update
     * @param isMovingLeft true if the {@code ItemEntity} is moving left
     * @param startTime the time in the Game Engine that this {@code ItemEntity}
     * is created
     * @param posX the left-most position of the {@code ItemEntity}
     * @param posY the bottom-most position of the {@code ItemEntity}
     * @param width the width of the {@code ItemEntity}
     * @param height the height of the {@code ItemEntity}
     * @param imageFilePath a string representing the file path to the image
     * that will be rendered on the {@code ItemEntity}
     * @throws IllegalArgumentException the entity speed is less than 0
     */
    public ItemEntity(FlappyBirdItem item, double entitySpeed, boolean isMovingLeft,
            long startTime, double posX, double posY, double width, double height, String imageFilePath) {
        super(startTime, posX, posY, width, height, imageFilePath);
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
        this.item = item;
        this.entitySpeed = entitySpeed;
        this.isMovingLeft = isMovingLeft;
    }

    /**
     * Creates a new {@code ItemEntity} with a {@code FlappyBirdItem} to hold on
     * to, a speed that it will move left or right, frames to be rendered on
     * each movement, a specified position, and dimension
     *
     * @param item the {@code FlappyBirdItem} that the {@code ItemEntity} will
     * contain
     * @param entitySpeed the speed that the {@code ItemEntity} will move on
     * each update
     * @param isMovingLeft true if the {@code ItemEntity} is moving left
     * @param startTime the time in the Game Engine that this {@code ItemEntity}
     * is created
     * @param framesFilePaths an array of strings in order representing the file
     * paths of each frame that will be rendered when the {@code ItemEntity} is
     * moving in any direction
     * @param posX the left-most position of the {@code ItemEntity}
     * @param posY the bottom-most position of the {@code ItemEntity}
     * @param width the width of the {@code ItemEntity}
     * @param height the height of the {@code ItemEntity}
     * @throws IllegalArgumentException the entity speed is less than 0
     * @deprecated replaced by using a .gif file to load in the image
     * constructor. This constructor should only be used when a .gif image is
     * absent and multiple images must be used to create an animation.
     */
    public ItemEntity(FlappyBirdItem item, double entitySpeed, boolean isMovingLeft,
            long startTime, String[] framesFilePaths, double posX, double posY, double width, double height) {
        super(startTime, framesFilePaths, null, null, null, posX, posY, width, height);
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
        this.item = item;
        this.entitySpeed = entitySpeed;
        this.isMovingLeft = isMovingLeft;
    }

    /**
     * Gets the {@code FlappyBirdItem} that this {@code ItemEntity} contains
     *
     * @return the {@code FlappyBirdItem} on this {@code ItemEntity}
     */
    public FlappyBirdItem getItem() {
        return item;
    }

    public double getEntitySpeed() {
        return entitySpeed;
    }

    public void setEntitySpeed(double entitySpeed) {
        this.entitySpeed = entitySpeed;
    }

    public boolean isMovingLeft() {
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
     * Renders this {@code ItemEntity} on the Flappy Bird Game Canvas. If no
     * image or frames are loaded on to this {@code ItemEntity}, then an orange
     * rectangular graphic will be rendered instead
     *
     * @param gc The {@code GraphicsContext} of the {@code Canvas} that the
     * {@code ItemEntity} will be rendered on
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
            gc.setFill(Color.ORANGE);
            gc.fillRect(posX, posY, getWidth(), getHeight());
        }
    }

    /**
     * Updates this {@code ItemEntity}'s position based on it's speed, whether
     * it is moving left or right, and the type of {@code FlappyBirdItem} loaded
     * on to this {@code ItemEntity}. In many cases, the velocity of the
     * {@code ItemEntity} will not be updated due to the parametric function of
     * it's update
     */
    @Override
    public void update() {
        switch (item) {
            case COIN:
                entitySpeed += .25;
                velocityX = isMovingLeft ? entitySpeed * -1 : entitySpeed;
                posX += velocityX;
                break;
            case WIND:
                entitySpeed += .125;
                velocityX = isMovingLeft ? entitySpeed * -1 : entitySpeed;
                posX += velocityX;
                break;
            default:
                posX = getWidth() * Math.cos(getCurrentTime() / 1e8) + startXPos - entitySpeed * getElapsedTime() / 1.2e7;
                posY = getHeight() * Math.sin(getCurrentTime() / 1e8) + startYPos;
                break;
        }
    }

}
