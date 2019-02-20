
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * The {@code FlappyBird} class is a specialized {@code VMovingEntity} that
 * accelerates with gravity on each game engine update. The {@code FlappyBird}
 * class is meant to be used for user inputs in the Flappy Bird Game
 *
 * @author dylan
 */
public class FlappyBird extends VMovingEntity {

    private double gravityAcceleration;

    /**
     * Creates a new {@code FlappyBird} with an acceleration that it will move
     * falling down, the speed it will move flying up, a specified position, and
     * dimension
     *
     * @param gravityAcceleration the acceleration the {@code FlappyBird} will
     * fall
     * @param flyingSpeed the speed the {@code FlappyBird} will fly
     * @param posX the left-most position of the {@code FlappyBird}
     * @param posY the bottom-most position of the {@code FlappyBird}
     * @param width the width of the {@code FlappyBird}
     * @param height the height of the {@code FlappyBird}
     * @throws IllegalArgumentException if the {@code FlappyBird} flying speed
     * or gravity acceleration is less than 0
     */
    public FlappyBird(double gravityAcceleration, double flyingSpeed, double posX,
            double posY, double width, double height) {
        super(flyingSpeed, true, posX, posY, width, height);
        if (gravityAcceleration < 0 || flyingSpeed < 0) {
            throw new IllegalArgumentException("FlappyBird gravity acceleration and speed"
                    + "must be bigger than 0");
        }
        this.gravityAcceleration = gravityAcceleration;
    }

    /**
     * Creates a new {@code FlappyBird} with an acceleration that it will move
     * falling down, the speed it will move flying up, a specified position,
     * dimension, and an image to render on this {@code FlappyBird}
     *
     * @param gravityAcceleration the acceleration the {@code FlappyBird} will
     * fall
     * @param flyingSpeed the speed the {@code FlappyBird} will fly
     * @param posX the left-most position of the {@code FlappyBird}
     * @param posY the bottom-most position of the {@code FlappyBird}
     * @param width the width of the {@code FlappyBird}
     * @param height the height of the {@code FlappyBird}
     * @param imageFilePath a string representing the file path to the image
     * that will be rendered on the {@code FlappyBird}
     * @throws IllegalArgumentException if the {@code FlappyBird} flying speed
     * or gravity acceleration is less than 0
     */
    public FlappyBird(double gravityAcceleration, double flyingSpeed, double posX,
            double posY, double width, double height, String imageFilePath) {
        super(flyingSpeed, true, posX, posY, width, height, imageFilePath);
        if (gravityAcceleration < 0 || flyingSpeed < 0) {
            throw new IllegalArgumentException("FlappyBird gravity acceleration and speed"
                    + "must be bigger than 0");
        }
        this.gravityAcceleration = gravityAcceleration;
    }

    /**
     * Creates a new {@code FlappyBird} with an acceleration that it will move
     * falling down, the speed it will move flying up, frames to be rendered on
     * flying and falling movement, a specified position, and dimension
     *
     * @param gravityAcceleration the acceleration the {@code FlappyBird} will
     * fall
     * @param flyingSpeed the speed the {@code FlappyBird} will fly
     * @param flyingFramesFilePaths an array of strings in order representing
     * the file paths of each frame that will be rendered when the
     * {@code FlappyBird} is flying
     * @param fallingFramesFilePaths an array of strings in order representing
     * the file paths of each frame that will be rendered when the
     * {@code FlappyBird} is falling
     * @param posX the left-most position of the {@code FlappyBird}
     * @param posY the bottom-most position of the {@code FlappyBird}
     * @param width the width of the {@code FlappyBird}
     * @param height the height of the {@code FlappyBird}
     * @throws IllegalArgumentException if the {@code FlappyBird} flying speed
     * or gravity acceleration is less than 0
     * @deprecated replaced by using a .gif file to load in the image
     * constructor. This constructor should only be used when a .gif image is
     * absent and multiple images must be used to create an animation.
     */
    public FlappyBird(double gravityAcceleration, double flyingSpeed, String[] flyingFramesFilePaths,
            String[] fallingFramesFilePaths, double posX, double posY, double width, double height) {
        super(flyingSpeed, true, flyingFramesFilePaths, fallingFramesFilePaths, posX, posY, width, height);
        if (gravityAcceleration < 0 || flyingSpeed < 0) {
            throw new IllegalArgumentException("FlappyBird gravity acceleration and speed"
                    + "must be bigger than 0");
        }
        this.gravityAcceleration = gravityAcceleration;
    }

    public double getGravityAcceleration() {
        return gravityAcceleration;
    }

    public void setGravityAcceleration(double gravityAcceleration) {
        this.gravityAcceleration = gravityAcceleration;
    }

    /**
     * Renders this {@code FlappyBird} on the Flappy Bird Game Canvas. If no
     * image or frames are loaded on to this {@code FlappyBird}, then a red
     * rectangular graphic will be rendered instead
     *
     * @param gc The {@code GraphicsContext} of the {@code Canvas} that the
     * {@code FlappyBird} will be rendered on
     */
    @Override
    public void render(GraphicsContext gc) {
        if (upFrames != null && downFrames != null) {
            if (isMovingDown()) {
                Frame frame = downFrames.poll();
                gc.drawImage(frame.getImage(), posX, posY);
                downFrames.offer(frame);
            } else {
                Frame frame = upFrames.poll();
                gc.drawImage(frame.getImage(), posX, posY);
                upFrames.offer(frame);
            }
        } else if (getImage() != null) {
            gc.drawImage(getImage(), posX, posY);
        } else {
            gc.strokeText("Velocity X :: " + velocityX + " Velocity Y :: " + velocityY,
                    posX - getWidth(), posY - 10, getWidth() * 3);
            gc.setFill(Color.RED);
            gc.fillRect(posX, posY, getWidth(), getHeight());
        }
    }

    /**
     * Updates this {@code FlappyBird}'s Y Velocity based on its flying speed
     * and whether it is flying or falling, and moves it vertically to a new
     * position. If the {@code FlappyBird} is falling, then the Y Velocity will
     * be updated with gravity
     */
    @Override
    public void update() {
        if (isMovingDown() == false) {
            velocityY = getEntitySpeed() - gravityAcceleration;
        } else {
            velocityY += gravityAcceleration * -1;
        }
        posY -= velocityY;
    }

}
