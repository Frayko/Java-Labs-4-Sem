package LaboratoryWork;

import LaboratoryWork.Habitat.Habitat;
import LaboratoryWork.Application.Application;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        new Application(800, 600, new Habitat());
    }
}
