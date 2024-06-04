package model;

import org.joml.Vector3f;

/**
 * Represents a drone with position and direction.
 */
public class Drone {
    private static final float MOVEMENT_SPEED = 1;
    private static final float ROTATION_SPEED = 1;

    // Position of the drone.
    Vector3f position;
    // Front facing direction vector of the drone.
    Vector3f front;
    // Up facing direction vector of the drone.
    Vector3f up;

    float yaw;
    float pitch;
    float roll;

    public Drone(Vector3f position, Vector3f front, Vector3f up) {
        this.position = position;
        this.front = front;
        this.up = up;
        this.yaw = 0;
        this.pitch = 0;
        this.roll = 0;
    }

    public Vector3f getPosition() {
        return new Vector3f(this.position);
    }

    /**
     * Calculates the front direction vector using the pitch, yaw, and roll.
     */
    private Vector3f calculateFrontVector() {
        return new Vector3f(this.front)
                .rotateX((float) Math.toRadians(pitch)).normalize()
                .rotateY((float) Math.toRadians(yaw)).normalize()
                .rotateZ((float) Math.toRadians(roll)).normalize();
    }

    public Vector3f getFront() {
        return calculateFrontVector();
    }

    /**
     * Calculates the up direction vector using the pitch, yaw, and roll.
     */
    private Vector3f calculateUpVector() {
        return new Vector3f(this.up)
                .rotateX((float) Math.toRadians(pitch)).normalize()
                .rotateY((float) Math.toRadians(yaw)).normalize()
                .rotateZ((float) Math.toRadians(roll)).normalize();
    }

    public Vector3f getUp() {
        return calculateUpVector();
    }

    public Vector3f getTarget() {
        return new Vector3f(this.position).add(this.calculateFrontVector());
    }

    public void moveUp() {
        this.position.add(new Vector3f(this.up).mul(MOVEMENT_SPEED));
    }

    public void moveDown() {
        this.position.sub(new Vector3f(this.up).mul(MOVEMENT_SPEED));
    }

    private Vector3f getRight() {
        return new Vector3f(this.calculateFrontVector()).cross(this.calculateUpVector()).normalize();
    }

    public void moveLeft() {
        this.position.sub(this.getRight()).mul(MOVEMENT_SPEED);
    }

    public void moveRight() {
        this.position.add(this.getRight()).mul(MOVEMENT_SPEED);
    }

    public void turnLeft() {
        this.yaw = (this.yaw + ROTATION_SPEED) % 360;
    }

    public void turnRight() {
        this.yaw = (this.yaw - ROTATION_SPEED) % 360;
    }

    public void turnUp() {
        this.pitch = (this.pitch + ROTATION_SPEED) % 360;
    }

    public void turnDown() {
        this.pitch = (this.pitch - ROTATION_SPEED) % 360;
    }

    public void tiltRight() {
        this.roll = (this.roll - ROTATION_SPEED) % 360;
    }

    public void tiltLeft() {
        this.roll = (this.roll + ROTATION_SPEED) % 360;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

}
