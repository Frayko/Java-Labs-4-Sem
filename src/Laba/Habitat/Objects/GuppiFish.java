package Laba.Habitat.Objects;

import Laba.Habitat.Fish;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GuppiFish extends Fish {
    private static BufferedImage image;

    static {
        try {
            image = ImageIO.read(new File("src/Laba/Assets/guppi_fish.png"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GuppiFish(int x, int y) {
        super(x,y);
    }

    public void drawImage(Graphics g) {
        g.drawImage(image, this.X, this.Y, null);
    }
}
