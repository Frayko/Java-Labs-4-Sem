package LaboratoryWork.Habitat.Fish;

import LaboratoryWork.Habitat.Fish.Objects.GoldenFish;
import LaboratoryWork.Habitat.Fish.Objects.GuppiesFish;

public class FishCreator {
    public FishCreator() {}

    public Fish createFish(FishTypes type, int x, int y) {
        Fish toReturn;
        switch(type) {
            case GoldenFish -> toReturn = new GoldenFish(x, y);
            case GuppiesFish -> toReturn = new GuppiesFish(x, y);
            default -> throw new IllegalArgumentException("Wrong fish type: " + type);
        }

        return toReturn;
    }
}