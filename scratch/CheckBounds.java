package scratch;

import java.awt.*;
import javax.swing.*;
import aplikasdatafilm.*;

public class CheckBounds {
    public static void main(String[] args) {
        Login frame = new Login();
        frame.addNotify();
        frame.setSize(1280, 720);
        frame.validate();
        
        Component[] comps = frame.getContentPane().getComponents();
        System.out.println("ContentPane component count: " + comps.length);
        for (Component c : comps) {
            System.out.println("Component: " + c.getClass().getName() + " bounds: " + c.getBounds());
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) {
                    System.out.println("  Child: " + child.getClass().getName() + " bounds: " + child.getBounds());
                }
            }
        }
        System.exit(0);
    }
}
