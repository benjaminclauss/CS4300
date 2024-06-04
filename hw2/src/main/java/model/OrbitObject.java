package model;

/**
 * Represents an object that orbits around some center.
 */
public abstract class OrbitObject {
    private int radius;
    private int distance;
    private int rotationAngle;
    private int rotationSpeed;
    private int maximumVerticalAngle;
    private int shift;


    /**
     * Creates an orbiting object.
     *
     * @param radius the radius of this object.
     * @param distance the distance from the center of orbit.
     * @param rotation the rotation around the center of orbit.
     * @param rotationSpeed the speed of rotation around the center of orbit.
     * @param maximumVerticalAngle the maximum vertical angle of this orbit from its center.
     * @param shift shift off center of orbit.
     */
    public OrbitObject(int radius, int distance, int rotation, int rotationSpeed, int maximumVerticalAngle, int shift) {
        this.radius = radius;
        this.distance = distance;
        this.rotationAngle = rotation;
        this.rotationSpeed = rotationSpeed;
        this.maximumVerticalAngle = maximumVerticalAngle;
        this.shift = shift;
    }

    public OrbitObject(int radius, int distance, int rotation, int rotationSpeed, int maximumVerticalAngle) {
        this(radius, distance, rotation, rotationSpeed, maximumVerticalAngle, 0);
    }

    public int getRadius() {
        return radius;
    }

    public int getDistance() {
        return distance;
    }

    public int getRotationAngle() {
        return rotationAngle;
    }

    /**
     * Rotate this orbit by its rotation speed.
     */
    public void rotate() {
        rotationAngle = (rotationAngle + rotationSpeed) % 360;
    }

    public int getMaximumVerticalAngle() {
        return maximumVerticalAngle;
    }

    public int getShift() {
        return shift;
    }
}
