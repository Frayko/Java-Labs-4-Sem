package LaboratoryWork.Habitat;

import LaboratoryWork.Habitat.Objects.GoldenFish;

import java.awt.*;
import java.util.*;

public class FishArray {
    private static FishArray fishArray;
    private static LinkedList<Fish> list = new LinkedList<>();
    private static HashSet<Integer> set = new HashSet<>();
    private static TreeMap<Integer, Double> map = new TreeMap<>();

    private FishArray() {}

    public static FishArray getFishArray() {
        if(fishArray == null)
            fishArray = new FishArray();
        return fishArray;
    }

    public void addFish(Fish fish, double bornTime) {
        int id = fish.hashCode();
        list.add(fish);
        set.add(id);
        map.put(id, bornTime);
    }

    public void checkTTL(double currentTime, double goldenTTL, double guppiesTTL) {
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

    public void drawFishes(Graphics g) {
        for(Fish fish : list) {
            fish.drawImage(g);
        }
    }

    public void removeAllFishes() {
        list.clear();
        set.clear();
        map.clear();
    }

    public LinkedList<Fish> getList() { return list; }
    public TreeMap<Integer, Double> getMap() { return map; }
}
