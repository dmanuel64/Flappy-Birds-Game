
import java.util.Queue;
import javafx.scene.canvas.GraphicsContext;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * The {@code VMovingEntity} class is a subclass of the {@code MovingEntity}
 * class that is capable of moving vertically on each update in the Flappy Bird
 * Game Engine
 *
 * @author dylan
 */
public class VMovingEntity extends MovingEntity {

    private double entitySpeed;
    private boolean isMovingDown;

    /**
     * Creates a new {@code VMovingEntity} with a speed that it will move up or
     * down, a specified position, and dimension
     *
     * @param entitySpeed the speed that the {@code VMovingEntity} will move on
     * each update
     * @param isMovingDown true if the {@code VMovingEntity} is moving down
     * @param posX the left-most position of the {@code VMovingEntity}
     * @param posY the bottom-most position of the {@code VMovingEntity}
     * @param width the width of the {@code VMovingEntity}
     * @param height the height of the {@code VMovingEntity}
     * @throws IllegalArgumentException if the entity speed is less than 0
     */
    public VMovingEntity(double entitySpeed, boolean isMovingDown, double posX, double posY, double width, double height) {
        super(0, posX, posY, width, height);
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
        this.entitySpeed = entitySpeed;
        this.isMovingDown = isMovingDown;
    }

    /**
     * Creates a new {@code VMovingEntity} with a speed that it will move up or
     * down, a specified position, dimension, and an image to render on this
     * {@code VMovingEntity}
     *
     * @param entitySpeed the speed that the {@code VMovingEntity} will move on
     * each update
     * @param isMovingDown true if the {@code VMovingEntity} is moving down
     * @param posX the left-most position of the {@code VMovingEntity}
     * @param posY the bottom-most position of the {@code VMovingEntity}
     * @param width the width of the {@code VMovingEntity}
     * @param height the height of the {@code VMovingEntity}
     * @param imageFilePath a string representing the file path to the image
     * that will be rendered on the {@code VMovingEntity}
     * @throws IllegalArgumentException if the entity speed is less than 0
     */
    public VMovingEntity(double entitySpeed, boolean isMovingDown, double posX, double posY,
            double width, double height, String imageFilePath) {
        super(0, posX, posY, width, height, imageFilePath);
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
        this.entitySpeed = entitySpeed;
        this.isMovingDown = isMovingDown;
    }

    /**
     * Creates a new {@code VMovingEntity} with a speed that it will move up or
     * down, frames to be rendered moving up and down movement, a specified
     * position, and dimension
     *
     * @param entitySpeed the speed that the {@code VMovingEntity} will move on
     * each update
     * @param isMovingDown true if the {@code VMovingEntity} is moving down
     * @param upFramesFilePaths an array of strings in order representing the
     * file paths of each frame that will be rendered when the
     * {@code VMovingEntity} is moving up
     * @param downFramesFilePaths an array of strings in order representing the
     * file paths of each frame that will be rendered when the
     * {@code VMovingEntity} is moving down
     * @param posX the left-most position of the {@code VMovingEntity}
     * @param posY the bottom-most position of the {@code VMovingEntity}
     * @param width the width of the {@code VMovingEntity}
     * @param height the height of the {@code VMovingEntity}
     * @throws IllegalArgumentException if the entity speed is less than 0
     * @deprecated replaced by using a .gif file to load in the image
     * constructor. This constructor should only be used when a .gif image is
     * absent and multiple images must be used to create an animation.
     */
    public VMovingEntity(double entitySpeed, boolean isMovingDown, String[] upFramesFilePaths,
            String[] downFramesFilePaths, double posX, double posY, double width, double height) {
        super(0, upFramesFilePaths, null, downFramesFilePaths, null, posX, posY, width, height);
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
        this.entitySpeed = entitySpeed;
        this.isMovingDown = isMovingDown;
    }

    /**
     * Gets how fast this {@code VMovingEntity} is moving
     *
     * @return the speed of the {@code VMovingEntity}
     */
    public double getEntitySpeed() {
        return entitySpeed;
    }

    /**
     * Sets how fast this {@code VMovingEntity} is moving
     *
     * @param entitySpeed the new speed of the {@code VMovingEntity}
     * @throws IllegalArgumentException if the entity speed is less than 0
     */
    public void setEntitySpeed(double entitySpeed) {
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
        this.entitySpeed = entitySpeed;
    }

    public boolean isMovingDown() {
        return isMovingDown;
    }

    public void setIsMovingDown(boolean isMovingDown) {
        this.isMovingDown = isMovingDown;
    }

    /**
     * Gets the sequential order of the frames that are being rendered on this
     * {@code VMovingEntity}'s up movement.
     *
     * @return a {@code Queue} of the frames that the {@code VMovingEntity} is
     * using on its up movement
     * @deprecated should only be used if multiple images are loaded on this
     * {@code VMovingEntity}
     */
    public Queue<Frame> getUpFrames() {
        return upFrames;
    }

    /**
     * Gets the sequential order of the frames that are being rendered on this
     * {@code VMovingEntity}'s down movement.
     *
     * @return a {@code Queue} of the frames that the {@code VMovingEntity} is
     * using on its down movement
     * @deprecated should only be used if multiple images are loaded on this
     * {@code VMovingEntity}
     */
    public Queue<Frame> getDownFrames() {
        return downFrames;
    }

    /**
     * Renders this {@code VMovingEntity} on the Flappy Bird Game Canvas. If no
     * image or frames are loaded on to this {@code VMovingEntity}, then a gray
     * rectangular graphic will be rendered instead
     *
     * @param gc The {@code GraphicsContext} of the {@code Canvas} that the
     * {@code VMovingEntity} will be rendered on
     */
    @Override
    public void render(GraphicsContext gc) {
        if (upFrames != null && downFrames != null) {
            if (isMovingDown) {
                Frame frame = downFrames.poll();
                gc.drawImage(frame.getImage(), posX, posY);
                downFrames.offer(frame);
            } else {
                Frame frame = upFrames.poll();
                gc.drawImage(frame.getImage(), posX, posY);
                upFrames.offer(frame);
            }
        } else {
            super.render(gc);
        }
    }

    /**
     * Updates this {@code VMovingEntity}'s Y Velocity based on its speed and
     * whether it is moving up or down, and moves it vertically to a new
     * position
     */
    @Override
    public void update() {
        velocityY = isMovingDown ? entitySpeed * -1 : entitySpeed;
        posY -= velocityY;
    }

}
