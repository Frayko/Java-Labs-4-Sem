package Laba;

import Laba.Habitat.Habitat;
import Laba.Application.Application;

public class Main {
    public static void main(String[] args) {
        int N1 = 500;        //0.5
        int N2 = 800;        //0.8
        double P1 = 0.3;
        double P2 = 0.2;
        int Period = 100;

        final Habitat h = new Habitat(N1, N2, P1, P2, Period);
        final Application app = new Application(800, 600, h);
    }
}
