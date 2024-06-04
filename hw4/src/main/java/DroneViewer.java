import org.joml.Vector3f;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class DroneViewer {
    private static String scenegraphModel;
    private static Vector3f globalCameraPosition;
    private static Vector3f globalCameraLookAt;
    private static Vector3f globalCameraUp;

    public static void main(String[] args) throws FileNotFoundException {
        loadInputConfiguration(args[0]);
        // Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void loadInputConfiguration(String filename) throws FileNotFoundException {
        Scanner in = new Scanner(new FileReader(filename));
        scenegraphModel = in.nextLine();
        float cameraPositionX = Float.parseFloat(in.nextLine());
        float cameraPositionY = Float.parseFloat(in.nextLine());
        float cameraPositionZ = Float.parseFloat(in.nextLine());
        globalCameraPosition = new Vector3f(cameraPositionX, cameraPositionY, cameraPositionZ);
        float cameraLookAtX = Float.parseFloat(in.nextLine());
        float cameraLookAtY = Float.parseFloat(in.nextLine());
        float cameraLookAtZ = Float.parseFloat(in.nextLine());
        globalCameraLookAt = new Vector3f(cameraLookAtX, cameraLookAtY, cameraLookAtZ);
        float cameraUpX = Float.parseFloat(in.nextLine());
        float cameraUpY = Float.parseFloat(in.nextLine());
        float cameraUpZ = Float.parseFloat(in.nextLine());
        globalCameraUp = new Vector3f(cameraUpX, cameraUpY, cameraUpZ);
        in.close();
    }

    private static void createAndShowGUI() {
        JFrame frame = new JOGLFrame("Drone Viewer", scenegraphModel, globalCameraPosition, globalCameraLookAt, globalCameraUp);
        frame.setVisible(true);
    }
}
