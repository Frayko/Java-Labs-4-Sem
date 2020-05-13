package LaboratoryWork.Habitat.Fish;

import LaboratoryWork.Habitat.Fish.Objects.GoldenFish;
import LaboratoryWork.Habitat.Fish.Objects.GuppiesFish;

public class FishCreator {
    private int GoldenFishCount = 0;
    private int GuppiesFishCount = 0;

    public FishCreator() {}

    public Fish createFish(FishTypes type, int x, int y) {
        Fish toReturn;
        switch(type) {
            case GoldenFish -> {
                toReturn = new GoldenFish(x, y);
                ++this.GoldenFishCount;
            }
            case GuppiesFish -> {
                toReturn = new GuppiesFish(x, y);
                ++this.GuppiesFishCount;
            }
            default -> throw new IllegalArgumentException("Wrong fish type: " + type);
        }

        return toReturn;
    }

    public void resetFishes() {
        this.GoldenFishCount = 0;
        this.GuppiesFishCount = 0;
    }

    public int getAllFishCount() {
        return this.GoldenFishCount + this.GuppiesFishCount;
    }

    public int getFishCount(FishTypes type) {
        int toReturn;
        switch(type) {
            case GoldenFish -> toReturn = this.GoldenFishCount;
            case GuppiesFish -> toReturn = this.GuppiesFishCount;
            default -> throw new IllegalArgumentException("Wrong fish type: " + type);
        }
        return toReturn;
    }
}
