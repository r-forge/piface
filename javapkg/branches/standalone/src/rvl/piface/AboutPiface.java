package rvl.piface;

import rvl.piface.*;
import java.awt.*;

// "About" modal dialog for Piface.

public class AboutPiface extends Piface {

    public void gui () {
        remove(menuBar);
        setBackground(Color.white);

        filler();
        panel.add(new PiImage("piface.png",AboutPiface.class),"Center");
        filler();
        label("Illustration by Brian W. Lenth",
            Color.gray,
            new Font("SanSerif", Font.PLAIN, 9));

        newColumn();
        label("Piface", (Color.blue).darker(),
            new Font("SansSerif", Font.BOLD, 36));
        label("by Russell V. Lenth");
        label("Version " + Piface.getVersion());
        //label("The University of Iowa");
        beginSubpanel(1);
            panel.setForeground(Color.black);
            panel.setLayout(new FlowLayout(FlowLayout.CENTER));
            button("close"," OK ");
        endSubpanel();
    }

    public void click() {}

    public void label(String text, Color color, Font font) {
        Label lbl = new Label(text, Label.CENTER);
        lbl.setForeground(color);
        lbl.setFont(font);
        panel.add(lbl);
    }
    public void label(String text) {
        label(text, (Color.red).darker(), bigFont);
    }

    public AboutPiface() {
        super("About Piface");
    }

    public void close() {
        dispose();
    }

}

