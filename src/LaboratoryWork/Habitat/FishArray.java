package LaboratoryWork.Habitat;

import LaboratoryWork.Habitat.Objects.GoldenFish;

import java.awt.*;
import java.util.*;

public class FishArray {
    private static volatile FishArray fishArray;
    private static volatile LinkedList<Fish> list = new LinkedList<>();
    private static HashSet<Integer> set = new HashSet<>();
    private static TreeMap<Integer, Double> map = new TreeMap<>();

    private FishArray() {}

    public static synchronized FishArray getFishArray() {
        if(fishArray == null)
            fishArray = new FishArray();
        return fishArray;
    }

    public synchronized void addFish(Fish fish, double bornTime) {
        int id = fish.hashCode();
        list.add(fish);
        set.add(id);
        map.put(id, bornTime);
    }

    public synchronized void checkTTL(double currentTime, double goldenTTL, double guppiesTTL) {
        LinkedList<Fish> delFishes = new LinkedList<>();

        for(Fish fish : list) {
            double TTL = fish instanceof GoldenFish ? goldenTTL : guppiesTTL;
            for(Map.Entry<Integer, Double> entry : map.entrySet()) {
                if(entry.getKey() == fish.hashCode()) {
                    if ((entry.getValue() + TTL) <= currentTime) {
                        delFishes.add(fish);
                    }
                }
            }
        }

        for(Fish fish : delFishes) {
            list.remove(fish);
            set.remove(fish.hashCode());
            map.remove(fish.hashCode());
        }
    }

    public synchronized void drawFishes(Graphics g) {
        for(Fish fish : list) {
            fish.drawImage(g);
        }
    }

    public synchronized void removeAllFishes() {
        list.clear();
        set.clear();
        map.clear();
    }

    public synchronized LinkedList<Fish> getList() { return list; }
    public synchronized TreeMap<Integer, Double> getMap() { return map; }
}
