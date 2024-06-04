import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import model.Drone;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * The View class is the "controller" of all our OpenGL stuff.
 * It cleanly encapsulates all our OpenGL functionality from the rest of Java GUI, managed by the JOGLFrame class.
 */
public class View {
  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Stack<Matrix4f> modelView;
  private Matrix4f projection;
  private util.ShaderProgram program;
  private util.ShaderLocationsVault shaderLocations;
  private int projectionLocation;
  private sgraph.IScenegraph<VertexAttrib> scenegraph;
  private sgraph.IScenegraph<VertexAttrib> droneModel;

  private Drone drone;
  private int time;

  private boolean droneCameraEnabled;
  private Vector3f globalCameraPosition;
  private Vector3f globalCameraLookAt;
  private Vector3f globalCameraUp;
  private float magnification;

  public View(Vector3f globalCameraPosition, Vector3f globalCameraLookAt, Vector3f globalCameraUp) {
    projection = new Matrix4f();
    modelView = new Stack<>();
    scenegraph = null;
    droneModel = null;

    // Initialize drone.
    Vector3f position = new Vector3f(0, 50, 125);
    Vector3f front = new Vector3f(0, 0, -1);
    Vector3f up = new Vector3f(0, 1, 0);
    this.drone = new Drone(position, front, up);

    time = 0;

    droneCameraEnabled = false;
    this.globalCameraPosition = globalCameraPosition;
    this.globalCameraLookAt = globalCameraLookAt;
    this.globalCameraUp = globalCameraUp;

    magnification = 1.0f;
  }

  public void initialize(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();

    // Compile and make our shader program.
    program = new util.ShaderProgram();

    program.createProgram(gl, "shaders/default.vert", "shaders/default.frag");

    shaderLocations = program.getAllShaderVariables(gl);

    // Get input variables that need to be given to the shader program.
    projectionLocation = shaderLocations.getLocation("projection");
  }

  public void initializeScenegraph(GLAutoDrawable gla, InputStream in) throws Exception {
    GL3 gl = gla.getGL().getGL3();

    if (scenegraph != null)
      scenegraph.dispose();

    program.enable(gl);

    scenegraph = sgraph.SceneXMLReader.importScenegraph(in, new VertexAttribProducer());

    sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
    renderer.setContext(gla);
    Map<String, String> shaderVarsToVertexAttribs = new HashMap<>();
    shaderVarsToVertexAttribs.put("vPosition", "position");
    shaderVarsToVertexAttribs.put("vNormal", "normal");
    shaderVarsToVertexAttribs.put("vTexCoord", "texcoord");
    renderer.initShaderProgram(program, shaderVarsToVertexAttribs);
    scenegraph.setRenderer(renderer);
    program.disable(gl);
  }

  public void initializeDrone(GLAutoDrawable gla, InputStream in) throws Exception {
    GL3 gl = gla.getGL().getGL3();

    if (droneModel != null)
      droneModel.dispose();

    program.enable(gl);

    droneModel = sgraph.SceneXMLReader.importScenegraph(in, new VertexAttribProducer());

    sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
    renderer.setContext(gla);
    Map<String, String> shaderVarsToVertexAttribs = new HashMap<>();
    shaderVarsToVertexAttribs.put("vPosition", "position");
    shaderVarsToVertexAttribs.put("vNormal", "normal");
    shaderVarsToVertexAttribs.put("vTexCoord", "texcoord");
    renderer.initShaderProgram(program, shaderVarsToVertexAttribs);
    droneModel.setRenderer(renderer);
    program.disable(gl);
  }

  public void draw(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

    gl.glClearColor(0, 0, 0, 1);
    gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
    gl.glEnable(gl.GL_DEPTH_TEST);

    // Set main camera and picture-in-picture camera.
    Vector3f currentCameraPosition = this.globalCameraPosition;
    Vector3f currentCameraTarget = this.globalCameraLookAt;
    Vector3f currentCameraUp = this.globalCameraUp;
    Vector3f otherCameraPosition = drone.getPosition();
    Vector3f otherCameraTarget = drone.getTarget();
    Vector3f otherCameraUp = drone.getUp();

    if (droneCameraEnabled) {
      currentCameraPosition = drone.getPosition();
      currentCameraTarget = drone.getTarget();
      currentCameraUp = drone.getUp();
      otherCameraPosition = this.globalCameraPosition;
      otherCameraTarget = this.globalCameraLookAt;
      otherCameraUp = this.globalCameraUp;
    }

    // Increment time as animation is based on it.
    time += 1;
    // Animate drone propellers.
    droneModel.animate(time);

    program.enable(gl);

    while (!modelView.empty())
      modelView.pop();

    modelView.push(new Matrix4f());
    // Set camera for toggled camera.
    modelView.peek().lookAt(currentCameraPosition, currentCameraTarget, currentCameraUp);

    // Supply the shader with all the matrices it expects.
    FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
    gl.glUniformMatrix4fv(projectionLocation, 1, false, projection.get(fb));

    // Zoom in drone camera.
    if (droneCameraEnabled) {
      modelView.peek().scale(magnification, magnification, magnification);
    }
    scenegraph.draw(modelView);
    modelView.pop();

    modelView.push(new Matrix4f());
    modelView.peek().lookAt(currentCameraPosition, currentCameraTarget, currentCameraUp);
    // Move drone to position and rotate accordingly.
    modelView.peek()
            .translate(drone.getPosition().x, drone.getPosition().y, drone.getPosition().z)
            .rotateY((float) Math.toRadians(drone.getYaw()));
    droneModel.draw(modelView);
    modelView.pop();

    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
    modelView.push(new Matrix4f());

    // Draw picture-in-picture.
    gl.glViewport(3*WINDOW_WIDTH/4, 3*WINDOW_HEIGHT/4, WINDOW_WIDTH/4, WINDOW_HEIGHT/4);
    // Set camera for picture-in-picture camera.
    modelView.peek().lookAt(otherCameraPosition, otherCameraTarget, otherCameraUp);

    // Zoom in drone camera.
    if (!droneCameraEnabled) {
      modelView.peek().scale(magnification, magnification, magnification);
    }
    scenegraph.draw(modelView);
    modelView.pop();

    modelView.push(new Matrix4f());
    modelView.peek().lookAt(otherCameraPosition, otherCameraTarget, otherCameraUp);
    // Move drone to position and rotate accordingly.
    modelView.peek()
            .translate(drone.getPosition().x, drone.getPosition().y, drone.getPosition().z)
            .rotateY((float) Math.toRadians(drone.getYaw()));
    droneModel.draw(modelView);
    modelView.pop();

    /*
     * OpenGL batch-processes all its OpenGL commands.
     * The next command asks OpenGL to "empty" its batch of issued commands, i.e. draw
     *
     * This a non-blocking function. That is, it will signal OpenGL to draw, but won't wait for it to finish drawing.
     *
     * If you would like OpenGL to start drawing and wait until it is done, call glFinish() instead.
     */
    gl.glFlush();

    program.disable(gl);
  }

  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);

    projection = new Matrix4f().perspective((float) Math.toRadians(120), (float) width / height, 0.1f, 10000.0f);
//    projection = new Matrix4f().ortho(-400, 400, -400, 400, 0.1f, 10000.0f);
  }

  public void dispose(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();
  }

  public Drone getDrone() {
    return drone;
  }

  public void changeCamera() {
    droneCameraEnabled = !droneCameraEnabled;
  }

  /**
   * Zoom camera of drone in.
   */
  public void zoomIn() {
    if (magnification + .1 < 1.6) {
      magnification += .1;
    }
  }

  /**
   * Zoom camera of drone out.
   */
  public void zoomOut() {
    if (magnification - .1 > 1) {
      magnification -= .1;
    }
  }

}
