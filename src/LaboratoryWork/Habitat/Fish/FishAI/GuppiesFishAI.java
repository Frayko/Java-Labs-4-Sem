package LaboratoryWork.Habitat.Fish.FishAI;

import LaboratoryWork.Habitat.Fish.FishArray;

public class GuppiesFishAI extends BaseAI {
    public GuppiesFishAI() { Thread.currentThread().setName("GuppiesFishAI"); }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                FishArray.getFishArray().moveGuppiesFishes(dX, dY, windowHeight);
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
