package LaboratoryWork.Habitat;

import java.awt.*;
import java.util.Vector;

public class FishArray {
    private static volatile FishArray fishArray;
    private static final Vector<Fish> list = new Vector<>();

    private FishArray() {}

    public static synchronized FishArray getFishArray() {
        if(fishArray == null)
            fishArray = new FishArray();
        return fishArray;
    }

    public synchronized void addFish(Fish fish) {
        list.addElement(fish);
    }

    public synchronized void drawFishes(Graphics g) {
        for(Fish fish : list) {
            fish.drawImage(g);
        }
    }

    public synchronized void removeAllFishes() {
        list.removeAllElements();
    }
}
