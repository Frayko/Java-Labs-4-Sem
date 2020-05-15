package LaboratoryWork.Habitat.Fish.Objects;

import LaboratoryWork.Habitat.Fish.Fish;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.imageio.ImageIO;

public class GoldenFish extends Fish implements Serializable {
    private static BufferedImage image;

    public GoldenFish(int x, int y) {
        super(x, y);
        this.setImageWidth(image.getWidth());
        this.setImageHeight(image.getHeight());
    }

    public void drawImage(Graphics g) {
        g.drawImage(image, this.getX(), this.getY(), this.getImageWidth(), this.getImageHeight(), null);
    }

    static {
        try {
            image = ImageIO.read(new File("src/LaboratoryWork/Assets/golden_fish.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
