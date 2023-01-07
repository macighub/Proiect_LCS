package benke.ladislau.attila;

import java.awt.*;

public class Main {
    private static frm_Main frm_main;

    public static void main(String[] args) {
        frm_main = new frm_Main();

        frm_main.setVisible(true);
        frm_main.setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth(), (GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height));
        frm_main.setLocation(0,0);
    }
}
