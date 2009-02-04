package rvl.awt;

import rvl.awt.*;
import rvl.util.*;
import java.awt.*;
import java.awt.event.*;

public class ViewWindow extends Frame
    implements ActionListener, AdjustmentListener
{
   public TextArea ta;
   private Scrollbar fontSB;
   private Label fontLab = new Label("12 pt    ");
   private int fontSize = 12;
   private Button clearButton;

   public ViewWindow (String title, int rows, int columns) {
      setTitle(title);
      setLayout(new BorderLayout());
      ta = new TextArea(rows,columns);
      ta.setEditable(false);
      ta.setFont(new Font("Courier", Font.PLAIN, fontSize));

      Panel bottom = new Panel();
      bottom.setBackground(Color.lightGray);
      bottom.setLayout(new FlowLayout());
      fontSB = new Scrollbar(Scrollbar.HORIZONTAL, fontSize, 1, 6, 19);
      clearButton = new Button("Clear");
      clearButton.setVisible(false);
      Button closeB = new Button("Close");
      bottom.add(new Label("Font size"));
      bottom.add(fontSB);
      bottom.add(fontLab);
      bottom.add(clearButton);
      bottom.add(closeB);
      closeB.addActionListener(this);
      fontSB.addAdjustmentListener(this);

      addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent we) {
              dispose();
          }
      });

      add("Center",ta);
      add("South",bottom);
      pack();
      show();
   }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Close"))
            dispose();
        else if(ae.getActionCommand().equals("Clear"))
            ta.setText("");
    }

    public void setClearButton(boolean available) {
        if (available) {
            clearButton.addActionListener(this);
            clearButton.setVisible(true);
            show();
        }
        else {
            clearButton.removeActionListener(this);
            clearButton.setVisible(false);
            show();
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent ae) {
        if (ae.getSource().equals(fontSB)) {
            fontSize = ae.getValue();
            fontLab.setText(fontSize + " pt    ");
            ta.setFont(new Font("Courier", Font.PLAIN, fontSize));
        }
    }

    public void append(String text) {
        ta.append(text);
    }

    public void setText(String text) {
        ta.setText(text);
    }

    public String getText() {
        return ta.getText();
    }

    public void setTop() {
        ta.setCaretPosition(0);
    }

}

