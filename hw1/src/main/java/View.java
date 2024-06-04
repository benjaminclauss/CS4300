import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import model.Clock;
import model.Digit;
import model.Encoding;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import util.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The View class is the "controller" of all our OpenGL stuff.
 *
 * It cleanly encapsulates all our OpenGL functionality from the rest of Java GUI, managed by the JOGLFrame class.
 */
public class View {
  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Matrix4f proj, modelview;
  private ObjectInstance digitObject; // One part of digit in clock.
  private ObjectInstance circleObject; // Circle used as part of colon.
  private ShaderLocationsVault shaderLocations;

  private Vector4f color;
  private Clock clock;

  ShaderProgram program;

  private float scale; // Scale used for size of digits and colon.

  public View() {
    proj = new Matrix4f();
    modelview = new Matrix4f();
    proj.identity();

    digitObject = null;
    circleObject = null;
    shaderLocations = null;
    WINDOW_WIDTH = WINDOW_HEIGHT = 0;

    // Set clock color to green.
    color = new Vector4f(0,1,0,1);
    clock = new Clock();
  }

  public void initialize(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();

    program = new ShaderProgram(); // Compile and make our shader program.
    program.createProgram(gl, "shaders/default.vert", "shaders/default.frag");

    shaderLocations = program.getAllShaderVariables(gl);

    initializeDigitObject(gl);
    initializeCircleObject(gl);

    program.disable(gl);
  }

  /**
   * Initializes digit object.
   *
   * The digit object is one piece of a seven-segment display for the clock.
   * Multiple digit objects can be combined geometrically to compose digits using proper encodings.
   *
   * https://en.wikipedia.org/wiki/Seven-segment_display
   */
  private void initializeDigitObject(GL3 gl) {
    /*
     * Create a triangle mesh from these vertices.
     * The mesh has vertex positions and indices.
     */

    /*
     * Create the vertices of the triangles to be drawn.
     * Since we are drawing in 2D, z-coordinate of all points will be 0.
     * The fourth number for each vertex is 1.
     * This is the homogeneous coordinate and "1" means this is a location not a direction.
     */
    List<Vector4f> positions = new ArrayList<>();
    positions.add(new Vector4f(-2.0f * 1.0f, 1.0f, 0.0f, 1.0f));
    positions.add(new Vector4f(2.0f * 1.0f, 1.0f, 0.0f, 1.0f));
    positions.add(new Vector4f(-3.0f * 1.0f, 0.0f, 0.0f, 1.0f));
    positions.add(new Vector4f(3.0f * 1.0f, 0.0f, 0.0f, 1.0f));
    positions.add(new Vector4f(-2.0f * 1.0f, -1.0f, 0.0f, 1.0f));
    positions.add(new Vector4f(2.0f * 1.0f, -1.0f, 0.0f, 1.0f));

    // Set up vertex attributes (in this case we have only position).
    List<IVertexData> vertexData = new ArrayList<>();
    VertexAttribProducer producer = new VertexAttribProducer();
    for (Vector4f position : positions) {
      IVertexData v = producer.produce();
      v.setData("position", new float[] { position.x, position.y, position.z, position.w});
      vertexData.add(v);
    }

    /*
     * We now generate a series of indices.
     * These indices will be for the above list of vertices to draw triangles with the corresponding indices.
     */
    List<Integer> indices = new ArrayList<>();
    indices.add(0);
    indices.add(2);
    indices.add(4);
    indices.add(0);
    indices.add(1);
    indices.add(4);
    indices.add(1);
    indices.add(4);
    indices.add(5);
    indices.add(1);
    indices.add(3);
    indices.add(5);

    PolygonMesh mesh;
    mesh = new PolygonMesh();

    mesh.setVertexData(vertexData);
    mesh.setPrimitives(indices);

    // Read the list of indices 3 at a time interpret them as triangles.
    mesh.setPrimitiveType(GL.GL_TRIANGLES);
    mesh.setPrimitiveSize(3);

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
    digitObject = new ObjectInstance(gl, program, shaderLocations, shaderToVertexAttribute, mesh, "triangles");
  }

  /**
   * Initializes digit object.
   *
   * Multiple circle objects can be combined for colon in clock.
   */
  private void initializeCircleObject(GL3 gl) {
    /*
     * Create a triangle mesh from these vertices.
     * The mesh has vertex positions and indices.
     */

    // Create the vertices of the circle.
    List<Vector4f> positions = new ArrayList<>();

    int SLICES = 50;
    // Push the center of the circle as the first vertex.
    positions.add(new Vector4f(0, 0, 0, 1));
    for (int i = 0; i < SLICES; i++) {
      float theta = (float) (i * 2 * Math.PI / SLICES);
      positions.add(new Vector4f((float) Math.cos(theta),(float) Math.sin(theta), 0, 1));
    }
    // Add the last vertex to make the circle watertight.
    positions.add(new Vector4f(1, 0, 0, 1));

    // Set up vertex attributes (in this case we have only position).
    List<IVertexData> vertexData = new ArrayList<>();
    VertexAttribProducer producer = new VertexAttribProducer();
    for (Vector4f position : positions) {
      IVertexData v = producer.produce();
      v.setData("position", new float[] { position.x, position.y, position.z, position.w});
      vertexData.add(v);
    }

    /*
     * We now generate a series of indices.
     * These indices will be for the above list of vertices to draw triangles with the corresponding indices.
     */
    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < positions.size(); i++) {
      indices.add(i);
    }

    PolygonMesh mesh;
    mesh = new PolygonMesh<>();
    mesh.setVertexData(vertexData);
    mesh.setPrimitives(indices);
    mesh.setPrimitiveType(GL.GL_TRIANGLE_FAN);
    mesh.setPrimitiveSize(3);

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
    gl.glClear(gl.GL_COLOR_BUFFER_BIT);
    // Enable the shader program.
    program.enable(gl);

    gl.glUniform4fv(shaderLocations.getLocation("vColor"), 1, color.get(fb4));
    gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, modelview.get(fb16));
    gl.glUniformMatrix4fv(shaderLocations.getLocation("projection"), 1, false, proj.get(fb16));

    // Draw digits.
    // Shift for digits left/right from center.
    float digitShift = 11.0f * -2.5f * scale;
    // Additional shift for digits left/right to add colons in between.
    float colonShift = -1.0f * 3.0f * scale;

    List<Digit> digits = clock.getDigitsForTime();
    for (int digitIndex = 0; digitIndex < digits.size(); digitIndex++) {
      Digit digit = digits.get(digitIndex);
      float shift = digitShift + colonShift;
      for (Encoding encoding : digit.getEncoding()) {
        modelview = new Matrix4f();
        // Shift this digit to its position.
        modelview = modelview.translate(
                shift + (scale * encoding.getDx()),
                scale * encoding.getDy(),
                0);
        // Scale digit accordingly.
        modelview.scale(scale, scale, scale);

        // Rotate digit accordingly.
        if (encoding.isVertical()) {
          modelview = modelview.rotate((float) Math.toRadians(90), 0, 0, 1);
        }

        // Send this transformation to the shader.
        gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, modelview.get(fb16));
        digitObject.draw(gla);
      }
      digitShift += 11.0f * scale;
      // For every 2 digits, add to colon shift.
      colonShift = (-1.0f + (digitIndex + 1) / 2) * 3 * scale;
    }

    // Draw colons.
    float radius = scale;

    for (float x = -12.5f; x <= 12.5f; x += 25.0f) {
      for (float y = -3.5f; y <= 3.5f; y += 7.0f) {
        modelview = new Matrix4f();
        // Shift this its circle to its position.
        modelview = modelview.translate(x * scale, y * scale, 0.0f);
        // Scale circle accordingly.
        modelview.scale(radius,radius,radius);

        // Send this transformation to the shader.
        gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, modelview.get(fb16));
        circleObject.draw(gla);
      }
    }

    gl.glFlush();
    // Disable the program.
    program.disable(gl);
  }

  // This method is called from the JOGLFrame class, every time the window is resized.
  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);

    // Scale clock by minimum of screen width and height.
    scale = Math.min(WINDOW_WIDTH, WINDOW_HEIGHT) / 100.0f;

    proj = new Matrix4f().ortho2D(
            -(WINDOW_WIDTH / 2), (WINDOW_WIDTH / 2),
            -(WINDOW_HEIGHT/2), (WINDOW_HEIGHT/2));
  }

  public void dispose(GLAutoDrawable gla) {
  }
}
