package rvl.stat.anova;

import java.awt.*;
import rvl.util.*;

/** GUI for soliciting factors */
public class GetFactorsGUI extends Frame {
    TextField facName, nLev;
    Checkbox fixedBox, randomBox;
    boolean random = false, firstTime = true;
    List facList;
    Model model;

    public GetFactorsGUI(Model m) {
        model = m;
        setTitle("Enter factors for ANOVA model");

        //-- Factor-name panel
        Panel fpan = new Panel();
        fpan.setLayout(new GridLayout(5,1));
        fpan.add(new Label("Name of factor"));
        facName = new TextField(10);
        nLev = new TextField(4);
        fpan.add(facName);
        //-- radio-button panel
          Panel fixRan = new Panel();
          CheckboxGroup g = new CheckboxGroup();
          fixedBox = new Checkbox("Fixed",g,true);
          randomBox = new Checkbox("Random",g,false);
          fixRan.add(fixedBox);
          fixRan.add(randomBox);
        fpan.add(fixRan);
        fpan.add(new Label("# levels"));
        fpan.add(nLev);
        
        //-- nested-factors panel
        Panel npan = new Panel();
        npan.setLayout(new BorderLayout());
        npan.add("North", new Label("Nested in..."));
        facList = new List(3, true);
        npan.add("Center",facList);

        //-- action buttons
        Panel bpan = new Panel();
        bpan.setLayout(new FlowLayout(FlowLayout.LEFT));
        bpan.add(new Button("Accept"));
        bpan.add(new Button("Finish"));
        bpan.add(new Button("Start over"));
        bpan.add(new Button("Cancel"));

        //-- assemble panels and buttons
        setLayout(new BorderLayout());
        add("West",fpan);
        add("South",bpan);
        add("Center",npan);

        resize(350,200);
        show();
    }

    public boolean HandleEvent(Event evt) {
        if (evt.id == Event.WINDOW_DESTROY) {
System.out.println("Window closed");
            dispose();
            return true;
        }
        return super.handleEvent(evt);
    }

    public boolean action(Event evt, Object arg) {
        if (arg.equals("Cancel")) {
            dispose();
            System.exit(0);
        }
        else if (evt.target.equals(fixedBox)) random = false;
        else if (evt.target.equals(randomBox)) random = true;
        else if (arg.equals("Accept")) {
            String s = facName.getText();
            int n = Utility.strtoi(nLev.getText());
            boolean isNested = false;
            Term t = new Term();
            for (int i=0; i<facList.countItems(); i++)
                if (facList.isSelected(i)) {
                    t = isNested ?
                        new Term(t,model.getFac(i)) :
                        model.getFac(i);
                    facList.deselect(i);
                    isNested = true;
                }
            Factor f = isNested ?
                new Factor(s,n,t) :
                new Factor(s,n,random);
            model.addFactor(f);
            facList.addItem(f.getName());
            facName.setText("");
            nLev.setText("");
            random = false;
            fixedBox.setState(true);
        }
        else if (arg.equals("Finish")) {
            model.printEMS();
        }
        return true;
    }

}
