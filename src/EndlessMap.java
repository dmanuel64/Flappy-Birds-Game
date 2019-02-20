
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
 * The {@code EndlessMap} is a type of map that is played in the Flappy Bird
 * Game. The {@code EndlessMap} handles {@code Scene} transitions from the
 * {@code FlappyBirdGame} main menu and checks the game logic on its game engine
 * The {@code EndlessMap}'s game engine allows the user to play until the user
 * loses the game by hitting a pipe
 *
 * @author dylan
 */
public class EndlessMap {

    private FlappyBird flappyBird;
    private final double flappyBirdWidth;
    private final double flappyBirdHeight;
    private boolean flappyBirdIsFlying;
    private boolean hasPassedBottomPipe;
    private double pipeGapLength;
    private double pipeSpeed;
    private HMovingEntity topPipe;
    private HMovingEntity bottomPipe;
    private ItemEntity item;
    private DebuffEntity debuff;
    private DebuffEntity[] childDebuffs;
    private ArrayList<HMovingEntity> hills;
    private ArrayList<HMovingEntity> clouds;

    private final double screenWidth = FlappyBirdGame.SCREEN_WIDTH;
    private final double screenHeight = FlappyBirdGame.SCREEN_HEIGHT;

    private final Stage primaryStage;
    private final Scene mainMenuScene;
    private final BorderPane root;
    private final Scene mapScene;
    private final Canvas canvas;
    private final Label scoreLabel;

    private final AnimationTimer gameEngine;
    private boolean gameHasStarted;
    private int score;

    /**
     * Creates a new {@code EndlessMap} with the user's Flappy Bird
     * customizations
     *
     * @param flappyBirdSkin the string of the Flappy Bird skin being used on
     * this map
     * @param flappyBirdTrail the string of the Flappy Bird trail being used on
     * this map
     * @param primaryStage the @{code Stage} being used on the main menu
     */
    public EndlessMap(String flappyBirdSkin, Stage primaryStage) {
        //create FlappyBird based on customization parameters
        if (flappyBirdSkin == null || flappyBirdSkin.isEmpty()) {
            flappyBird = new FlappyBird(0.5, 7, screenWidth / 3, screenHeight / 2, 50, 50);
        } else {
            if (flappyBirdSkin.equals("flappy_bird")) {
                flappyBird = new FlappyBird(0.5, 7, screenWidth / 3, screenHeight / 2, 50, 50, "assets\\skins\\" + flappyBirdSkin + ".gif");
            } else if (flappyBirdSkin.equals("slow_flyer")) {
                flappyBird = new FlappyBird(0.3, 4, screenWidth / 3, screenHeight / 2, 50, 50, "assets\\skins\\" + flappyBirdSkin + ".gif");
            } else if (flappyBirdSkin.equals("quick_wings")) {
                flappyBird = new FlappyBird(0.6, 10, screenWidth / 3, screenHeight / 2, 50, 50, "assets\\skins\\" + flappyBirdSkin + ".gif");
            }
        }
        flappyBirdWidth = flappyBird.getWidth();
        flappyBirdHeight = flappyBird.getHeight();
        pipeGapLength = flappyBird.getHeight() * 3;
        pipeSpeed = 5;
        topPipe = new HMovingEntity(pipeSpeed, true, 60 * -1, 0, 100, 0);
        bottomPipe = new HMovingEntity(pipeSpeed, true, 60 * -1, 0, 100, 0);
        clouds = new ArrayList<>();
        hills = new ArrayList<>();

        this.primaryStage = primaryStage;
        mainMenuScene = primaryStage.getScene();
        //create new Scene for the EndlessMap
        root = new BorderPane();
        mapScene = new Scene(root, screenWidth, screenHeight);
        mapScene.getStylesheets().add("mainMenuStyle.css");
        //create key bindings
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
        //create Canvas to render Entities on
        canvas = new Canvas(screenWidth, screenHeight);
        root.getChildren().add(canvas);
        scoreLabel = new Label("Score :: 0");
        scoreLabel.setFont(new Font("Bodoni MT Black", 30));
        HBox scoreBox = new HBox(scoreLabel);
        scoreBox.setAlignment(Pos.CENTER_RIGHT);
        root.setTop(scoreBox);

        //create the EndlessMap game engine
        gameEngine = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //handle user inputs
                flappyBird.setIsMovingDown(flappyBirdIsFlying == false);
                //update FlappyBird position
                flappyBird.update();
                //adjust FlappyBird offest
                if (flappyBird.getPosX() < screenWidth / 3) {
                    flappyBird.setPosX(flappyBird.getPosX() + 2);
                }

                generateScenery();
                generatePipes(now);

                //update entity positions
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
                if (debuff != null) {
                    debuff.setCurrentTime(now);
                    debuff.update();
                    //create child debuffs in certain situations
                    if (requestChildrenDebuffs(debuff.getDebuff())) {
                        childDebuffs = debuff.createChildrenDebuffs();
                    }
                    if (childDebuffs != null) {
                        for (DebuffEntity childDebuffEntity : childDebuffs) {
                            childDebuffEntity.update();
                        }
                    }
                }

                //check game logic
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
                if (debuff != null) {
                    if (childDebuffs != null) {
                        for (DebuffEntity childDebuffEntity : childDebuffs) {
                            childDebuffEntity.render(gc);
                        }
                    }
                    debuff.render(gc);
                }
                topPipe.render(gc);
                bottomPipe.render(gc);
            }
        };
        score = -1;
    }

    /**
     * Gets the {@code FlappyBird} that is loaded on this {@code EndlessMap}
     *
     * @return this {@code EndlessMap}'s {@code FlappyBird}
     */
    public FlappyBird getFlappyBird() {
        return flappyBird;
    }

    /**
     * Gets the {@code Stage} that is being used by this {@code EndlessMap}
     *
     * @return the current {@code Stage} being used by this {@code EndlessMap}
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Scene getMapScene() {
        return mapScene;
    }

    public int getScore() {
        return score;
    }

    public void startMap() {
        if (gameHasStarted) {
            throw new IllegalStateException("FlappyBird Map has already been started");
        }
        gameHasStarted = true;
        primaryStage.setScene(mapScene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        Label mapGameLabel = new Label("Keep Flying! Press W To Move Up\n"
                + "\tPress SPACE To Continue");
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
        restartButton.setDisable(true);
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
        //remove any clouds off the canvas
        for (Iterator<HMovingEntity> iterator = clouds.iterator(); iterator.hasNext();) {
            HMovingEntity next = iterator.next();
            if (next.getPosX() + next.getWidth() <= 0) {
                iterator.remove();
            }
        }
        //remove any hills off the canvas
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
            //create the gap position between the top and bottom pipe
            double gapTopPosY = Math.random() * (screenHeight - 500) + pipeGapLength;
            double gapBottomPosY = gapTopPosY + pipeGapLength;
            //increase pipe speed
            pipeSpeed += .25;
            //create new top and bottom pipes at random positions
            topPipe = new HMovingEntity(pipeSpeed, true,
                    screenWidth, gapTopPosY, topPipe.getWidth(), gapTopPosY, "assets\\default\\top_pipe.png");
            bottomPipe = new HMovingEntity(pipeSpeed, true,
                    screenWidth, screenHeight, bottomPipe.getWidth(), screenHeight - gapBottomPosY, "assets\\default\\bottom_pipe.png");
            hasPassedBottomPipe = false;
            //remove items and debuffs that are off the screen
            item = null;
            debuff = null;
            //30 percent chance to generate an item
            int itemGenerationChance = (int) (Math.random() * 101);
            if (itemGenerationChance >= 70) {
                FlappyBirdItem[] items = FlappyBirdItem.values();
                FlappyBirdItem itemType = items[(int) (Math.random() * (items.length - 2))];
                item = new ItemEntity(itemType, 8, true, now, screenWidth, gapTopPosY, 50, 50, "assets\\default\\random_item_1.png");
            }
            //30 percent chance to generate a debuff after 5 passes
            int debuffGenerationChance = (int) (Math.random() * 101);
            if (debuffGenerationChance >= 70 && score >= 5) {
                FlappyBirdDebuff[] debuffs = FlappyBirdDebuff.values();
                FlappyBirdDebuff debuffType = debuffs[(int) (Math.random() * (debuffs.length - 3))];
                switch (debuffType) {
                    case RAIN_CLOUD_DEBUFF:
                        debuff = new DebuffEntity(debuffType, 10, true, now, screenWidth, 200, 400, 300, "assets\\default\\rain_cloud.png");
                        childDebuffs = debuff.createChildrenDebuffs();
                        Media rainSFX = new Media(new File("assets\\sounds\\rain_sfx.mp3").toURI() + "");
                        MediaPlayer rainSFXPlayer = new MediaPlayer(rainSFX);
                        rainSFXPlayer.setVolume(0.5);
                        rainSFXPlayer.play();
                        break;
                    case BIRD_SWARM_DEBUFF:
                        debuff = new DebuffEntity(debuffType, 12, true, now, screenWidth,
                                gapBottomPosY, flappyBird.getWidth(), flappyBird.getHeight(), "assets\\default\\bird_debuff.gif");
                        childDebuffs = debuff.createChildrenDebuffs();
                        break;
                    case STORM_CLOUD_DEBUFF:
                        debuff = new DebuffEntity(debuffType, 10, true, now, screenWidth, 200, 400, 300, "assets\\default\\storm_cloud.png");
                        Media stormSFX = new Media(new File("assets\\sounds\\storm_cloud_sfx.mp3").toURI() + "");
                        MediaPlayer stormSFXPlayer = new MediaPlayer(stormSFX);
                        stormSFXPlayer.setVolume(0.5);
                        stormSFXPlayer.play();
                        break;
                    default:
                        break;
                }
            }

            //reset buffs
            if (flappyBird.getWidth() == flappyBirdWidth / 2 && flappyBird.getHeight() == flappyBirdHeight / 2) {
                flappyBird.setWidth(flappyBirdWidth);
                flappyBird.setHeight(flappyBirdHeight);
            }
        }
    }

    private void useItem(FlappyBirdItem item) {
        if (item == FlappyBirdItem.DISJOINT_PIPES_BUFF) {
            topPipe.setPosX(topPipe.getPosX() + topPipe.getWidth() * 3);
        } else if (item == FlappyBirdItem.SMALL_FLAPPY_BIRD) {
            flappyBird.setWidth(flappyBird.getWidth() / 2);
            flappyBird.setHeight(flappyBird.getHeight() / 2);
        }
    }

    private void useDebuff(FlappyBirdDebuff debuff) {
        if (debuff == FlappyBirdDebuff.RAIN_DEBUFF) {
            flappyBird.setVelocityY(flappyBird.getVelocityY() - 2);
        } else if (debuff == FlappyBirdDebuff.BIRD_SWARM_DEBUFF || debuff == FlappyBirdDebuff.BIRD_DEBUFF) {
            flappyBird.setPosX(flappyBird.getPosX() + this.debuff.getVelocityX() - 2);
        } else if (debuff == FlappyBirdDebuff.LIGHTNING_DEBUFF) {
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
                    primaryStage.setScene(mainMenuScene);
                    primaryStage.setFullScreen(true);
                    primaryStage.setFullScreenExitHint("");
                    primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
                }
            });
        }
    }

    private boolean requestChildrenDebuffs(FlappyBirdDebuff debuff) {
        if (debuff == FlappyBirdDebuff.STORM_CLOUD_DEBUFF) {
            if (this.debuff.getPosX() % (screenWidth / 4) == 0 && this.debuff.getPosX() > 0) {
                return true;
            }
        }
        return false;
    }

    private void executeGameLogic() {
        //if the FlappyBird passes the bottom pipe, add a point to the score
        if (flappyBird.getPosX() > bottomPipe.getPosX() + bottomPipe.getWidth() && hasPassedBottomPipe == false) {
            score++;
            hasPassedBottomPipe = true;
            scoreLabel.setText("Score :: " + score);
        }
        //if the FlappyBird hits an item, use the item
        if (item != null && flappyBird.intersects(item)) {
            useItem(item.getItem());
            item = null;
        }
        //if the FlappyBird hits a debuff or child debuff, use the debuff
        if (debuff != null) {
            if (flappyBird.intersects(debuff)) {
                useDebuff(debuff.getDebuff());
            }
            if (childDebuffs != null) {
                for (DebuffEntity childDebuffEntity : childDebuffs) {
                    if (flappyBird.intersects(childDebuffEntity)) {
                        useDebuff(childDebuffEntity.getDebuff());
                    }
                }
            }
        }
        //check for extended intersections
        boolean isIntersectingExtendedTopPipe = flappyBird.getPosY() < 0
                && topPipe.getPosX() < flappyBird.getPosX() && flappyBird.getPosX() < topPipe.getPosX() + topPipe.getWidth();
        boolean isIntersectingExtendedBottomPipe = flappyBird.getPosY() > screenHeight
                && bottomPipe.getPosX() < flappyBird.getPosX() && flappyBird.getPosX() < bottomPipe.getPosX() + bottomPipe.getWidth();
        //if the FlappyBird hits a pipe, end the game
        if (flappyBird.intersects(topPipe) || flappyBird.intersects(bottomPipe)
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
                    //set the background to the simulation background
                    root.getChildren().clear();
                    SimulationMap backgroundSimulation = new SimulationMap(screenWidth, screenHeight, "assets\\default");
                    root.getChildren().add(backgroundSimulation.getCanvas());
                    //check to see if a high score needs to be inserted
                    int highScoreSpot = insertHighScore();
                    if (highScoreSpot > -1) {
                        deathSoundPlayer.stop();
                        endGameSoundPlayer.stop();
                        Media highScoreSound = new Media(new File("assets\\sounds\\high_score_sfx.mp3").toURI() + "");
                        MediaPlayer highScoreSoundPlayer = new MediaPlayer(highScoreSound);
                        highScoreSoundPlayer.play();
                        Label highScoreLabel = new Label("\t\t    NEW HIGH SCORE\nEnter Your Name and Press ENTER to Continue");
                        highScoreLabel.setTextFill(Color.YELLOW);
                        highScoreLabel.setFont(new Font("Bodoni MT Black", 20));
                        TextField highScoreNameTextField = new TextField();
                        highScoreNameTextField.setMaxWidth(screenWidth / 6);
                        VBox enterHighScoreVBox = new VBox(highScoreLabel, highScoreNameTextField);
                        enterHighScoreVBox.setAlignment(Pos.CENTER);
                        root.setCenter(enterHighScoreVBox);
                        mapScene.setOnKeyPressed((keyEvent) -> {
                            if (keyEvent.getCode() == KeyCode.ENTER) {
                                String[] currentHighScores = writeNewHighScores(highScoreSpot,
                                        highScoreNameTextField.getText(), getHighScores());
                                root.setCenter(null);
                                displayHighScores(currentHighScores);
                            }
                        });
                    } else {
                        displayHighScores(getHighScores());
                    }
                }
            });
        }
    }

    private void displayHighScores(String[] highScores) {
        Label highScoresLabel = new Label("High Scores");
        highScoresLabel.setFont(new Font("Arial Rounded MT Bold", 40));
        Label[] highScoresLabels = new Label[5];
        for (int i = 0; i < highScores.length; i++) {
            highScoresLabels[i] = new Label(highScores[i]);
            highScoresLabels[i].setFont(new Font("Bodoni MT Black", 20));
        }
        VBox highScoreVBox = new VBox(screenHeight / 10, highScoresLabel);
        highScoreVBox.getChildren().addAll(highScoresLabels);
        Button backToMainMenuButton = new Button("Exit to Main Menu");
        backToMainMenuButton.setOnAction((event) -> {
            primaryStage.setScene(mainMenuScene);
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        });
        backToMainMenuButton.setOnMouseEntered((event) -> {
            Media buttonEnteredSound = new Media(new File("assets\\sounds\\button_entered.mp3").toURI() + "");
            MediaPlayer buttonEnteredSoundPlayer = new MediaPlayer(buttonEnteredSound);
            buttonEnteredSoundPlayer.setVolume(0.5);
            buttonEnteredSoundPlayer.play();
            backToMainMenuButton.setPrefSize(backToMainMenuButton.getWidth() * 1.25, backToMainMenuButton.getHeight() * 1.25);
        });
        backToMainMenuButton.setOnMouseExited((event) -> {
            backToMainMenuButton.setPrefSize(backToMainMenuButton.getWidth() / 1.25, backToMainMenuButton.getHeight() / 1.25);
        });
        Button playAgainButton = new Button("Play Again");
        playAgainButton.setOnMouseEntered((event) -> {
            Media buttonEnteredSound = new Media(new File("assets\\sounds\\button_entered.mp3").toURI() + "");
            MediaPlayer buttonEnteredSoundPlayer = new MediaPlayer(buttonEnteredSound);
            buttonEnteredSoundPlayer.setVolume(0.5);
            buttonEnteredSoundPlayer.play();
            playAgainButton.setPrefSize(playAgainButton.getWidth() * 1.25, playAgainButton.getHeight() * 1.25);
        });
        playAgainButton.setOnMouseExited((event) -> {
            playAgainButton.setPrefSize(playAgainButton.getWidth() / 1.25, playAgainButton.getHeight() / 1.25);
        });
        HBox playAgainHBox = new HBox(10, playAgainButton, backToMainMenuButton);
        playAgainHBox.setAlignment(Pos.CENTER);
        highScoreVBox.getChildren().add(playAgainHBox);
        highScoreVBox.setAlignment(Pos.CENTER);
        root.setCenter(highScoreVBox);
    }

    private int insertHighScore() {
        String[] highScores = getHighScores();
        for (int i = 0; i < highScores.length; i++) {
            if (highScores[i].equals("[Empty]")) {
                return i;
            } else {
                String[] highScore = highScores[i].split("\\s");
                if (score > Integer.parseInt(highScore[highScore.length - 1])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Gets the high scores from previous Endless Maps
     *
     * @return an array of high scores from previous games
     */
    private String[] getHighScores() {
        String[] highScores = new String[5];
        try {
            Scanner scanner = new Scanner(new File("highscores\\endless.dat"));
            int highScoreSpot = 0;
            while (scanner.hasNextLine()) {
                highScores[highScoreSpot++] = scanner.nextLine();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not load high scores data file");
        }
        return highScores;
    }

    /**
     * Writes new high scores with the current high score being inserted into
     * the top 5 high scores
     *
     * @param highScoreSpot the high spot the high score is being inserted at
     * @param name the name of the player who got the high score
     * @param highScores the original list of high scores
     * @return an array of the new high score list
     */
    private String[] writeNewHighScores(int highScoreSpot, String name, String[] highScores) {
        String[] currentHighScores = new String[5];
        //load the higher scores
        for (int i = 0; i < highScoreSpot; i++) {
            currentHighScores[i] = highScores[i];
        }
        //add the new high score in
        currentHighScores[highScoreSpot] = name + " :: " + score;
        //push the old high scores back
        for (int i = highScoreSpot; i < currentHighScores.length - 1; i++) {
            currentHighScores[i + 1] = highScores[i];
        }

        //write the new high scores to the file
        try {
            PrintWriter pw = new PrintWriter("highscores\\endless.dat");
            for (String line : currentHighScores) {
                pw.println(line);
            }
            pw.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not write high scores to highscores\\endless.dat");
        }
        return currentHighScores;
    }

}
