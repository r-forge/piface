package rvl.piface;

public interface IntComponent extends PiComponent {
    public int getValue();
    public String getTextValue();   // string associated w/ getValue();
    public void setValue(int x);
}
