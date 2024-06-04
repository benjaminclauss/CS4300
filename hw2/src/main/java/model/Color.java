package model;

import util.Material;

/**
 * Represents a color.
 */
public enum Color {
    RED(1, 0, 0),
    GREEN(0, 1, 0),
    BLUE(0, 0, 1),
    CYAN(0, 1, 1),
    YELLOW(1, 1, 0),
    GRAY(0.5f, 0.5f, 0.5f);

    private float r;
    private float g;
    private float b;

    Color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Material getMaterial() {
        Material material = new util.Material();
        material.setAmbient(this.r, this.g, this.b);
        material.setDiffuse(1,1,1);
        material.setSpecular(1,1,1);
        return material;
    }
}
