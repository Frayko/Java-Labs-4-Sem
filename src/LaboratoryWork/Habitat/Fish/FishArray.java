package LaboratoryWork.Habitat.Fish;

import LaboratoryWork.Habitat.Fish.Objects.GoldenFish;
import LaboratoryWork.Habitat.Fish.Objects.GuppiesFish;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.Map;

public class FishArray {
    private static volatile FishArray fishArray;
    private static volatile LinkedList<Fish> list = new LinkedList<>();
    private static HashSet<Integer> set = new HashSet<>();
    private static TreeMap<Integer, Double> map = new TreeMap<>();
    private static int countGoldenFishCreated;
    private static int countGuppiesFishCreated;

    private FishArray() {
        countGoldenFishCreated = 0;
        countGuppiesFishCreated = 0;
    }

    public static synchronized FishArray getFishArray() {
        if (fishArray == null) {
            fishArray = new FishArray();
        }
        return fishArray;
    }

    public synchronized void addFish(Fish fish, double bornTime) {
        if(fish instanceof GoldenFish)
            countGoldenFishCreated++;
        else
            countGuppiesFishCreated++;

        int id = fish.hashCode();
        list.add(fish);
        set.add(id);
        map.put(id, bornTime);
    }

    public synchronized void setFishes(LinkedList<Fish> fishes, double bornTime) {
        for(Fish fish : fishes) {
            if(fish.getReverse() && fish instanceof GoldenFish && bornTime == 0.0)
                fish.reverseImageWidth();
            addFish(fish, bornTime);
        }
    }

    public synchronized void moveGoldenFishes(int dX, int dY, int windowWidth) {
        for(Fish fish : list) {
            if (fish instanceof GoldenFish) {
                if (fish.getX() + fish.getImageWidth() >= windowWidth) {
                    fish.reverseImageWidth();
                    fish.setX(fish.getX() - fish.getImageWidth());
                    fish.setReverse(true);
                } else if (fish.getX() + fish.getImageWidth() <= 0) {
                    fish.reverseImageWidth();
                    fish.setX(fish.getX() - fish.getImageWidth());
                    fish.setReverse(false);
                }

                if (fish.getReverse()) {
                    fish.move(-dX, dY);
                } else {
                    fish.move(dX, dY);
                }
            }
        }
    }

    public synchronized void moveGuppiesFishes(int dX, int dY, int windowHeight) {
        for(Fish fish : list) {
            if (fish instanceof GuppiesFish) {
                if (fish.getY() + fish.getImageHeight() >= windowHeight) {
                    fish.setReverse(true);
                } else if (fish.getY() <= 0) {
                    fish.setReverse(false);
                }

                if (fish.getReverse()) {
                    fish.move(dX, -dY);
                } else {
                    fish.move(dX, dY);
                }
            }
        }
    }

    public synchronized void checkTTL(double currentTime, double goldenTTL, double guppiesTTL) {
        LinkedList<Fish> delFishes = new LinkedList<>();

        for(Fish fish : list) {
            double TTL = fish instanceof GoldenFish ? goldenTTL : guppiesTTL;
            for (Map.Entry<Integer, Double> entry : map.entrySet()) {
                if (entry.getKey() == fish.hashCode() && entry.getValue() + TTL <= currentTime) {
                    delFishes.add(fish);
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
        countGoldenFishCreated = 0;
        countGuppiesFishCreated = 0;
        list.clear();
        set.clear();
        map.clear();
    }

    public synchronized LinkedList<Fish> getList() {
        return list;
    }
    public synchronized TreeMap<Integer, Double> getMap() {
        return map;
    }

    public synchronized int getAllFishCount() {
        return countGoldenFishCreated + countGuppiesFishCreated;
    }

    public synchronized int getFishCount(FishTypes type) {
        int toReturn;
        switch(type) {
            case GoldenFish -> toReturn = countGoldenFishCreated;
            case GuppiesFish -> toReturn = countGuppiesFishCreated;
            default -> throw new IllegalArgumentException("Wrong fish type: " + type);
        }
        return toReturn;
    }
}