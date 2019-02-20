
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dylan
 */
public class MapAddition {

    public static final byte INFINITE_RAIN = 0b1;
    public static final byte RISING_WATER = 0b10;
    public static final byte MOVING_FOG = 0b100;

    private final byte mapAdditionData;
    private final double screenWidth;
    private final double screenHeight;

    private DebuffEntity rain;
    private Entity risingWater;
    private HMovingEntity movingFog;

    public MapAddition(byte mapAdditionData, double screenWidth, double screenHeight) {
        this.mapAdditionData = mapAdditionData;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        createEntities();
    }

    private void createEntities() {
        if ((mapAdditionData & INFINITE_RAIN) == INFINITE_RAIN) {
            rain = new DebuffEntity(FlappyBirdDebuff.RAIN_DEBUFF, 0,
                    true, 1, 0, screenHeight, screenWidth, screenHeight, "assets\\default\\rain.gif");
        }
        if ((mapAdditionData & RISING_WATER) == RISING_WATER) {
            risingWater = new Entity(0, screenHeight, screenWidth, screenHeight / 6);
        }
        if ((mapAdditionData & MOVING_FOG) == MOVING_FOG) {
            double posY = Math.random() * screenHeight;
            movingFog = new HMovingEntity(5, true, screenWidth, posY, screenWidth, screenHeight / 2, "assets\\default\\cloud.png");
        }
    }

    public byte getMapAdditionData() {
        return mapAdditionData;
    }

    public ArrayList<Entity> getEntities() {
        ArrayList<Entity> entities = new ArrayList<>();
        if (rain != null) {
            entities.add(rain);
        }
        if (risingWater != null) {
            entities.add(risingWater);
        }
        if (movingFog != null) {
            entities.add(movingFog);
        }
        return entities;
    }

    public void executeAdditionTask(double... mapAdditionArgs) {
        if (risingWater != null) {
            if (mapAdditionArgs.length == 0) {
                throw new IllegalArgumentException("missing rising water argument");
            }
            risingWater.setHeight(risingWater.getHeight() + mapAdditionArgs[0]);
        }
        if (movingFog != null) {
            //reset fog position if it is off the screen
            if (movingFog.getPosX() + movingFog.getWidth() < 0) {
                movingFog.setPosX(screenWidth);
                double posY = Math.random() * screenHeight;
                movingFog.setPosY(posY);
            }
            movingFog.update();
        }
    }

}
