
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * The {@code Entity} class is a graphical representation of a Flappy Bird Game
 * object that can be rendered on to a {@code Canvas} provided by a Flappy Bird
 * Game Engine
 *
 * @author dylan
 */
public class Entity {

    protected double posX;
    protected double posY;
    private double width;
    private double height;

    private Frame image;

    /**
     * Creates a new {@code Entity} with a specified position and dimension
     *
     * @param posX the left-most position of the {@code Entity}
     * @param posY the bottom-most position of the {@code Entity}
     * @param width the width of the {@code Entity}
     * @param height the height of the {@code Entity}
     * @throws IllegalArgumentException if the width or height of the
     * {@code Entity} is less than 0
     */
    public Entity(double posX, double posY, double width, double height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Entity width and height must be bigger than 0");
        }
        this.posX = posX;
        this.posY = posY - height;
        this.width = width;
        this.height = height;
    }

    /**
     * Creates a new {@code Entity} with a specified position, dimension, and an
     * image to render on this {@code Entity}
     *
     * @param posX the left-most position of the {@code Entity}
     * @param posY the bottom-most position of the {@code Entity}
     * @param width the width of the {@code Entity}
     * @param height the height of the {@code Entity}
     * @param imageFilePath a string representing the file path to the image
     * that will be rendered on the {@code Entity}
     * @throws IllegalArgumentException if the width or height of the
     * {@code Entity} is less than 0
     */
    public Entity(double posX, double posY, double width, double height, String imageFilePath) {
        this(posX, posY, width, height);
        try {
            image = new Frame(imageFilePath);
        } catch (FileNotFoundException e) {
            System.err.println("Could not load Entity Image File :: " + imageFilePath);
        }
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (image != null) {
            try {
                image.resize(width, height);
            } catch (FileNotFoundException e) {
                System.err.println("Could not resize Entity image width");
                image = null;
            }
        }
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if (image != null) {
            try {
                image.resize(width, height);
            } catch (FileNotFoundException e) {
                System.err.println("Could not resize Entity image height");
                image = null;
            }
        }
        this.height = height;
    }

    public Image getImage() {
        if (image != null) {
            return image.getImage();
        }
        return null;
    }
    
    public String getImageFilePath(){
        if(image != null){
            return image.getImageFilePath();
        }
        return null;
    }

    public void setImage(String imageFilePath) {
        try {
            image.setImage(imageFilePath);
        } catch (FileNotFoundException e) {
            System.err.println("Could not load Entity Image File :: " + imageFilePath);
            image = null;
        }
    }

    /**
     * Gets the area that is consumed by this {@code Entity}
     *
     * @return a {@code Rectangle2D} representing the boundary of this
     * {@code Entity}
     */
    public Rectangle2D getBoundary() {
        return new Rectangle2D(posX, posY, width, height);
    }

    /**
     * Checks for an intersection between this {@code Entity} and another
     * {@code Entity} based on their boundaries
     *
     * @param e the other {@code Entity} to be checked for an intersection
     * @return true if the entities intersect each other
     */
    public boolean intersects(Entity e) {
        return getBoundary().intersects(e.getBoundary());
    }

    /**
     * Renders this {@code Entity} on the Flappy Bird Game Canvas. If no image
     * is loaded on to this {@code Entity}, then a gray rectangular graphic will
     * be rendered instead
     *
     * @param gc The {@code GraphicsContext} of the {@code Canvas} that the
     * {@code Entity} will be rendered on
     */
    public void render(GraphicsContext gc) {
        if (image == null) {
            gc.setFill(Color.GRAY);
            gc.fillRect(posX, posY, width, height);
        } else {
            gc.drawImage(image.getImage(), posX, posY);
        }
    }

    /**
     * The {@code Frame} class is designed to help organize Flappy Bird Image
     * assets and to assist in resizing and loading images on to an
     * {@code Entity}
     */
    protected class Frame {

        private Image image;
        private String imageFilePath;

        public Frame(String imageFilePath) throws FileNotFoundException {
            image = new Image(new FileInputStream(imageFilePath), width, height, false, true);
            this.imageFilePath = imageFilePath;
        }

        public Image getImage() {
            return image;
        }

        public void setImage(String imageFilePath) throws FileNotFoundException {
            image = new Image(new FileInputStream(imageFilePath), width, height, false, true);
            this.imageFilePath = imageFilePath;
        }

        public String getImageFilePath() {
            return imageFilePath;
        }

        public void resize(double width, double height) throws FileNotFoundException {
            image = new Image(new FileInputStream(imageFilePath), width, height, false, true);
        }
    }

}
