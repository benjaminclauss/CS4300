import javax.swing.*;

public class SolarSystem {
    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JOGLFrame("Solar System");
        frame.setVisible(true);
    }
}
