package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import aplikasdatafilm.*;

public class TestRender {
    public static void main(String[] args) throws Exception {
        Login frame = new Login();
        frame.setSize(1280, 720);
        frame.validate();
        
        BufferedImage img = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        frame.getContentPane().setSize(1280, 720);
        frame.getContentPane().validate();
        frame.getContentPane().printAll(g2);
        g2.dispose();
        ImageIO.write(img, "png", new File("scratch/test_login_render.png"));
        System.out.println("Rendered successfully, size: " + new File("scratch/test_login_render.png").length());
        System.exit(0);
    }
}
