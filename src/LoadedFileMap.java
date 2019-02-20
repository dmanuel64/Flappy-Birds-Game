
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
public class LoadedFileMap {

    private FlappyBird flappyBird;
    private boolean flappyBirdIsFlying;

    private final String flappyMapFileName;
    private final boolean mapIsLoaded;
    private String assetsFileFolderName;
    private int numOfPasses;
    private int entitySpeed;
    private int topPipeWidth;
    private int bottomPipeWidth;
    private final Queue<HMovingEntity> nextTopPipes;
    private HMovingEntity topPipe;
    private final Queue<HMovingEntity> nextBottomPipes;
    private HMovingEntity bottomPipe;
    private final Queue<MovingEntity> nextMovingEntities;
    private MovingEntity movingEntity;
    private DebuffEntity[] childrenDebuffs;
    private final ArrayList<HMovingEntity> topScenery;
    private final ArrayList<HMovingEntity> bottomScenery;
    private MapAddition mapSpecialConditions;

    private final double screenWidth = FlappyBirdGame.SCREEN_WIDTH;
    private final double screenHeight = FlappyBirdGame.SCREEN_HEIGHT;

    private final Stage primaryStage;
    private final Scene mainMenuScene;
    private final BorderPane root;
    private final Scene mapScene;
    private final Canvas canvas;
    private final ProgressBar mapCompletionBar;
    private Label scoreLabel;
    private Label timeLabel;
    private Image mapBackground;
    private long mapCompletionTime;
    private String nextMap;

    private AnimationTimer gameEngine;
    private boolean gameHasStarted;
    private long currentGameTime;
    private int score;
    private final double finishLinePosX;
    private double distanceFromStart;

    public LoadedFileMap(String flappyBirdSkin, Stage primaryStage, String flappyMapFileName) {
        //create FlappyBird based on customization parameters
        if (flappyBirdSkin == null || flappyBirdSkin.isEmpty()) {
            flappyBird = new FlappyBird(0.5, 7, screenWidth / 3, screenHeight / 2, 50, 50);
        }
        else{
            if(flappyBirdSkin.equals("flappy_bird")){
                flappyBird = new FlappyBird(0.5, 7, screenWidth / 3, screenHeight / 2, 50, 50, "assets\\skins\\" + flappyBirdSkin + ".gif");
            }
            else if(flappyBirdSkin.equals("slow_flyer")){
                flappyBird = new FlappyBird(0.3, 4, screenWidth / 3, screenHeight / 2, 50, 50, "assets\\skins\\" + flappyBirdSkin + ".gif");
            }
            else if(flappyBirdSkin.equals("quick_wings")){
                flappyBird = new FlappyBird(0.6, 10, screenWidth / 3, screenHeight / 2, 50, 50, "assets\\skins\\" + flappyBirdSkin + ".gif");
            }
        }

        this.flappyMapFileName = flappyMapFileName;
        nextTopPipes = new LinkedList<>();
        topPipe = new HMovingEntity(5, true, -1, 0, 0, 0);
        nextBottomPipes = new LinkedList<>();
        bottomPipe = new HMovingEntity(5, true, -1, 0, 100, 0);
        nextMovingEntities = new LinkedList<>();
        topScenery = new ArrayList<>();
        bottomScenery = new ArrayList<>();
        mapIsLoaded = loadFMFResource();

        this.primaryStage = primaryStage;
        mainMenuScene = primaryStage.getScene();
        //create new Scene for the EndlessMap
        root = new BorderPane();
        mapScene = new Scene(root, screenWidth, screenHeight);
        mapScene.getStylesheets().add("mainMenuStyle.css");

        //create Canvas to render Entities on
        canvas = new Canvas(screenWidth, screenHeight);
        root.getChildren().add(canvas);
        mapCompletionBar = new ProgressBar();
        try {
            scoreLabel = new Label("0", new ImageView(new Image(new FileInputStream("assets\\default\\coin.png"), 50, 50, false, true)));
            timeLabel = new Label("0", new ImageView(new Image(new FileInputStream("assets\\default\\stopwatch.png"), 50, 50, false, true)));
        } catch (FileNotFoundException e) {
            scoreLabel = new Label();
            timeLabel = new Label();
        }
        scoreLabel.setFont(new Font("Bodoni MT Black", 30));
        timeLabel.setFont(new Font("Bodoni MT Black", 12));
        //timeLabel.setFont(new Font("Bodoni MT Black", 30));
        HBox scoreBox = new HBox(screenWidth / 16, timeLabel, mapCompletionBar, scoreLabel);
        scoreBox.setAlignment(Pos.CENTER);
        mapCompletionBar.setPrefWidth(screenWidth - screenWidth / 3.5);
        root.setTop(scoreBox);

        //create the LoadedMap game engine
        gameEngine = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //set the time label to the current play time on the map
                currentGameTime++;
                timeLabel.setText((long) (currentGameTime / 100) + "."
                        + (currentGameTime % 100 < 10 ? "0" + currentGameTime % 100 : currentGameTime % 100));
                //end the game if there are no more entities entering on to the canvas
                if (nextBottomPipes.isEmpty() && nextTopPipes.isEmpty() && nextMovingEntities.isEmpty()
                        && topPipe == null && bottomPipe == null && movingEntity == null) {
                    gameEngine.stop();
                    Label gameOverLabel = new Label("   [LEVEL COMPLETE]\nPress SPACE to Continue");
                    gameOverLabel.setTextFill(Color.GREEN);
                    gameOverLabel.setFont(new Font("Bodoni MT Black", 20));
                    root.setCenter(gameOverLabel);
                    Media mapCompleteSound = new Media(new File("assets\\sounds\\map_complete.mp3").toURI() + "");
                    MediaPlayer completionSoundPlayer = new MediaPlayer(mapCompleteSound);
                    completionSoundPlayer.play();
                    mapScene.setOnKeyPressed((event) -> {
                        if (event.getCode() == KeyCode.SPACE) {
                            primaryStage.setScene(mainMenuScene);
                            primaryStage.setFullScreen(true);
                            primaryStage.setFullScreenExitHint("");
                            primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
                        }
                    });
                    if(currentGameTime / 100 < mapCompletionTime){
                        writeNextLevel();
                    }
                    writeCurrentCoinCount();
                }

                //handle user inputs
                flappyBird.setIsMovingDown(flappyBirdIsFlying == false);
                //update FlappyBird position
                flappyBird.update();
                //adjust FlappyBird offest
                if (flappyBird.getPosX() < screenWidth / 3) {
                    flappyBird.setPosX(flappyBird.getPosX() + 2);
                }
                //update progress bar
                if (topPipe != null && topPipe.getEntitySpeed() > entitySpeed) {
                    distanceFromStart += topPipe.getEntitySpeed();
                } else if (bottomPipe != null && bottomPipe.getEntitySpeed() > entitySpeed) {
                    distanceFromStart += bottomPipe.getEntitySpeed();
                } else {
                    distanceFromStart += entitySpeed;
                }
                mapCompletionBar.setProgress(distanceFromStart / finishLinePosX);

                generateScenery();
                generateNextEntities(now);

                //update entity positions
                topScenery.forEach(movingEntity -> {
                    movingEntity.update();
                });
                bottomScenery.forEach(movingEntity -> {
                    movingEntity.update();
                });
                if (topPipe != null) {
                    topPipe.update();
                }
                if (bottomPipe != null) {
                    bottomPipe.update();
                }
                if (movingEntity != null) {
                    movingEntity.setCurrentTime(now);
                    movingEntity.update();
                    if (childrenDebuffs != null) {
                        for (DebuffEntity childDebuffEntity : childrenDebuffs) {
                            childDebuffEntity.update();
                        }
                    }
                }

                //execute map special conditions
                if (mapSpecialConditions != null) {
                    mapSpecialConditions.executeAdditionTask();
                    if ((mapSpecialConditions.getMapAdditionData() & MapAddition.INFINITE_RAIN) == MapAddition.INFINITE_RAIN) {
                        useDebuff(FlappyBirdDebuff.RAIN_DEBUFF);
                    }
                }

                //check game logic
                executeGameLogic();

                //render
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0, 0, screenWidth, screenHeight);
                gc.drawImage(mapBackground, 0, 0);
                topScenery.forEach(movingEntity -> {
                    movingEntity.render(gc);
                });
                bottomScenery.forEach(movingEntity -> {
                    movingEntity.render(gc);
                });
                flappyBird.render(gc);
                if (movingEntity != null) {
                    if (childrenDebuffs != null) {
                        for (DebuffEntity childDebuffEntity : childrenDebuffs) {
                            childDebuffEntity.render(gc);
                        }
                    }
                    movingEntity.render(gc);
                }
                if (topPipe != null) {
                    topPipe.render(gc);
                }
                if (bottomPipe != null) {
                    bottomPipe.render(gc);
                }
                if (mapSpecialConditions != null) {
                    ArrayList<Entity> specialEntities = mapSpecialConditions.getEntities();
                    for (Entity entity : specialEntities) {
                        entity.render(gc);
                    }
                }
            }
        };
        finishLinePosX = (screenWidth + bottomPipeWidth) * numOfPasses;
    }

    public FlappyBird getFlappyBird() {
        return flappyBird;
    }

    public String getFMFName() {
        return flappyMapFileName;
    }

    public boolean mapIsLoaded() {
        return mapIsLoaded;
    }

    public String getAssetsFileFolderName() {
        return assetsFileFolderName;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Scene getMapScene() {
        return mapScene;
    }

    public long getGameTime() {
        return currentGameTime;
    }

    public int getScore() {
        return score;
    }

    public void startMap() {
        if (gameHasStarted) {
            throw new IllegalStateException("FlappyBird Map has already been started");
        }
        if (mapIsLoaded == false) {
            throw new IllegalStateException("Cannot load FlappyBird Map with corrupted FMF asset");
        }
        gameHasStarted = true;
        primaryStage.setScene(mapScene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        Label mapGameLabel = new Label("Complete the Level in " + mapCompletionTime + " Seconds\n\t"
                + "  to Unlock Level " + nextMap + "\n      Use the W Key to Fly Up\n\t"
                + "Press SPACE to Start");
        mapGameLabel.setTextFill(Color.BLUE);
        mapGameLabel.setFont(new Font("Bodoni MT Black", 20));
        root.setCenter(mapGameLabel);
        mapScene.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.SPACE) {
                root.setCenter(null);
                //create key bindings
                mapScene.setOnKeyPressed((keyEvent) -> {
                    KeyCode code = keyEvent.getCode();
                    if (code == KeyCode.W) {
                        flappyBirdIsFlying = true;
                    } else if (code == KeyCode.ESCAPE) {
                        pauseGame();
                    }
                });
                mapScene.setOnKeyReleased((keyEvent) -> {
                    KeyCode code = keyEvent.getCode();
                    if (code == KeyCode.W) {
                        flappyBirdIsFlying = false;
                    }
                });
            }
            gameEngine.start();
        });
    }

    public void pauseGame() {
        if (gameHasStarted == false) {
            throw new IllegalStateException("FlappyBird Map has not yet been started");
        }
        gameEngine.stop();
        VBox menuBox = new VBox(50);
        menuBox.setAlignment(Pos.CENTER);
        root.setCenter(menuBox);
        Button resumeButton = new Button("Resume");
        resumeButton.setOnAction((event) -> {
            root.setCenter(null);
            gameEngine.start();
        });
        resumeButton.setOnMouseEntered((event) -> {
            Media buttonEnteredSound = new Media(new File("assets\\sounds\\button_entered.mp3").toURI() + "");
            MediaPlayer buttonEnteredSoundPlayer = new MediaPlayer(buttonEnteredSound);
            buttonEnteredSoundPlayer.setVolume(0.5);
            buttonEnteredSoundPlayer.play();
            resumeButton.setPrefSize(resumeButton.getWidth() * 1.25, resumeButton.getHeight() * 1.25);
        });
        resumeButton.setOnMouseExited((event) -> {
            resumeButton.setPrefSize(resumeButton.getWidth() / 1.25, resumeButton.getHeight() / 1.25);
        });
        menuBox.getChildren().add(resumeButton);
        Button restartButton = new Button("Restart");
        restartButton.setOnMouseEntered((event) -> {
            Media buttonEnteredSound = new Media(new File("assets\\sounds\\button_entered.mp3").toURI() + "");
            MediaPlayer buttonEnteredSoundPlayer = new MediaPlayer(buttonEnteredSound);
            buttonEnteredSoundPlayer.setVolume(0.5);
            buttonEnteredSoundPlayer.play();
            restartButton.setPrefSize(restartButton.getWidth() * 1.25, restartButton.getHeight() * 1.25);
        });
        restartButton.setOnMouseExited((event) -> {
            restartButton.setPrefSize(restartButton.getWidth() / 1.25, restartButton.getHeight() / 1.25);
        });
        menuBox.getChildren().add(restartButton);
        Button exitButton = new Button("Exit to Menu");
        exitButton.setOnAction((event) -> {
            primaryStage.setScene(mainMenuScene);
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        });
        exitButton.setOnMouseEntered((event) -> {
            Media buttonEnteredSound = new Media(new File("assets\\sounds\\button_entered.mp3").toURI() + "");
            MediaPlayer buttonEnteredSoundPlayer = new MediaPlayer(buttonEnteredSound);
            buttonEnteredSoundPlayer.setVolume(0.5);
            buttonEnteredSoundPlayer.play();
            exitButton.setPrefSize(exitButton.getWidth() * 1.25, exitButton.getHeight() * 1.25);
        });
        exitButton.setOnMouseExited((event) -> {
            exitButton.setPrefSize(exitButton.getWidth() / 1.25, exitButton.getHeight() / 1.25);
        });
        menuBox.getChildren().add(exitButton);
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
                topScenery.add(new HMovingEntity(assetSpeed, true, screenWidth + offset, height, width, height, assetsFileFolderName + "\\top_scenery.png"));
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
                bottomScenery.add(new HMovingEntity(assetSpeed, true, screenWidth + offset, screenHeight, width, height, assetsFileFolderName + "\\bottom_scenery.png"));
                offset = width + 50;
            }
        }
    }

    private void generateNextEntities(long now) {
        if (bottomPipe == null || bottomPipe.getPosX() + bottomPipe.getWidth() <= 0) {
            bottomPipe = nextBottomPipes.poll();
            topPipe = nextTopPipes.poll();

            //load the next MovingEntity
            MovingEntity loadedMovingEntity = nextMovingEntities.poll();
            if (loadedMovingEntity instanceof ItemEntity) {
                ItemEntity loadedItemEntity = (ItemEntity) loadedMovingEntity;
                movingEntity = new ItemEntity(loadedItemEntity.getItem(), entitySpeed, true, now, screenWidth, loadedItemEntity.getPosY(),
                        loadedItemEntity.getWidth(), loadedItemEntity.getHeight(), loadedItemEntity.getImageFilePath());
            } else if (loadedMovingEntity instanceof DebuffEntity) {
                DebuffEntity loadedDebuffEntity = (DebuffEntity) loadedMovingEntity;
                movingEntity = new DebuffEntity(loadedDebuffEntity.getDebuff(), loadedDebuffEntity.getEntitySpeed(), true, now, screenWidth, loadedDebuffEntity.getPosY(),
                        loadedDebuffEntity.getWidth(), loadedDebuffEntity.getHeight(), loadedDebuffEntity.getImageFilePath());
                FlappyBirdDebuff debuff = loadedDebuffEntity.getDebuff();
                //if the DebuffEntity has children debuffs needed to be created at the start of the frame, create children debuffs
                if (debuff == FlappyBirdDebuff.RAIN_CLOUD_DEBUFF || debuff == FlappyBirdDebuff.BIRD_SWARM_DEBUFF) {
                    childrenDebuffs = loadedDebuffEntity.createChildrenDebuffs();
                }
            } else {
                movingEntity = loadedMovingEntity;
            }
        }
    }

    private void useItem(FlappyBirdItem item) {
        if (item == FlappyBirdItem.WIND) {
            Media windBoostSound = new Media(new File("assets\\sounds\\wind_boost.mp3").toURI() + "");
            MediaPlayer windBoostSoundPlayer = new MediaPlayer(windBoostSound);
            windBoostSoundPlayer.setVolume(0.5);
            windBoostSoundPlayer.play();
            if (topPipe != null) {
                topPipe.setEntitySpeed(entitySpeed * 2);
            }
            if (bottomPipe != null) {
                bottomPipe.setEntitySpeed(entitySpeed * 2);
            }
            topScenery.forEach(scenicEntity -> {
                scenicEntity.setEntitySpeed(scenicEntity.getEntitySpeed() * 2);
            });
            bottomScenery.forEach(scenicEntity -> {
                scenicEntity.setEntitySpeed(scenicEntity.getEntitySpeed() * 2);
            });
        } else if (item == FlappyBirdItem.COIN) {
            Media collectCoinSound = new Media(new File("assets\\sounds\\collect_coin.mp3").toURI() + "");
            MediaPlayer collectCoinSoundPlayer = new MediaPlayer(collectCoinSound);
            collectCoinSoundPlayer.setVolume(0.5);
            collectCoinSoundPlayer.play();
            scoreLabel.setText(++score + "");
        }
    }

    private void useDebuff(FlappyBirdDebuff debuff) {
        if (null == debuff) {
            topPipe.setVelocityX(entitySpeed * -1);
            bottomPipe.setVelocityX(entitySpeed * -1);
        } else switch (debuff) {
            case RAIN_DEBUFF:
                flappyBird.setVelocityY(flappyBird.getVelocityY() - 2);
                break;
            case BIRD_SWARM_DEBUFF:
            case BIRD_DEBUFF:
                flappyBird.setPosX(flappyBird.getPosX() + this.movingEntity.getVelocityX() - 2);
                if (topPipe != null) {
                    topPipe.setVelocityX(entitySpeed * -1 - movingEntity.getVelocityX());
                }   if (bottomPipe != null) {
                    bottomPipe.setVelocityX(entitySpeed * -1 - movingEntity.getVelocityX());
                }   break;
            case LIGHTNING_DEBUFF:
                Media lightningBoltSFX = new Media(new File("assets\\sounds\\lightning_strike.mp3").toURI() + "");
                MediaPlayer lightningBoltSFXPlayer = new MediaPlayer(lightningBoltSFX);
                lightningBoltSFXPlayer.play();
                gameEngine.stop();
                Label gameOverLabel = new Label(" \t[GAME OVER]\nPress SPACE to Continue");
                gameOverLabel.setTextFill(Color.RED);
                gameOverLabel.setFont(new Font("Bodoni MT Black", 20));
                root.setCenter(gameOverLabel);
                mapScene.setOnKeyPressed((event) -> {
                    if (event.getCode() == KeyCode.SPACE) {
                        writeCurrentCoinCount();
                        primaryStage.setScene(mainMenuScene);
                        primaryStage.setFullScreen(true);
                        primaryStage.setFullScreenExitHint("");
                        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
                    }
                }); break;
            default:
                topPipe.setVelocityX(entitySpeed * -1);
                bottomPipe.setVelocityX(entitySpeed * -1);
                break;
        }
    }

    private void executeGameLogic() {
        //if the FlappyBird hits an item or a debuff, use its effect
        if (movingEntity != null && flappyBird.intersects(movingEntity)) {
            if (movingEntity instanceof ItemEntity) {
                ItemEntity movingItem = (ItemEntity) movingEntity;
                useItem(movingItem.getItem());
                //remove the item off the canvas
                movingEntity = null;
            } else {
                DebuffEntity movingDebuff = (DebuffEntity) movingEntity;
                useDebuff(movingDebuff.getDebuff());
            }
        }
        //if the FlappyBird hits a child debuff, use its effect
        if (childrenDebuffs != null) {
            for (DebuffEntity childDebuff : childrenDebuffs) {
                if (flappyBird.intersects(childDebuff)) {
                    useDebuff(childDebuff.getDebuff());
                }
            }
        }
        //check for extended intersections
        boolean isIntersectingExtendedTopPipe = topPipe == null ? false : flappyBird.getPosY() < 0
                && topPipe.getPosX() < flappyBird.getPosX() && flappyBird.getPosX() < topPipe.getPosX() + topPipe.getWidth();
        boolean isIntersectingExtendedBottomPipe = bottomPipe == null ? false : flappyBird.getPosY() > screenHeight
                && bottomPipe.getPosX() < flappyBird.getPosX() && flappyBird.getPosX() < bottomPipe.getPosX() + bottomPipe.getWidth();
        //if the FlappyBird hits a pipe, end the game
        if (topPipe != null && flappyBird.intersects(topPipe) || bottomPipe != null && flappyBird.intersects(bottomPipe)
                || isIntersectingExtendedTopPipe || isIntersectingExtendedBottomPipe) {
            gameEngine.stop();
            Media deathSound = new Media(new File("assets\\sounds\\slap.mp3").toURI() + "");
            Media endGameSound = new Media(new File("assets\\sounds\\lost_game_sfx.mp3").toURI() + "");
            MediaPlayer deathSoundPlayer = new MediaPlayer(deathSound);
            MediaPlayer endGameSoundPlayer = new MediaPlayer(endGameSound);
            endGameSoundPlayer.setVolume(0.5);
            deathSoundPlayer.play();
            endGameSoundPlayer.play();
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
            writeCurrentCoinCount();
        }
    }

    private boolean loadFMFResource() {
        //load the FMF file
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(flappyMapFileName));
        } catch (FileNotFoundException e) {
            System.err.println("Could not load FlappyMap File :: " + flappyMapFileName);
            return false;
        }
        //load the assets folder
        if (scanner.hasNext() == false) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline 1 :: missing assets folder");
            return false;
        }
        assetsFileFolderName = scanner.next();
        //load map background
        try {
            mapBackground = new Image(new FileInputStream(assetsFileFolderName + "\\background.png"), screenWidth, screenHeight, false, true);
        } catch (FileNotFoundException e) {
            System.err.println("Could not find asset \"background.png\" in " + assetsFileFolderName);
            return false;
        }
        //load the number of sets of pipes
        if (scanner.hasNextInt() == false) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline 2 :: missing number of passes");
            return false;
        }
        numOfPasses = scanner.nextInt();
        //load the pipes' speed
        if (scanner.hasNextInt() == false) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline 3 :: missing pipes' speed");
            return false;
        }
        entitySpeed = scanner.nextInt();
        //load the pipe widths
        if (scanner.hasNextLine() == false) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline 4 :: missing pipe widths");
            return false;
        }
        scanner.nextLine();
        String[] entityWidths = scanner.nextLine().split("\\s");
        if (entityWidths.length != 2) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline 4 :: expected 2 width parameters, found " + entityWidths.length + " parameters");
            return false;
        }
        try {
            bottomPipeWidth = Integer.parseInt(entityWidths[0]);
        } catch (NumberFormatException e) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline 4 :: missing bottom pipe width");
            return false;
        }
        try {
            topPipeWidth = Integer.parseInt(entityWidths[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline 4 :: missing top pipe width");
            return false;
        }
        //load any special map conditions
        if (scanner.hasNextLine() == false) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline 4 :: missing special map conditions");
            return false;
        }
        String[] mapConditions = scanner.nextLine().split("\\s");
        if (mapConditions[0].equals("null") == false) {
            byte conditionData = 0;
            for (String condition : mapConditions) {
                if (condition.equals("INFINITE_RAIN")) {
                    conditionData |= MapAddition.INFINITE_RAIN;
                } else if (condition.equals("RISING_WATER")) {
                    conditionData |= MapAddition.RISING_WATER;
                } else if (condition.equals("MOVING_FOG")) {
                    conditionData |= MapAddition.MOVING_FOG;
                } else {
                    System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                            + "\nline 5 :: unknown map addition argument \"" + condition + "\"");
                    return false;
                }
            }
            mapSpecialConditions = new MapAddition(conditionData, screenWidth, screenHeight);
        }

        for (int i = 0; i < numOfPasses; i++) {
            if (scanner.hasNextLine() == false) {
                System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                        + "\nline " + (i + 6) + " :: expected parameters b_pipe, t_pipe, and item ("
                        + i + " lines of parameters found, expected " + numOfPasses + " lines of parameters)");
                return false;
            }
            String[] entityParameters = scanner.nextLine().split("\\s");
            if (entityParameters.length != 3) {
                System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                        + "\nline " + (i + 6) + " :: expected 3 parameters, found " + entityParameters.length
                        + " parameters");
                return false;
            }
            String bottomPipeParameter = entityParameters[0];
            if (bottomPipeParameter.startsWith("b_pipe") == false && bottomPipeParameter.equals("null") == false) {
                System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                        + "\nline " + (i + 6) + " :: missing parameter \"b_pipe\"");
                return false;
            } else if (bottomPipeParameter.equals("null")) {
                nextBottomPipes.offer(null);
            } else {
                if (bottomPipeParameter.contains("=") == false || bottomPipeParameter.endsWith("=")) {
                    System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                            + "\nline " + (i + 6) + " :: parameter \"b_pipe\" is missing an argument");
                    return false;
                }
                if (createMovingEntity("b_pipe", bottomPipeParameter.substring(7)) == false) {
                    System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                            + "\nline " + (i + 6) + " :: parameter \"b_pipe\" unkown argument \""
                            + bottomPipeParameter.substring(7) + "\"");
                    return false;
                }
            }

            String topPipeParameter = entityParameters[1];
            if (topPipeParameter.startsWith("t_pipe") == false && topPipeParameter.equals("null") == false) {
                System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                        + "\nline " + (i + 6) + " :: missing parameter \"t_pipe\"");
                return false;
            } else if (topPipeParameter.equals("null")) {
                nextTopPipes.offer(null);
            } else {
                if (topPipeParameter.contains("=") == false || topPipeParameter.endsWith("=")) {
                    System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                            + "\nline " + (i + 6) + " :: parameter \"t_pipe\" is missing an argument");
                    return false;
                }
                if (createMovingEntity("t_pipe", topPipeParameter.substring(7)) == false) {
                    System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                            + "\nline " + (i + 6) + " :: parameter \"t_pipe\" unkown argument \""
                            + topPipeParameter.substring(7) + "\"");
                    return false;
                }
            }

            String movingEntityParameter = entityParameters[2];
            if (movingEntityParameter.equals("null")) {
                nextMovingEntities.offer(null);
            } else if (movingEntityParameter.startsWith("item") ^ movingEntityParameter.startsWith("debuff")) {
                String movingEntityType = movingEntityParameter.startsWith("item") ? "item" : "debuff";
                if (movingEntityParameter.contains("::") == false || movingEntityParameter.endsWith("::")) {
                    System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                            + "\nline " + (i + 6) + " :: parameter \"" + movingEntityType + "\" is missing a type");
                    return false;
                }
                if (movingEntityParameter.contains("=") == false || movingEntityParameter.endsWith("=")) {
                    System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                            + "\nline " + (i + 6) + " :: parameter \"" + movingEntityType + "\" is missing an argument");
                    return false;
                }
                if (createMovingEntity(movingEntityParameter.substring(0, movingEntityParameter.indexOf("=")),
                        movingEntityParameter.substring(movingEntityParameter.indexOf("=") + 1)) == false) {
                    System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                            + "\nline " + (i + 6) + " :: parameter \"item\" or \"debuff\" has an unkown"
                            + " type or argument");
                    return false;
                }
            } else {
                System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                        + "\nline " + (i + 6) + " :: missing parameter \"item\" or \"debuff\"");
                return false;
            }
        }
        //load the time needed to complete the map and the next map
        if (scanner.hasNextLine() == false) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline " + (numOfPasses + 6) + " :: missing map completion time and next map");
            return false;
        }
        String[] nextMapTime = scanner.nextLine().split("\\s");
        if (nextMapTime.length != 2) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline " + numOfPasses + 6 + " :: missing map completion time or next map");
            return false;
        }
        try {
            mapCompletionTime = Integer.parseInt(nextMapTime[0]);
        } catch (NumberFormatException e) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline " + numOfPasses + 6 + " :: missing map completion time");
            return false;
        }
        if (nextMapTime[1].matches("\\d-\\d") == false) {
            System.err.println("Error in FlappyMap File :: " + flappyMapFileName
                    + "\nline " + numOfPasses + 6 + " :: missing next map");
            return false;
        }
        nextMap = nextMapTime[1];

        return true;
    }

    private boolean createMovingEntity(String movingEntityType, String positionArgument) {
        if (movingEntityType.equals("b_pipe")) {
            HMovingEntity bottomPipe = null;
            switch (positionArgument) {
                case "high":
                    bottomPipe = new HMovingEntity(entitySpeed, true, screenWidth, screenHeight, bottomPipeWidth, screenHeight / 1.5
                            - flappyBird.getHeight(), assetsFileFolderName + "\\bottom_pipe.png");
                    break;
                case "med":
                    bottomPipe = new HMovingEntity(entitySpeed, true, screenWidth, screenHeight, bottomPipeWidth, screenHeight / 2
                            - flappyBird.getHeight(), assetsFileFolderName + "\\bottom_pipe.png");
                    break;
                case "low":
                    bottomPipe = new HMovingEntity(entitySpeed, true, screenWidth, screenHeight, bottomPipeWidth, screenHeight / 3
                            - flappyBird.getHeight(), assetsFileFolderName + "\\bottom_pipe.png");
                    break;
                default:
                    return false;
            }
            nextBottomPipes.offer(bottomPipe);
        } else if (movingEntityType.equals("t_pipe")) {
            HMovingEntity topPipe = null;
            switch (positionArgument) {
                case "high":
                    topPipe = new HMovingEntity(entitySpeed, true, screenWidth, screenHeight / 3 - flappyBird.getHeight(),
                            topPipeWidth, screenHeight / 3 - flappyBird.getHeight(), assetsFileFolderName + "\\top_pipe.png");
                    break;
                case "med":
                    topPipe = new HMovingEntity(entitySpeed, true, screenWidth, screenHeight / 2 - flappyBird.getHeight(),
                            topPipeWidth, screenHeight / 2 - flappyBird.getHeight(), assetsFileFolderName + "\\top_pipe.png");
                    break;
                case "low":
                    topPipe = new HMovingEntity(entitySpeed, true, screenWidth, screenHeight / 1.5 - flappyBird.getHeight(),
                            topPipeWidth, screenHeight / 1.5 - flappyBird.getHeight(), assetsFileFolderName + "\\top_pipe.png");
                    break;
                default:
                    return false;
            }
            nextTopPipes.offer(topPipe);
        } else if (movingEntityType.startsWith("item")) {
            String itemType = movingEntityType.substring(movingEntityType.indexOf(":") + 2);
            FlappyBirdItem item = null;
            try {
                item = FlappyBirdItem.valueOf(itemType);
            } catch (IllegalArgumentException e) {
                return false;
            }
            itemType = itemType.toLowerCase();
            ItemEntity itemEntity = null;
            switch (positionArgument) {
                case "high":
                    itemEntity = new ItemEntity(item, entitySpeed, true, 0, screenWidth, screenHeight / 3, 50, 50, "assets\\default\\" + itemType + ".gif");
                    break;
                case "med":
                    itemEntity = new ItemEntity(item, entitySpeed, true, 0, screenWidth, screenHeight / 2, 50, 50, "assets\\default\\" + itemType + ".gif");
                    break;
                case "low":
                    itemEntity = new ItemEntity(item, entitySpeed, true, 0, screenWidth, screenHeight / 1.5, 50, 50, "assets\\default\\" + itemType + ".gif");
                    break;
                default:
                    return false;
            }
            nextMovingEntities.offer(itemEntity);
        } else if (movingEntityType.startsWith("debuff")) {
            String debuffType = movingEntityType.substring(movingEntityType.indexOf(":") + 2);
            FlappyBirdDebuff debuff = null;
            try {
                debuff = FlappyBirdDebuff.valueOf(debuffType);
            } catch (IllegalArgumentException e) {
                return false;
            }
            debuffType = debuffType.toLowerCase();
            //adjust asset load
            switch (debuffType) {
                case "bird_swarm_debuff":
                    debuffType = "bird_debuff.gif";
                    break;
                case "rain_cloud_debuff":
                    debuffType = "rain_cloud.png";
                    break;
                default:
                    debuffType += ".png";
                    break;
            }
            DebuffEntity debuffEntity = null;
            switch (positionArgument) {
                case "high":
                    debuffEntity = new DebuffEntity(debuff, entitySpeed * 2, true, 0, screenWidth, screenHeight / 3, 50, 50, "assets\\default\\"
                            + debuffType);
                    break;
                case "med":
                    debuffEntity = new DebuffEntity(debuff, entitySpeed * 2, true, 0, screenWidth, screenHeight / 2, 50, 50, "assets\\default\\"
                            + debuffType);
                    break;
                case "low":
                    debuffEntity = new DebuffEntity(debuff, entitySpeed * 2, true, 0, screenWidth, screenHeight / 1.5, 50, 50, "assets\\default\\"
                            + debuffType);
                    break;
                default:
                    return false;
            }
            nextMovingEntities.offer(debuffEntity);
        }
        return true;
    }

    private void writeNextLevel() {
        //load the current load
        ArrayList<String> currentLoad = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("current_load.dat"));
            scanner.nextLine();
            while(scanner.hasNextLine()){
                currentLoad.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot load \"current_load.dat\"");
        }
        //write the next level to the current load
        try {
            PrintWriter pw = new PrintWriter("current_load.dat");
            pw.println(nextMap);
            for(String currentLoadLine : currentLoad){
                pw.println(currentLoadLine);
            }
            pw.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not write next level to \"current_load.dat\"");
        }
    }
    
    private void writeCurrentCoinCount(){
        //load the current load
        String nextLevel = "";
        int coinCount = 0;
        ArrayList<String> currentLoad = new ArrayList<>();
        try{
            Scanner scanner = new Scanner(new File("current_load.dat"));
            nextLevel = scanner.nextLine();
            coinCount = scanner.nextInt();
            scanner.nextLine();
            while(scanner.hasNextLine()){
                currentLoad.add(scanner.nextLine());
            }
        }
        catch(FileNotFoundException e){
            System.out.println("Cannot load \"current_load.dat\"");
        }
        //write the coin count to the current load
        try {
            PrintWriter pw = new PrintWriter("current_load.dat");
            pw.println(nextLevel);
            pw.println(coinCount + score);
            for(String currentLoadLine : currentLoad){
                pw.println(currentLoadLine);
            }
            pw.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not write coin count to \"current_load.dat\"");
        }
    }
}
