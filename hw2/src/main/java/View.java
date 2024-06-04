import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import model.Color;
import model.Planet;
import model.Satellite;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.*;

/**
 * The View class is the "controller" of all our OpenGL stuff.
 *
 * It cleanly encapsulates all our OpenGL functionality from the rest of Java GUI, managed by the JOGLFrame class.
 */
public class View {
    private int WINDOW_WIDTH, WINDOW_HEIGHT;
    private Matrix4f projection;
    private Stack<Matrix4f> modelView;
    private ObjectInstance wireframeObject;
    private ObjectInstance sphereObject;
    private ObjectInstance circleObject;

    private ShaderProgram program;
    private ShaderLocationsVault shaderLocations;

    private static final Vector4f LINE_COLOR = new Vector4f(1,1,1,1);

    private Vector3f cameraUp, cameraEye;
    private int translateX, translateY;

    private int systemSize;
    private List<Planet> planets;

    public View() {
        projection = new Matrix4f();
        projection.identity();
        modelView = new Stack<>();
        WINDOW_WIDTH = WINDOW_HEIGHT = 0;

        systemSize = 500;

        planets = new ArrayList<>();
        Planet sun = new Planet(75, 0, 0, 0, 0, Color.YELLOW);
        Planet p1 = new Planet(15, 80, 30, 3, 30, Color.RED);
        Planet p2 = new Planet(40, 150, 120, 2, 20, Color.GREEN);
        Planet p3 = new Planet(30, 270, 200, 1, 10, Color.BLUE);
        Planet p4 = new Planet(25, 420, 90, 1, 5, Color.CYAN);

        planets.add(sun);
        planets.add(p1);
        planets.add(p2);
        planets.add(p3);
        planets.add(p4);

        Satellite s1 = new Satellite(10, 40, 0, 1, 10, 0);
        Satellite s2 = new Satellite(10, 30, 0, 1, 15, 0);
        Satellite s3 = new Satellite(20, 80, 180, 1, 5, 20);

        p2.addSatellite(s1);
        p3.addSatellite(s2);
        p3.addSatellite(s3);

        cameraUp = new Vector3f(0, 1, 0);
        cameraEye = new Vector3f(2000, 1500, 1500);
    }

    public void initialize(GLAutoDrawable gla) throws Exception {
        GL3 gl = gla.getGL().getGL3();

        program = new ShaderProgram(); // Compile and make shader program.
        program.createProgram(gl, "shaders/default.vert", "shaders/default.frag");

        shaderLocations = program.getAllShaderVariables(gl);

        initializeWireframeObject(gl);
        initializeSphereObject(gl);
        initializeCircleObject(gl);

        program.disable(gl);
    }

    private void initializeWireframeObject(GL3 gl) {
        List<Vector4f> positions = new ArrayList<>();
        positions.add(new Vector4f(1, 1, 1, 1));
        positions.add(new Vector4f(-1, 1, 1, 1));
        positions.add(new Vector4f(-1, 1, -1, 1));
        positions.add(new Vector4f(1, 1, -1, 1));

        positions.add(new Vector4f(1, -1, 1, 1));
        positions.add(new Vector4f(-1, -1, 1, 1));
        positions.add(new Vector4f(-1, -1, -1, 1));
        positions.add(new Vector4f(1, -1, -1, 1));

        // Set up vertex attributes (in this case we have only position).
        List<IVertexData> vertexData = new ArrayList<>();
        VertexAttribProducer producer = new VertexAttribProducer();
        for (Vector4f position : positions) {
            IVertexData v = producer.produce();
            v.setData("position", new float[] {position.x, position.y, position.z, position.w});
            vertexData.add(v);
        }

        /*
         * We now generate a series of indices.
         * These indices will be for the above list of vertices to draw lines with the corresponding indices.
         */
        List<Integer> indices = new ArrayList<>();
        // Add top of wireframe.
        indices.add(0);
        indices.add(1);
        indices.add(1);
        indices.add(2);
        indices.add(2);
        indices.add(3);
        indices.add(3);
        indices.add(0);
        // Add sides of wireframe.
        indices.add(0);
        indices.add(4);
        indices.add(1);
        indices.add(5);
        indices.add(2);
        indices.add(6);
        indices.add(3);
        indices.add(7);
        // Add bottom of wireframe.
        indices.add(4);
        indices.add(5);
        indices.add(5);
        indices.add(6);
        indices.add(6);
        indices.add(7);
        indices.add(7);
        indices.add(4);

        PolygonMesh mesh;
        mesh = new PolygonMesh();

        mesh.setVertexData(vertexData);
        mesh.setPrimitives(indices);

        // Read the list of indices 2 at a time interpret them as lines.
        mesh.setPrimitiveType(GL.GL_LINES);
        mesh.setPrimitiveSize(2);

        /*
         * Provide a mapping between attribute name in the mesh and corresponding shader variable name.
         * This will allow us to use PolygonMesh with any shader program, without assuming that the attribute names in the
         * mesh and the names of shader variables will be the same.
         */
        Map<String, String> shaderToVertexAttribute = new HashMap<>();
        shaderToVertexAttribute.put("vPosition", "position");

        /*
         * Create an object instance for digits (combined to compose digits in clock).
         * The ObjectInstance encapsulates a lot of the OpenGL-specific code to draw this object.
         */
        wireframeObject = new ObjectInstance(gl, program, shaderLocations, shaderToVertexAttribute, mesh, "wireframe");
    }

    private void initializeSphereObject(GL3 gl) throws FileNotFoundException {
        PolygonMesh mesh;
        InputStream in;

        in = new FileInputStream("models/sphere.obj");
        mesh = util.ObjImporter.importFile(new VertexAttribProducer(), in, true);

        Map<String, String> shaderToVertexAttribute = new HashMap<>();

        // Currently there is only one per-vertex attribute: position.
        shaderToVertexAttribute.put("vPosition", "position");

        sphereObject = new ObjectInstance(gl, program, shaderLocations, shaderToVertexAttribute, mesh, "sphere");
    }

    private void initializeCircleObject(GL3 gl) {
        // Create the vertices of the circle.
        List<Vector4f> positions = new ArrayList<>();

        int SLICES = 50;
        // Push the center of the circle as the first vertex.
        positions.add(new Vector4f(0, 0, 0, 1));
        for (int i = 0; i < SLICES; i++) {
            float theta = (float) (i * 2 * Math.PI / SLICES);
            positions.add(new Vector4f((float) Math.cos(theta), 0, (float) Math.sin(theta), 1));
        }
        // Add the last vertex to make the circle watertight.
        positions.add(new Vector4f(1, 0, 0, 1));

        // Set up vertex attributes (in this case we have only position).
        List<IVertexData> vertexData = new ArrayList<>();
        VertexAttribProducer producer = new VertexAttribProducer();
        for (Vector4f position : positions) {
            IVertexData v = producer.produce();
            v.setData("position", new float[] {position.x, position.y, position.z, position.w});
            vertexData.add(v);
        }

        /*
         * We now generate a series of indices.
         * These indices will be for the above list of vertices to draw lines with the corresponding indices.
         */
        List<Integer> indices = new ArrayList<>();
        for (int i = 1; i < positions.size(); i++) {
            indices.add(i);
        }

        PolygonMesh mesh;
        mesh = new PolygonMesh<>();
        mesh.setVertexData(vertexData);
        mesh.setPrimitives(indices);
        mesh.setPrimitiveType(GL.GL_LINE_STRIP);
        mesh.setPrimitiveSize(2);

        /*
         * Provide a mapping between attribute name in the mesh and corresponding shader variable name.
         * This will allow us to use PolygonMesh with any shader program, without assuming that the attribute names in the
         * mesh and the names of shader variables will be the same.
         */
        Map<String, String> shaderToVertexAttribute = new HashMap<>();
        shaderToVertexAttribute.put("vPosition", "position");

        /*
         * Create an object instance for circles (used for colons in clock).
         * The ObjectInstance encapsulates a lot of the OpenGL-specific code to draw this object.
         */
        circleObject = new ObjectInstance(gl, program, shaderLocations, shaderToVertexAttribute, mesh, "circles");
    }

    public void draw(GLAutoDrawable gla) {
        GL3 gl = gla.getGL().getGL3();
        FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
        FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);

        // Set the background color to be black.
        gl.glClearColor(0, 0, 0, 1);
        // Clear the background.
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL.GL_DEPTH_TEST);
        // Enable the shader program.
        program.enable(gl);

        gl.glUniformMatrix4fv(shaderLocations.getLocation("projection"), 1, false, projection.get(fb16));
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE); // Outlines

        modelView.push(new Matrix4f().identity());

        // Add initial look translation.
        Vector3f midAxis = new Vector3f(cameraUp).cross(cameraEye).normalize();
        cameraEye = cameraEye.rotateAxis((float) -Math.toRadians(translateX / 2), cameraUp.x(), cameraUp.y(), cameraUp.z());
        cameraUp = new Vector3f(cameraEye).cross(midAxis).normalize();
        cameraEye = cameraEye.rotateAxis((float) Math.toRadians(translateY / 2), midAxis.x(), midAxis.y(), midAxis.z());
        modelView.peek().lookAt(cameraEye, new Vector3f(0, 0,0), cameraUp);

        // Draw wireframe.
        modelView.push(new Matrix4f(modelView.peek()));
        modelView.peek().scale(systemSize, systemSize, systemSize);
        gl.glUniform4fv(shaderLocations.getLocation("vColor"), 1, LINE_COLOR.get(fb4));
        gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, modelView.peek().get(fb16));
        wireframeObject.draw(gla);
        modelView.pop();

        // Draw planets (with satellites).
        for (Planet planet : planets) {
            modelView.push(new Matrix4f(modelView.peek()));

            // Draw planet orbit.
            modelView.push(new Matrix4f(modelView.peek()));
            modelView.peek()
                    .rotate((float) Math.toRadians(planet.getMaximumVerticalAngle()), 0, 0, 1)
                    .scale(planet.getDistance(), planet.getDistance(), planet.getDistance());
            gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, modelView.peek().get(fb16));
            gl.glUniform4fv(shaderLocations.getLocation("vColor"), 1, LINE_COLOR.get(fb4));
            circleObject.draw(gla);
            modelView.pop();

            modelView.peek()
                    .rotate((float) Math.toRadians(planet.getRotationAngle()),0,1,0)
                    .rotate((float) Math.toRadians(planet.getMaximumVerticalAngle() *
                            (float) Math.cos(Math.toRadians(planet.getRotationAngle()))), 0, 0, 1)
                    .translate(planet.getDistance(), 0, 0);

            // Draw satellites.
            for (Satellite satellite : planet.getSatellites()) {
                // Draw satellite orbit.
                modelView.push(new Matrix4f(modelView.peek()));
                modelView.peek()
                        .translate(satellite.getShift(), 0, 0)
                        .rotate((float) Math.toRadians(satellite.getMaximumVerticalAngle()), 0, 0, 1)
                        .scale(satellite.getDistance(), satellite.getDistance(), satellite.getDistance());
                gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, modelView.peek().get(fb16));
                gl.glUniform4fv(shaderLocations.getLocation("vColor"), 1, LINE_COLOR.get(fb4));
                circleObject.draw(gla);
                modelView.pop();

                modelView.push(new Matrix4f(modelView.peek()));
                modelView.peek()
                        .translate(satellite.getShift(), 0, 0)
                        .rotate((float) Math.toRadians(satellite.getRotationAngle()), 0, 1, 0)
                        .rotate((float) Math.toRadians(satellite.getMaximumVerticalAngle() * (float) Math.cos(Math.toRadians(satellite.getRotationAngle()))), 0, 0, 1)
                        .translate(satellite.getDistance(), 0, 0)
                        .scale(satellite.getRadius(), satellite.getRadius(), satellite.getRadius());

                gl.glUniform4fv(shaderLocations.getLocation("vColor"), 1, Satellite.COLOR.getMaterial().getAmbient().get(fb4));
                gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, modelView.peek().get(fb16));
                sphereObject.draw(gla);
                satellite.rotate();
                modelView.pop();
            }

            modelView.peek().scale(planet.getRadius(), planet.getRadius(), planet.getRadius());

            // Draw planets
            gl.glUniform4fv(shaderLocations.getLocation("vColor"), 1, planet.getColor().getMaterial().getAmbient().get(fb4));
            gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, modelView.peek().get(fb16));
            sphereObject.draw(gla);
            planet.rotate();
            modelView.pop();
        }

        modelView.pop();

        gl.glFlush();
        // Disable the program.
        program.disable(gl);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_FILL); // Back to fill
    }

    // This method is called from the JOGLFrame class, every time the window is resized.
    public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
        GL gl = gla.getGL();
        WINDOW_WIDTH = width;
        WINDOW_HEIGHT = height;
        gl.glViewport(0, 0, width, height);

        projection = new Matrix4f().perspective(
                (float) Math.toRadians(30.0f),
                (float) WINDOW_WIDTH / WINDOW_HEIGHT,
                0.1f,
                10000.0f);
    }

    public void dispose(GLAutoDrawable gla) {
        sphereObject.cleanup(gla);
    }

    public void translate(int x, int y) {
        this.translateX = x;
        this.translateY = y;
    }
}
