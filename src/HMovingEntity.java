
import java.util.Queue;
import javafx.scene.canvas.GraphicsContext;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * The {@code HMovingEntity} class is a subclass of the {@code MovingEntity}
 * class that is capable of moving horizontally on each update in the Flappy
 * Bird Game Engine
 *
 * @author dylan
 */
public class HMovingEntity extends MovingEntity {

    private double entitySpeed;
    private boolean isMovingLeft;

    /**
     * Creates a new {@code HMovingEntity} with a speed that it will move left
     * or right, a specified position, and dimension
     *
     * @param entitySpeed the speed that the {@code HMovingEntity} will move on
     * each update
     * @param isMovingLeft true if the {@code HMovingEntity} is moving left
     * @param posX the left-most position of the {@code HMovingEntity}
     * @param posY the bottom-most position of the {@code HMovingEntity}
     * @param width the width of the {@code HMovingEntity}
     * @param height the height of the {@code HMovingEntity}
     * @throws IllegalArgumentException if the entity speed is less than 0
     */
    public HMovingEntity(double entitySpeed, boolean isMovingLeft, double posX, double posY, double width, double height) {
        super(0, posX, posY, width, height);
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
        this.entitySpeed = entitySpeed;
        this.isMovingLeft = isMovingLeft;
    }

    /**
     * Creates a new {@code HMovingEntity} with a speed that it will move left
     * or right, a specified position, dimension, and an image to render on this
     * {@code HMovingEntity}
     *
     * @param entitySpeed the speed that the {@code HMovingEntity} will move on
     * each update
     * @param isMovingLeft true if the {@code HMovingEntity} is moving left
     * @param posX the left-most position of the {@code HMovingEntity}
     * @param posY the bottom-most position of the {@code HMovingEntity}
     * @param width the width of the {@code HMovingEntity}
     * @param height the height of the {@code HMovingEntity}
     * @param imageFilePath a string representing the file path to the image
     * that will be rendered on the {@code HMovingEntity}
     * @throws IllegalArgumentException if the entity speed is less than 0
     */
    public HMovingEntity(double entitySpeed, boolean isMovingLeft, double posX, double posY,
            double width, double height, String imageFilePath) {
        super(0, posX, posY, width, height, imageFilePath);
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
        this.entitySpeed = entitySpeed;
        this.isMovingLeft = isMovingLeft;
    }

    /**
     * Creates a new {@code HMovingEntity} with a speed that it will move left
     * or right, frames to be rendered on left and right movement, a specified
     * position, and dimension
     *
     * @param entitySpeed the speed that the {@code HMovingEntity} will move on
     * each update
     * @param isMovingLeft true if the {@code HMovingEntity} is moving left
     * @param rightFramesFilePaths an array of strings in order representing the
     * file paths of each frame that will be rendered when the
     * {@code HMovingEntity} is moving right
     * @param leftFramesFilePaths an array of strings in order representing the
     * file paths of each frame that will be rendered when the
     * {@code HMovingEntity} is moving left
     * @param posX the left-most position of the {@code HMovingEntity}
     * @param posY the bottom-most position of the {@code HMovingEntity}
     * @param width the width of the {@code HMovingEntity}
     * @param height the height of the {@code HMovingEntity}
     * @throws IllegalArgumentException if the entity speed is less than 0
     * @deprecated replaced by using a .gif file to load in the image
     * constructor. This constructor should only be used when a .gif image is
     * absent and multiple images must be used to create an animation.
     */
    public HMovingEntity(double entitySpeed, boolean isMovingLeft, String[] rightFramesFilePaths,
            String[] leftFramesFilePaths, double posX, double posY, double width, double height) {
        super(0, null, rightFramesFilePaths, null, leftFramesFilePaths, posX, posY, width, height);
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
        this.entitySpeed = entitySpeed;
        this.isMovingLeft = isMovingLeft;
    }

    /**
     * Gets how fast this {@code HMovingEntity} is moving
     *
     * @return the speed of the {@code HMovingEntity}
     */
    public double getEntitySpeed() {
        return entitySpeed;
    }

    /**
     * Sets how fast this {@code HMovingEntity} is moving
     *
     * @param entitySpeed the new speed of the {@code HMovingEntity}
     * @throws IllegalArgumentException if the entity speed is less than 0
     */
    public void setEntitySpeed(double entitySpeed) {
        if (entitySpeed < 0) {
            throw new IllegalArgumentException("entity speed must be bigger than 0");
        }
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
     * {@code HMovingEntity}'s right movement.
     *
     * @return a {@code Queue} of the frames that the {@code HMovingEntity} is
     * using on its right movement
     * @deprecated should only be used if multiple images are loaded on this
     * {@code HMovingEntity}
     */
    public Queue<Frame> getRightFrames() {
        return rightFrames;
    }

    /**
     * Gets the sequential order of the frames that are being rendered on this
     * {@code HMovingEntity}'s left movement.
     *
     * @return a {@code Queue} of the frames that the {@code HMovingEntity} is
     * using on its left movement
     * @deprecated should only be used if multiple images are loaded on this
     * {@code HMovingEntity}
     */
    public Queue<Frame> getLeftFrames() {
        return leftFrames;
    }

    /**
     * Renders this {@code HMovingEntity} on the Flappy Bird Game Canvas. If no
     * image or frames are loaded on to this {@code HMovingEntity}, then a gray
     * rectangular graphic will be rendered instead
     *
     * @param gc The {@code GraphicsContext} of the {@code Canvas} that the
     * {@code HMovingEntity} will be rendered on
     */
    @Override
    public void render(GraphicsContext gc) {
        if (leftFrames != null && rightFrames != null) {
            if (isMovingLeft) {
                Frame frame = leftFrames.poll();
                gc.drawImage(frame.getImage(), posX, posY);
                leftFrames.offer(frame);
            } else {
                Frame frame = rightFrames.poll();
                gc.drawImage(frame.getImage(), posX, posY);
                rightFrames.offer(frame);
            }
        } else {
            super.render(gc);
        }
    }

    /**
     * Updates this {@code HMovingEntity}'s X Velocity based on its speed and
     * whether it is moving left or right, and moves it horizontally to a new
     * position
     */
    @Override
    public void update() {
        velocityX = isMovingLeft ? entitySpeed * -1 : entitySpeed;
        posX += velocityX;
    }

}
