
import java.util.ArrayList;
import java.util.Iterator;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dylan
 */
public class SimulationMap {

    private final double screenWidth;
    private final double screenHeight;
    private final Canvas canvas;

    private String assetsFileFolderName;
    private final ArrayList<HMovingEntity> topScenery;
    private final ArrayList<HMovingEntity> bottomScenery;
    private final AnimationTimer animationTimer;

    public SimulationMap(double screenWidth, double screenHeight, String assetsFileFolderName) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        canvas = new Canvas(screenWidth, screenHeight);
        this.assetsFileFolderName = assetsFileFolderName;
        topScenery = new ArrayList<>();
        bottomScenery = new ArrayList<>();
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //generate background
                generateScenery();
                
                //update entity positions
                topScenery.forEach(movingEntity -> {
                    movingEntity.update();
                });
                bottomScenery.forEach(movingEntity -> {
                    movingEntity.update();
                });

                //render
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0, 0, screenWidth, screenHeight);
                gc.setFill(Color.SKYBLUE);
                gc.fillRect(0, 0, screenWidth, screenHeight);
                topScenery.forEach(movingEntity -> {
                    movingEntity.render(gc);
                });
                bottomScenery.forEach(movingEntity -> {
                    movingEntity.render(gc);
                });
            }
        };
        animationTimer.start();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public String getAssetsFileFolderName() {
        return assetsFileFolderName;
    }

    public void setAssetsFileFolderName(String assetsFileFolderName) {
        this.assetsFileFolderName = assetsFileFolderName;
    }

    private void generateScenery() {
        //remove top scenery off the canvas
        for (Iterator<HMovingEntity> iterator = topScenery.iterator(); iterator.hasNext();) {
            HMovingEntity next = iterator.next();
            if (next.getPosX() + next.getWidth() <= 0) {
                iterator.remove();
            }
        }
        //remove bottom scenery off the canvas
        for (Iterator<HMovingEntity> iterator = bottomScenery.iterator(); iterator.hasNext();) {
            HMovingEntity next = iterator.next();
            if (next.getPosX() + next.getWidth() <= 0) {
                iterator.remove();
            }
        }
        if (topScenery.isEmpty()) {
            //generate up to five top scenic assets
            int numOfAssets = (int) (Math.random() * 6);
            double assetSpeed = Math.random() * 7 + 1;
            double offset = 0;
            for (int i = 0; i < numOfAssets; i++) {
                double width = Math.random() * screenWidth / 4 + 100;
                double height = width - 60;
                topScenery.add(new HMovingEntity(assetSpeed, true, screenWidth + offset, height, width, height, "assets\\default\\cloud.png"));
                assetSpeed++;
                offset = width + 50;
            }
        }
        if (bottomScenery.isEmpty()) {
            //generate up to seven bottom scenic assets
            int numOfAssets = (int) (Math.random() * 8);
            double assetSpeed = Math.random() * 3 + 1;
            double offset = 0;
            for (int i = 0; i < numOfAssets; i++) {
                double width = Math.random() * screenWidth / 4 + 100;
                double height = width / 2;
                bottomScenery.add(new HMovingEntity(assetSpeed, true, screenWidth + offset, screenHeight, width, height, "assets\\default\\hill.png"));
                offset = width + 50;
            }
        }
    }

}
