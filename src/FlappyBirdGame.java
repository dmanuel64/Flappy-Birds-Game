/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.glass.ui.Screen;
import com.sun.prism.paint.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author dylan
 */
public class FlappyBirdGame extends Application {

    public static final double SCREEN_WIDTH = Screen.getMainScreen().getWidth();
    public static final double SCREEN_HEIGHT = Screen.getMainScreen().getHeight();

    private VBox shopRoot;
    private String currentSkin = "flappy_bird";

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene menuScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        menuScene.getStylesheets().add("mainMenuStyle.css");
        SimulationMap backgroundSimulation = new SimulationMap(SCREEN_WIDTH, SCREEN_HEIGHT, "assets\\default");
        root.getChildren().add(backgroundSimulation.getCanvas());

        Label titleLabel = new Label("Flying Birds");
        titleLabel.setFont(new Font("Arial Rounded MT Bold", 60));
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        root.setTop(titleBox);
        VBox menuPane = new VBox(SCREEN_HEIGHT / 2);
        menuPane.setAlignment(Pos.CENTER_LEFT);
        root.setLeft(menuPane);
        Button playGameButton = new Button("Play");
        playGameButton.setOnAction((event) -> {
            Media buttonClickedSound = new Media(new File("assets\\sounds\\button_clicked.mp3").toURI() + "");
            MediaPlayer buttonClickedSoundPlayer = new MediaPlayer(buttonClickedSound);
            buttonClickedSoundPlayer.setVolume(0.5);
            buttonClickedSoundPlayer.play();
            VBox playGamePane = new VBox((SCREEN_HEIGHT - titleLabel.getHeight()) / 4);
            playGamePane.setAlignment(Pos.CENTER_LEFT);
            root.setLeft(playGamePane);
            Button selectLevelButton = new Button("Select Level");
            selectLevelButton.setOnAction((buttonEvent) -> {
                root.setCenter(null);
                root.setRight(null);
                MediaPlayer selectLevelClickedSoundPlayer = new MediaPlayer(buttonClickedSound);
                selectLevelClickedSoundPlayer.setVolume(0.5);
                selectLevelClickedSoundPlayer.play();
                Button backButton = new Button("Back");
                backButton.setOnAction((onClickEvent) -> {
                    MediaPlayer backButtonClickedSoundPlayer = new MediaPlayer(buttonClickedSound);
                    backButtonClickedSoundPlayer.setVolume(0.5);
                    backButtonClickedSoundPlayer.play();
                    root.setLeft(playGamePane);
                    root.setCenter(null);
                });
                root.setLeft(backButton);
                GridPane levelSelectionPane = new GridPane();
                Button[][] levelButtons = new Button[5][5];
                for (int i = 0; i < levelButtons.length; i++) {
                    for (int j = 0; j < levelButtons[i].length; j++) {
                        int world = i + 1;
                        int level = j + 1;
                        levelButtons[i][j] = new Button("Level " + world + " - " + level);
                        levelButtons[i][j].setOnAction((mapButtonEvent) -> {
                            LoadedFileMap fmfMap = new LoadedFileMap(currentSkin, primaryStage,
                                    "maps\\world" + world + "_level" + level + ".fmf");
                            fmfMap.startMap();
                        });
                    }
                }
                String highestUnlockedLevel[] = getHighestUnlockedLevel().split("-");
                int highestWorld = Integer.parseInt(highestUnlockedLevel[0]);
                int highestLevel = Integer.parseInt(highestUnlockedLevel[1]);
                for (int i = highestLevel; i < levelButtons[highestWorld - 1].length; i++) {
                    levelButtons[highestWorld - 1][i].setDisable(true);
                }
                for (int i = highestWorld; i < levelButtons.length; i++) {
                    for (int j = 0; j < levelButtons[i].length; j++) {
                        levelButtons[i][j].setDisable(true);
                    }
                }

                for (int i = 0; i < levelButtons.length; i++) {
                    levelSelectionPane.addRow(i, levelButtons[i]);
                }
                levelSelectionPane.setVgap(SCREEN_HEIGHT / 25);
                levelSelectionPane.setHgap(SCREEN_WIDTH / 25);
                levelSelectionPane.setAlignment(Pos.CENTER);
                root.setCenter(levelSelectionPane);
            });
            playGamePane.getChildren().add(selectLevelButton);
            Button playEndlessGameButton = new Button("Endless Mode");
            playEndlessGameButton.setOnAction((buttonEvent) -> {
                root.setCenter(null);
                root.setRight(null);
                new EndlessMap(currentSkin, primaryStage).startMap();
            });
            playGamePane.getChildren().add(playEndlessGameButton);
            Button customizeButton = new Button("Customize");
            customizeButton.setOnAction((buttonEvent) -> {
                initCustomizationPane();
                root.setCenter(shopRoot);
                Image coinImage = null;
                try {
                    coinImage = new Image(new FileInputStream("assets\\default\\coin.png"), 50, 50, false, true);
                } catch (FileNotFoundException e) {
                    System.err.println("Could not load \"assets\\default\\coin.png\"");
                }
                ImageView coinImageView = new ImageView(coinImage);
                Label coinsLabel = new Label(getCoins() + "");
                coinsLabel.setFont(new Font("Bodoni MT Black", 30));
                VBox coinViewVBox = new VBox(coinImageView, coinsLabel);
                root.setRight(coinViewVBox);
            });
            playGamePane.getChildren().add(customizeButton);
            Button backButton = new Button("Back");
            backButton.setOnAction((buttonEvent) -> {
                MediaPlayer backButtonClickedSoundPlayer = new MediaPlayer(buttonClickedSound);
                backButtonClickedSoundPlayer.setVolume(0.5);
                backButtonClickedSoundPlayer.play();
                root.setCenter(null);
                root.setRight(null);
                root.setLeft(menuPane);
            });
            playGamePane.getChildren().add(backButton);
            for (Node node : playGamePane.getChildren()) {
                if (node instanceof Button) {
                    Button button = (Button) node;
                    button.setPrefWidth(SCREEN_WIDTH / 8);
                    button.setOnMouseEntered((buttonEvent) -> {
                        Media buttonEnteredSound = new Media(new File("assets\\sounds\\button_entered.mp3").toURI() + "");
                        MediaPlayer buttonEnteredSoundPlayer = new MediaPlayer(buttonEnteredSound);
                        buttonEnteredSoundPlayer.setVolume(0.5);
                        buttonEnteredSoundPlayer.play();
                        button.setPrefSize(button.getWidth() * 1.25, button.getHeight() * 1.25);
                    });
                    button.setOnMouseExited((buttonEvent) -> {
                        button.setPrefSize(button.getWidth() / 1.25, button.getHeight() / 1.25);
                    });
                }
            }
        });
        menuPane.getChildren().add(playGameButton);
        Button exitButton = new Button("Exit to Desktop");
        exitButton.setOnAction((event) -> {
            primaryStage.close();
        });
        menuPane.getChildren().add(exitButton);
        for (Node node : menuPane.getChildren()) {
            if (node instanceof Button) {
                Button b = (Button) node;
                b.setPrefWidth(SCREEN_WIDTH / 6);
                b.setOnMouseEntered((event) -> {
                    Media buttonEnteredSound = new Media(new File("assets\\sounds\\button_entered.mp3").toURI() + "");
                    MediaPlayer buttonEnteredSoundPlayer = new MediaPlayer(buttonEnteredSound);
                    buttonEnteredSoundPlayer.setVolume(0.5);
                    buttonEnteredSoundPlayer.play();
                    b.setPrefSize(b.getWidth() * 1.25, b.getHeight() * 1.25);
                });
                b.setOnMouseExited((event) -> {
                    b.setPrefSize(b.getWidth() / 1.25, b.getHeight() / 1.25);
                });
            }
        }

        primaryStage.setTitle("Flappy Birds");
        primaryStage.setScene(menuScene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.show();
    }

    private void initCustomizationPane() {
        shopRoot = new VBox();
        shopRoot.setAlignment(Pos.CENTER);
        shopRoot.setMaxSize(SCREEN_WIDTH / 1.5, SCREEN_HEIGHT / 1.5);
        List<String> unlockedSkinsAndTrails = getUnlockedSkinsAndTrails();
        Tab shopTab = new Tab("Shop");
        shopTab.setClosable(false);
        GridPane shopSkinsPane = new GridPane();
        int currentRow = 0;
        int currentCol = 0;
        ArrayList<String> skins = getShopSkins();
        for (String shopSkin : skins) {
            String[] shopData = shopSkin.split("\\s");
            String shopImageName = shopData[0];
            int skinPrice = Integer.parseInt(shopData[1]);
            Image skinItemImage = null;
            try {
                skinItemImage = new Image(new FileInputStream("assets\\skins\\" + shopImageName + ".gif"), 50, 50, false, true);
            } catch (FileNotFoundException ex) {
                System.err.println("Cannot load FlappyBird asset \"" + shopImageName + ".gif\"");
            }
            ImageView unlockedItemPreview = new ImageView(skinItemImage);
            String itemName = shopImageName.replaceAll("_", " ");
            Button purchaseItemButton = new Button(itemName + " " + skinPrice + " Coins");
            purchaseItemButton.setOnAction((event) -> {
                addSkin(skinPrice, shopImageName);
                purchaseItemButton.setDisable(true);
            });
            if (getCoins() - skinPrice < 0 || unlockedSkinsAndTrails.contains(shopImageName)) {
                purchaseItemButton.setDisable(true);
            }

            VBox itemVBox = new VBox(unlockedItemPreview, purchaseItemButton);
            shopSkinsPane.add(itemVBox, currentRow, currentCol);
            if (++currentCol > 2) {
                currentCol = 0;
                currentRow++;
            }
        }
        ScrollPane shopScrollPane = new ScrollPane(shopSkinsPane);

        Tab unlocksTab = new Tab("Unlocked");
        unlocksTab.setClosable(false);
        GridPane unlocksSkinsPane = new GridPane();
        currentRow = 0;
        currentCol = 0;
        for (String unlockedItem : unlockedSkinsAndTrails) {
            Image unlockedItemImage = null;
            try {
                unlockedItemImage = new Image(new FileInputStream("assets\\skins\\" + unlockedItem + ".gif"), 50, 50, false, true);
            } catch (FileNotFoundException ex) {
                System.err.println("Cannot load FlappyBird asset \"" + unlockedItem + ".gif\"");
            }
            ImageView unlockedItemPreview = new ImageView(unlockedItemImage);
            String itemName = unlockedItem.replaceAll("_", " ");
            Button equipItemButton = new Button(itemName);
            equipItemButton.setOnAction((event) -> {
                currentSkin = unlockedItem;
                equipItemButton.setDisable(true);
            });
            VBox itemVBox = new VBox(unlockedItemPreview, equipItemButton);
            unlocksSkinsPane.add(itemVBox, currentRow, currentCol);
            if (++currentCol > 2) {
                currentCol = 0;
                currentRow++;
            }
        }
        ScrollPane unlocksScrollPane = new ScrollPane(unlocksSkinsPane);
        TabPane shopTabPane = new TabPane(shopTab, unlocksTab);
        shopTabPane.setTabMinWidth(SCREEN_WIDTH / 3.5);

        shopTab.setOnSelectionChanged((event) -> {
            shopRoot.getChildren().removeIf((t) -> {
                return t instanceof TabPane == false;
            });
            shopRoot.getChildren().add(shopScrollPane);
        });
        unlocksTab.setOnSelectionChanged((event) -> {
            shopRoot.getChildren().removeIf((t) -> {
                return t instanceof TabPane == false;
            });
            shopRoot.getChildren().add(unlocksScrollPane);
        });
        shopRoot.getChildren().addAll(shopTabPane, shopScrollPane);
    }

    private List<String> getUnlockedSkinsAndTrails() {
        List<String> unlockedSkins = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("current_load.dat"));
            scanner.nextLine();
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                unlockedSkins.add(scanner.nextLine());
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Cannot load \"current_load.dat\"");
        }
        return unlockedSkins;
    }

    private int getCoins() {
        try {
            Scanner scanner = new Scanner(new File("current_load.dat"));
            scanner.nextLine();
            return scanner.nextInt();
        } catch (FileNotFoundException ex) {
            System.err.println("Cannot load \"current_load.dat\"");
        }
        return 0;
    }

    private String getHighestUnlockedLevel() {
        try {
            Scanner scanner = new Scanner(new File("current_load.dat"));
            return scanner.nextLine();
        } catch (FileNotFoundException ex) {
            System.err.println("Cannot load \"current_load.dat\"");
        }
        return "";
    }

    private ArrayList<String> getShopSkins() {
        ArrayList<String> skins = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("shop_prices.dat"));
            while (scanner.hasNextLine()) {
                skins.add(scanner.nextLine());
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Cannot load \"shop_prices.dat\"");
        }
        return skins;
    }

    private void addSkin(int skinPrice, String skin) {
        //load the current load
        String nextLevel = "";
        int coinCount = 0;
        ArrayList<String> currentLoad = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("current_load.dat"));
            nextLevel = scanner.nextLine();
            coinCount = scanner.nextInt();
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                currentLoad.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot load \"current_load.dat\"");
        }
        //write the coin count to the current load
        try {
            PrintWriter pw = new PrintWriter("current_load.dat");
            pw.println(nextLevel);
            pw.println(skinPrice - coinCount);
            for (String currentLoadLine : currentLoad) {
                pw.println(currentLoadLine);
            }
            pw.println(skin);
            pw.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not write coin count to \"current_load.dat\"");
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
