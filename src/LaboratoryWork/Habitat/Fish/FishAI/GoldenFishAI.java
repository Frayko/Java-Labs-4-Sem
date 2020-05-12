package LaboratoryWork.Habitat.Fish.FishAI;

import LaboratoryWork.Habitat.Fish.FishArray;

public class GoldenFishAI extends BaseAI {
    public GoldenFishAI() { Thread.currentThread().setName("GoldenFishAI"); }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                FishArray.getFishArray().moveGoldenFishes(dX, dY, windowWidth);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            synchronized (this) {
                while(!isRunning) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
