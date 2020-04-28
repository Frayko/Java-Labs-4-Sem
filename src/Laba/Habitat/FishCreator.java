package Laba.Habitat;

import Laba.Habitat.Objects.GoldenFish;
import Laba.Habitat.Objects.GuppiFish;

public class FishCreator {
    private int GoldenFishCount;
    private int GuppiFishCount;

    FishCreator() {
        GoldenFishCount = 0;
        GuppiFishCount = 0;
    }

    public Fish createFish(FishTypes type, int x, int y) {
        Fish toReturn;
        switch (type) {
            case GoldenFish: {
                toReturn = new GoldenFish(x, y);
                GoldenFishCount++;
                break;
            }
            case GuppiFish: {
                toReturn = new GuppiFish(x, y);
                GuppiFishCount++;
                break;
            }
            default: {
                throw new IllegalArgumentException("Wrong fish type: " + type);
            }
        }
        return toReturn;
    }

    public void resetFishs() {
        GoldenFishCount = 0;
        GuppiFishCount = 0;
    }

    public int getAllFishCount() {
        return GoldenFishCount + GuppiFishCount;
    }

    public int getFishCount(FishTypes type) {
        int toReturn;
        switch (type) {
            case GoldenFish: {
                toReturn = GoldenFishCount;
                break;
            }
            case GuppiFish: {
                toReturn = GuppiFishCount;
                break;
            }
            default: {
                throw new IllegalArgumentException("Wrong fish type: " + type);
            }
        }
        return toReturn;
    }

}