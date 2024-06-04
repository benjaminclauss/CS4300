import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class ScenegraphViewer {
    private static String scenegraphModel;
    private static Vector3f cameraPosition;
    private static Vector3f cameraLookAt;
    private static Vector3f cameraUp;

    private static float cameraY;
    private static float cameraZ;

    public static void main(String[] args) throws FileNotFoundException {
        loadConfiguration(args[0]);
        // Schedule a job for the event-dispatching thread - creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void loadConfiguration(String filename) throws FileNotFoundException {
        Scanner in = new Scanner(new FileReader(filename));
        scenegraphModel = in.nextLine();
        float cameraPositionX = Float.parseFloat(in.nextLine());
        float cameraPositionY = Float.parseFloat(in.nextLine());
        float cameraPositionZ = Float.parseFloat(in.nextLine());
        cameraPosition = new Vector3f(cameraPositionX, cameraPositionY, cameraPositionZ);
        float cameraLookAtX = Float.parseFloat(in.nextLine());
        float cameraLookAtY = Float.parseFloat(in.nextLine());
        float cameraLookAtZ = Float.parseFloat(in.nextLine());
        cameraLookAt = new Vector3f(cameraLookAtX, cameraLookAtY, cameraLookAtZ);
        float cameraUpX = Float.parseFloat(in.nextLine());
        float cameraUpY = Float.parseFloat(in.nextLine());
        float cameraUpZ = Float.parseFloat(in.nextLine());
        cameraUp = new Vector3f(cameraUpX, cameraUpY, cameraUpZ);
        in.close();
    }

    private static void createAndShowGUI() {
        JFrame frame = new JOGLFrame("Scene Graph Viewer", scenegraphModel, cameraPosition, cameraLookAt, cameraUp);
        frame.setVisible(true);
    }
}
