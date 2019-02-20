
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dylan
 */
public class FlappyBirdMap {

    private final String mapFile;
    private final AnimationTimer gameEngine;
    private final double screenWidth = FlappyBirdGame.SCREEN_WIDTH;
    private final double screenHeight = FlappyBirdGame.SCREEN_HEIGHT;
    private int score;

    private final Stage primaryStage;
    private final Scene mainMenuScene;

    private final BorderPane root;
    private final Scene mapScene;
    private final Canvas canvas;
    private final Label scoreLabel;

    private FlappyBird flappyBird;
    private boolean flappyBirdIsFlying;
    private int gapLength;

    public FlappyBirdMap(String flappyBirdSkin, String flappyBirdTrail, Stage primaryStage) {
        mapFile = "";
        if (flappyBirdSkin.isEmpty() && flappyBirdTrail.isEmpty()) {
            flappyBird = new FlappyBird(0.5, 7, screenHeight / 3, screenHeight / 2, 50, 50);
        }
        gapLength = (int) flappyBird.getHeight() * 3;

        root = new BorderPane();
        mapScene = new Scene(root, screenWidth, screenHeight);
        mapScene.setOnKeyPressed((event) -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.W) {
                flappyBirdIsFlying = true;
            } else if (code == KeyCode.ESCAPE) {
                pauseGame();
            }
        });
        mapScene.setOnKeyReleased((event) -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.W) {
                flappyBirdIsFlying = false;
            }
        });
        canvas = new Canvas(screenWidth, screenHeight);
        root.getChildren().add(canvas);
        scoreLabel = new Label("Score :: 0");
        scoreLabel.setFont(new Font("Bodoni MT Black", 30));
        HBox scoreBox = new HBox(scoreLabel);
        scoreBox.setAlignment(Pos.CENTER_RIGHT);
        root.setTop(scoreBox);

        gameEngine = new EndlessGameEngine();

        this.primaryStage = primaryStage;
        this.mainMenuScene = primaryStage.getScene();
        this.primaryStage.setScene(mapScene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

    }

    private void pauseGame() {
        gameEngine.stop();
        VBox menuBox = new VBox(50);
        menuBox.setAlignment(Pos.CENTER);
        root.setCenter(menuBox);
        Button resumeButton = new Button("Resume");
        resumeButton.setOnAction((event) -> {
            root.setCenter(null);
            gameEngine.start();
        });
        menuBox.getChildren().add(resumeButton);
        Button optionsButton = new Button("Options");
        menuBox.getChildren().add(optionsButton);
        Button exitButton = new Button("Exit to Menu");
        exitButton.setOnAction((event) -> {
            primaryStage.setScene(mainMenuScene);
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        });
        menuBox.getChildren().add(exitButton);
    }

    public String getMapFile() {
        return mapFile;
    }

    public Scene getMapScene() {
        return mapScene;
    }

    public FlappyBird getFlappyBird() {
        return flappyBird;
    }

    public void startMap() {
        gameEngine.start();
    }

    private class EndlessGameEngine extends AnimationTimer {

        private HMovingEntity topPipe;
        private HMovingEntity bottomPipe;
        private ItemEntity item;
        private final ArrayList<HMovingEntity> clouds;
        private final ArrayList<HMovingEntity> hills;

        public EndlessGameEngine() {
            this.topPipe = new HMovingEntity(5, true, 60 * -1, 0, 100, 0);
            this.bottomPipe = new HMovingEntity(5, true, 60 * -1, 0, 100, 0);
            this.clouds = new ArrayList<>();
            this.hills = new ArrayList<>();
        }

        private void generateScenery() {
            //remove any clouds off the canvas
            for (Iterator<HMovingEntity> iterator = clouds.iterator(); iterator.hasNext();) {
                HMovingEntity next = iterator.next();
                if (next.getPosX() + next.getWidth() <= 0) {
                    iterator.remove();
                }
            }
            //remove any hills of the canvas
            for (Iterator<HMovingEntity> iterator = hills.iterator(); iterator.hasNext();) {
                HMovingEntity next = iterator.next();
                if (next.getPosX() + next.getWidth() <= 0) {
                    iterator.remove();
                }
            }
            if (clouds.isEmpty()) {
                //generate up to five clouds
                int numOfClouds = (int) (Math.random() * 6);
                double cloudSpeed = Math.random() * 7 + 1;
                double offset = 0;
                for (int i = 0; i < numOfClouds; i++) {
                    double width = Math.random() * screenWidth / 4 + 100;
                    double height = width - 60;
                    clouds.add(new HMovingEntity(cloudSpeed, true, screenWidth + offset, height, width, height, "assets\\default\\cloud.png"));
                    cloudSpeed++;
                    offset = width + 50;
                }
            }
            if (hills.isEmpty()) {
                //generate up to seven hills
                int numOfHills = (int) (Math.random() * 8);
                double hillSpeed = Math.random() * 3 + 1;
                double offset = 0;
                for (int i = 0; i < numOfHills; i++) {
                    double width = Math.random() * screenWidth / 4 + 100;
                    double height = width / 2;
                    hills.add(new HMovingEntity(hillSpeed, true, screenWidth + offset, screenHeight, width, height, "assets\\default\\hill.png"));
                    offset = width + 50;
                }
            }
        }

        private void generatePipes(long now) {
            if (topPipe.getPosX() + topPipe.getWidth() <= 0 && bottomPipe.getPosX() + bottomPipe.getWidth() <= 0) {
                //create the gap between the top and bottom pipe
                int gapTopPosY = (int) (Math.random() * (screenHeight - 500)) + gapLength;
                int gapBottomPosY = gapTopPosY + gapLength;

                //create new top and bottom pipes at random positions
                topPipe = new HMovingEntity(topPipe.getEntitySpeed(), true,
                        screenWidth, gapTopPosY, topPipe.getWidth(), gapTopPosY, "assets\\default\\top_pipe.png");
                bottomPipe = new HMovingEntity(bottomPipe.getEntitySpeed(), true,
                        screenWidth, screenHeight, bottomPipe.getWidth(), screenHeight - gapBottomPosY, "assets\\default\\bottom_pipe.png");
                //remove items that are off the screen
                item = null;
                //30 percent chance to generate an item
                int itemGenerationChance = (int) (Math.random() * 101);
                if (itemGenerationChance >= 70) {
                    FlappyBirdItem[] items = FlappyBirdItem.values();
                    FlappyBirdItem itemType = items[(int) (Math.random() * items.length)];
                    item = new ItemEntity(itemType, 8, true, now, screenWidth, gapTopPosY, 50, 50, "assets\\default\\random_item_1.png");
                }
            }
        }

        private void useItem(FlappyBirdItem item) {
            if (item == FlappyBirdItem.DISJOINT_PIPES_BUFF) {
                topPipe.setPosX(topPipe.getPosX() + topPipe.getWidth() * 3);
            }
        }

        private void executeGameLogic() {
            //if the FlappyBird passes the bottom pipe, add a point to the score
            if (flappyBird.getPosX() == bottomPipe.getPosX() + bottomPipe.getWidth()) {
                score++;
                scoreLabel.setText("Score :: " + score);
            }
            //if the FlappyBird hits an item, use the item
            if (item != null && flappyBird.intersects(item)) {
                useItem(item.getItem());
                item = null;
            }
            //if the FlappyBird hits a pipe, end the game
            if (flappyBird.intersects(topPipe) || flappyBird.intersects(bottomPipe)) {
                stop();
                Label gameOverLabel = new Label(" \t[GAME OVER]\nPress SPACE to Continue");
                gameOverLabel.setTextFill(Color.RED);
                gameOverLabel.setFont(new Font("Bodoni MT Black", 20));
                root.setCenter(gameOverLabel);
                mapScene.setOnKeyPressed((event) -> {
                    if (event.getCode() == KeyCode.SPACE) {
                        primaryStage.setScene(mainMenuScene);
                        primaryStage.setFullScreen(true);
                        primaryStage.setFullScreenExitHint("");
                        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
                    }
                });
            }
        }

        @Override
        public void handle(long now) {
            //handle user inputs
            flappyBird.setIsMovingDown(flappyBirdIsFlying == false);
            //update FlappyBird position
            flappyBird.update();

            generateScenery();
            generatePipes(now);

            clouds.forEach(cloud -> {
                cloud.update();
            });
            hills.forEach(hill -> {
                hill.update();
            });
            topPipe.update();
            bottomPipe.update();
            if (item != null) {
                item.setCurrentTime(now);
                item.update();
            }

            executeGameLogic();

            //render
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, screenWidth, screenHeight);
            gc.setFill(Color.SKYBLUE);
            gc.fillRect(0, 0, screenWidth, screenHeight);
            clouds.forEach(cloud -> {
                cloud.render(gc);
            });
            hills.forEach(hill -> {
                hill.render(gc);
            });
            flappyBird.render(gc);
            if (item != null) {
                item.render(gc);
            }
            topPipe.render(gc);
            bottomPipe.render(gc);
        }

    }

    private class LoadedMapGameEngine extends AnimationTimer {

        private final String mapDataFile;
        private String assetsFileFolderName;
        private int numOfPasses;

        private final Queue<HMovingEntity> nextBottomPipes;
        private final Queue<HMovingEntity> nextTopPipes;
        private final Queue<MovingEntity> nextMovingEntities;

        public LoadedMapGameEngine(String mapDataFile) throws FileNotFoundException {
            this.mapDataFile = mapDataFile;
            nextBottomPipes = new LinkedList<>();
            nextTopPipes = new LinkedList<>();
            nextMovingEntities = new LinkedList<>();
            loadFlappyMapFile();
        }
        
        private void loadFlappyMapFile() throws FileNotFoundException{
            Scanner scanner = new Scanner(new File(mapDataFile));
            assetsFileFolderName = scanner.next();
            numOfPasses = scanner.nextInt();
            for (int i = 0; i < numOfPasses; i++) {
                if (scanner.hasNextLine() == false) {
                    System.err.println("Error in FlappyMapFile :: " + mapDataFile
                            + "\nexpected next line of arguments (only " + (i + 1) + " lines of arguments found,"
                            + "expected " + numOfPasses + ")");
                    System.exit(1);
                }
                String entityArgs[] = scanner.nextLine().split("\\s");
                if (entityArgs.length != 3) {
                    System.err.println("Error in FlappyMapFile :: " + mapDataFile
                            + "\nline " + (3 + i) + " :: expected 3 arguments, found " 
                            + entityArgs.length + " arguments");
                    System.exit(1);
                }
                
                String bottomPipeArgument = entityArgs[0];
                if (bottomPipeArgument.equals("null")) {
                    nextBottomPipes.offer(null);
                } else {
                    if (bottomPipeArgument.startsWith("b_pipe") == false) {
                        System.err.println("Error in FlappyMapFile :: " + mapDataFile
                                + "\nline " + (3 + i) + " :: argument does not"
                                        + "refer to b_pipe");
                        System.exit(1);
                    }
                    else if(bottomPipeArgument.contains("=") == false){
                        System.err.println("Error in FlappyMapFile :: " + mapDataFile
                                + "\nline " + (3 + i) + " :: missing b_pipe assignment");
                        System.exit(1);
                    }
                }
                String bottomPipeLocation = bottomPipeArgument.substring(7);
                if(bottomPipeLocation.equals("low")){
                    nextBottomPipes.offer(new HMovingEntity(5, true, screenWidth, screenHeight, 60, screenHeight / 3 - gapLength));
                }
                else if(bottomPipeLocation.equals("med")){
                    nextBottomPipes.offer(new HMovingEntity(5, true, screenWidth, screenHeight, 60, screenHeight / 2 - gapLength));
                }
                else if(bottomPipeLocation.equals("high")){
                    nextBottomPipes.offer(new HMovingEntity(5, true, screenWidth, screenHeight, 60, screenHeight / 1.5 - gapLength));
                }
                else{
                    System.err.println("Error in FlappyMapFile :: " + mapDataFile + 
                            "\nline " + (3 + i) + " :: unrecognized location argument \"" + bottomPipeLocation + "\"");
                    System.exit(1);
                }
                
                String topPipeArgument = entityArgs[1];
                if (topPipeArgument.equals("null")) {
                    nextTopPipes.offer(null);
                } else {
                    if (topPipeArgument.startsWith("t_pipe") == false) {
                        System.err.println("Error in FlappyMapFile :: " + mapDataFile
                                + "\nline " + (3 + i) + " :: argument does not"
                                        + "refer to t_pipe");
                        System.exit(1);
                    }
                    else if(topPipeArgument.contains("=") == false){
                        System.err.println("Error in FlappyMapFile :: " + mapDataFile
                                + "\nline " + (3 + i) + " :: missing t_pipe assignment");
                        System.exit(1);
                    }
                }               
                
                String movingEntityArgument = entityArgs[2];
                if (movingEntityArgument.equals("null")) {
                    nextMovingEntities.offer(null);
                } else {
                    if (movingEntityArgument.startsWith("item") == false) {
                        System.err.println("Error in FlappyMapFile :: " + mapDataFile
                                + "\nline " + (3 + i) + " :: argument does not"
                                        + "refer to item");
                        System.exit(1);
                    }
                    else if(bottomPipeArgument.contains("::") == false){
                        System.err.println("Error in FlappyMapFile :: " + mapDataFile
                                + "\nline " + (3 + i) + " :: missing item type");
                        System.exit(1);
                    }
                    else if(bottomPipeArgument.contains("=") == false){
                        System.err.println("Error in FlappyMapFile :: " + mapDataFile
                                + "\nline " + (3 + i) + " :: missing item assignment");
                        System.exit(1);
                    }
                }
            }
        }

        @Override
        public void handle(long now) {

        }

    }

}
