package rvl.piface;

public interface PiComponent {
    public String getName();
    public String getLabel();
    public void setName(String name, String label);
    public void addActionListener(java.awt.event.ActionListener al);
}
