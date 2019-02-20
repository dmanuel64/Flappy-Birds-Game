
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * The {@code MovingEntity} class is a subclass of the {@code Entity} class that
 * has the functionality of moving around on a Flappy Bird Game Canvas
 *
 * @author dylan
 */
public abstract class MovingEntity extends Entity {

    protected final double startXPos;
    protected final double startYPos;
    private final long startTime;
    private long currentTime;

    protected double velocityX;
    protected double velocityY;

    protected Queue<Frame> upFrames;
    protected Queue<Frame> rightFrames;
    protected Queue<Frame> downFrames;
    protected Queue<Frame> leftFrames;

    /**
     * Creates a new {@code MovingEntity} with the time in the Game Engine that
     * it was created, a specified position, and dimension
     *
     * @param startTime the time in the Game Engine that this
     * {@code MovingEntity} is created
     * @param posX the left-most position of the {@code MovingEntity}
     * @param posY the bottom-most position of the {@code MovingEntity}
     * @param width the width of the {@code MovingEntity}
     * @param height the height of the {@code MovingEntity}
     */
    public MovingEntity(long startTime, double posX, double posY, double width, double height) {
        super(posX, posY, width, height);
        startXPos = posX;
        startYPos = posY;
        this.startTime = startTime;
    }

    /**
     * Creates a new {@code MovingEntity} with the time in the Game Engine that
     * it was created, a specified position, dimension, and an image to render
     * on this {@code MovingEntity}
     *
     * @param startTime the time in the Game Engine that this
     * {@code MovingEntity} is created
     * @param posX the left-most position of the {@code MovingEntity}
     * @param posY the bottom-most position of the {@code MovingEntity}
     * @param width the width of the {@code MovingEntity}
     * @param height the height of the {@code MovingEntity}
     * @param imageFilePath a string representing the file path to the image
     * that will be rendered on the {@code MovingEntity}
     */
    public MovingEntity(long startTime, double posX, double posY, double width, double height, String imageFilePath) {
        super(posX, posY, width, height, imageFilePath);
        startXPos = posX;
        startYPos = posY;
        this.startTime = startTime;
    }

    /**
     * Creates a new {@code MovingEntity} with the time in the Game Engine that
     * it was created, frames to be rendered on movement, a specified position,
     * and dimension
     *
     * @param startTime the time in the Game Engine that this
     * {@code MovingEntity} is created
     * @param upFramesFilePaths an array of strings in order representing the
     * file paths of each frame that will be rendered when the
     * {@code MovingEntity} is moving up
     * @param rightFramesFilePaths an array of strings in order representing the
     * file paths of each frame that will be rendered when the
     * {@code MovingEntity} is moving right
     * @param downFramesFilePaths an array of strings in order representing the
     * file paths of each frame that will be rendered when the
     * {@code MovingEntity} is moving down
     * @param leftFramesFilePaths an array of strings in order representing the
     * file paths of each frame that will be rendered when the
     * {@code MovingEntity} is moving left
     * @param posX the left-most position of the {@code MovingEntity}
     * @param posY the bottom-most position of the {@code MovingEntity}
     * @param width the width of the {@code MovingEntity}
     * @param height the height of the {@code MovingEntity}
     * @deprecated replaced by using a .gif file to load in the image
     * constructor. This constructor should only be used when a .gif image is
     * absent and multiple images must be used to create an animation.
     */
    public MovingEntity(long startTime, String[] upFramesFilePaths, String[] rightFramesFilePaths, String[] downFramesFilePaths,
            String[] leftFramesFilePaths, double posX, double posY, double width, double height) {
        this(startTime, posX, posY, width, height);
        if (upFramesFilePaths != null && upFramesFilePaths.length > 0) {
            upFrames = new LinkedList<>();
            for (String imageFilePath : upFramesFilePaths) {
                try {
                    upFrames.offer(new Frame(imageFilePath));
                } catch (FileNotFoundException e) {
                    System.err.println("Could not load frame :: " + imageFilePath);
                    upFrames = null;
                    break;
                }
            }
        }
        if (rightFramesFilePaths != null && rightFramesFilePaths.length > 0) {
            rightFrames = new LinkedList<>();
            for (String imageFilePath : rightFramesFilePaths) {
                try {
                    rightFrames.offer(new Frame(imageFilePath));
                } catch (FileNotFoundException e) {
                    System.err.println("Could not load frame :: " + imageFilePath);
                    rightFrames = null;
                    break;
                }
            }
        }
        if (downFramesFilePaths != null && downFramesFilePaths.length > 0) {
            downFrames = new LinkedList<>();
            for (String imageFilePath : downFramesFilePaths) {
                try {
                    downFrames.offer(new Frame(imageFilePath));
                } catch (FileNotFoundException e) {
                    System.err.println("Could not load frame :: " + imageFilePath);
                    downFrames = null;
                    break;
                }
            }
        }
        if (leftFramesFilePaths != null && leftFramesFilePaths.length > 0) {
            leftFrames = new LinkedList<>();
            for (String imageFilePath : leftFramesFilePaths) {
                try {
                    leftFrames.offer(new Frame(imageFilePath));
                } catch (FileNotFoundException e) {
                    System.err.println("Could not load frame :: " + imageFilePath);
                    leftFrames = null;
                    break;
                }
            }
        }
    }

    @Override
    public void setWidth(double width) {
        if (upFrames == null && rightFrames == null && downFrames == null && leftFrames == null) {
            super.setWidth(width);
        } else {
            double height = getHeight();
            if (upFrames != null) {
                for (Frame frame : upFrames) {
                    try {
                        frame.resize(width, height);
                    } catch (FileNotFoundException e) {
                        System.err.println("Could not resize width of MovingEntity Frame :: "
                                + frame.getImageFilePath());
                        upFrames = null;
                        break;
                    }
                }
            }
            if (rightFrames != null) {
                for (Frame frame : rightFrames) {
                    try {
                        frame.resize(width, height);
                    } catch (FileNotFoundException e) {
                        System.err.println("Could not resize width of MovingEntity Frame :: "
                                + frame.getImageFilePath());
                        rightFrames = null;
                        break;
                    }
                }
            }
            if (downFrames != null) {
                for (Frame frame : downFrames) {
                    try {
                        frame.resize(width, height);
                    } catch (FileNotFoundException e) {
                        System.err.println("Could not resize width of MovingEntity Frame :: "
                                + frame.getImageFilePath());
                        downFrames = null;
                        break;
                    }
                }
            }
            if (leftFrames != null) {
                for (Frame frame : leftFrames) {
                    try {
                        frame.resize(width, height);
                    } catch (FileNotFoundException e) {
                        System.err.println("Could not resize width of MovingEntity Frame :: "
                                + frame.getImageFilePath());
                        leftFrames = null;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void setHeight(double height) {
        if (upFrames == null && rightFrames == null && downFrames == null && leftFrames == null) {
            super.setHeight(height);
        } else {
            double width = getWidth();
            if (upFrames != null) {
                for (Frame frame : upFrames) {
                    try {
                        frame.resize(width, height);
                    } catch (FileNotFoundException e) {
                        System.err.println("Could not resize height of MovingEntity Frame :: "
                                + frame.getImageFilePath());
                        upFrames = null;
                        break;
                    }
                }
            }
            if (rightFrames != null) {
                for (Frame frame : rightFrames) {
                    try {
                        frame.resize(width, height);
                    } catch (FileNotFoundException e) {
                        System.err.println("Could not resize height of MovingEntity Frame :: "
                                + frame.getImageFilePath());
                        rightFrames = null;
                        break;
                    }
                }
            }
            if (downFrames != null) {
                for (Frame frame : downFrames) {
                    try {
                        frame.resize(width, height);
                    } catch (FileNotFoundException e) {
                        System.err.println("Could not resize height of MovingEntity Frame :: "
                                + frame.getImageFilePath());
                        downFrames = null;
                        break;
                    }
                }
            }
            if (leftFrames != null) {
                for (Frame frame : leftFrames) {
                    try {
                        frame.resize(width, height);
                    } catch (FileNotFoundException e) {
                        System.err.println("Could not resize height of MovingEntity Frame :: "
                                + frame.getImageFilePath());
                        leftFrames = null;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Gets the time elapsed from the staring time this {@code MovingEntity} was
     * created and the current time this {@code MovingEntity} is at
     *
     * @return the time elapsed in this {@code MovingEntity}
     */
    public long getElapsedTime() {
        return currentTime - startTime;
    }

    /**
     * Gets the current time that this {@code MovingEntity} is at in the Flappy
     * Bird Game Engine
     *
     * @return the current time the {@code MovingEntity} is at
     */
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * Sets the current time that this {@code MovingEntity} is at in the Flappy
     * Bird Game Engine
     *
     * @param currentTime the current time of the Flappy Bird Game Engine
     */
    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    /**
     * Updates this {@code MovingEntity}'s velocity and moves the
     * {@code MovingEntity} to another location based on its velocity
     */
    public abstract void update();

}
