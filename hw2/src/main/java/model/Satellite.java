package model;

/**
 * Represents a satellite.
 */
public class Satellite extends OrbitObject {
    public static Color COLOR = Color.GRAY;

    public Satellite(int radius, int distance, int rotation, int rotationSpeed, int maximumVerticalAngle, int shift) {
        super(radius, distance, rotation, rotationSpeed, maximumVerticalAngle, shift);
    }
}
