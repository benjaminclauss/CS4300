import javax.swing.*;

/**
 * Entry point to start digital clock.
 */
public class DigitalClock {
  public static void main(String[] args) {
    // Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }

  private static void createAndShowGUI() {
    JFrame frame = new JOGLFrame("Digital Clock");
    frame.setVisible(true);
  }
}
