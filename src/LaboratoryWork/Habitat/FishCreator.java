package LaboratoryWork.Habitat;

import LaboratoryWork.Habitat.Objects.GoldenFish;
import LaboratoryWork.Habitat.Objects.GuppiesFish;

public class FishCreator {
    private int GoldenFishCount;
    private int GuppiesFishCount;

    FishCreator() {
        GoldenFishCount = 0;
        GuppiesFishCount = 0;
    }

    public Fish createFish(FishTypes type, int x, int y) {
        Fish toReturn;
        switch (type) {
            case GoldenFish -> {
                toReturn = new GoldenFish(x, y);
                GoldenFishCount++;
            }
            case GuppiesFish -> {
                toReturn = new GuppiesFish(x, y);
                GuppiesFishCount++;
            }
            default -> throw new IllegalArgumentException("Wrong fish type: " + type);
        }
        return toReturn;
    }

    public void resetFishes() {
        GoldenFishCount = 0;
        GuppiesFishCount = 0;
    }

    public int getAllFishCount() {
        return GoldenFishCount + GuppiesFishCount;
    }

    public int getFishCount(FishTypes type) {
        int toReturn;
        switch (type) {
            case GoldenFish -> toReturn = GoldenFishCount;
            case GuppiesFish -> toReturn = GuppiesFishCount;
            default -> throw new IllegalArgumentException("Wrong fish type: " + type);
        }
        return toReturn;
    }
}