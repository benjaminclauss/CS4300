package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a planet.
 */
public class Planet extends OrbitObject {
    private Color color;
    private List<Satellite> satellites; // satellites for this planet

    public Planet(int radius, int distance, int rotation, int rotationSpeed, int maximumVerticalAngle, Color color) {
        super(radius, distance, rotation, rotationSpeed, maximumVerticalAngle);
        this.color = color;
        this.satellites = new ArrayList<>();
    }

    public Color getColor() {
        return color;
    }

    public void addSatellite(Satellite satellite) {
        this.satellites.add(satellite);
    }

    public List<Satellite> getSatellites() {
        return satellites;
    }
}
