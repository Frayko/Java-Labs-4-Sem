package LaboratoryWork.Habitat.Objects;

import LaboratoryWork.Habitat.Fish;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GuppiesFish extends Fish {
    private static BufferedImage image;

    static {
        try {
            image = ImageIO.read(new File("src/LaboratoryWork/Assets/guppies_fish.png"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GuppiesFish(int x, int y) {
        super(x,y);
    }

    public void drawImage(Graphics g) {
        g.drawImage(image, getX(), getY(), null);
    }
}
