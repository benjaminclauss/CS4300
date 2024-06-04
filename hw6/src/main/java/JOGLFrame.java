import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.Font;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.PMVMatrix;
import org.joml.Vector3f;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

public class JOGLFrame extends JFrame {
  private View view;
  private MyTextRenderer textRenderer;
  private GLCanvas canvas;

  public JOGLFrame(String title, String scenegraphModel, Vector3f globalCameraPosition, Vector3f globalCameraLookAt, Vector3f globalCameraUp) {
    super(title);
    setSize(500, 500);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    view = new View(globalCameraPosition, globalCameraLookAt, globalCameraUp);

    GLProfile glp = GLProfile.getMaxProgrammable(true);
    GLCapabilities caps = new GLCapabilities(glp);
    caps.setDepthBits(24);
    canvas = new GLCanvas(caps);

    add(canvas);

    canvas.addKeyListener(new KeyboardListener());

    canvas.addGLEventListener(new GLEventListener() {
      @Override
      public void init(GLAutoDrawable glAutoDrawable) {
        try {
          // Initialize view of input configuration.
          view.initialize(glAutoDrawable);
          textRenderer = new MyTextRenderer(glAutoDrawable);
          InputStream scenegraphInputStream = getClass().getClassLoader().getResourceAsStream("scenegraphmodels/" + scenegraphModel);
          view.initializeScenegraph(glAutoDrawable, scenegraphInputStream);
          glAutoDrawable.getGL().setSwapInterval(0);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(JOGLFrame.this, e.getMessage(), "Error while loading", JOptionPane.ERROR_MESSAGE);
          System.exit(1);
        }
      }

      @Override
      public void dispose(GLAutoDrawable glAutoDrawable) {
        view.dispose(glAutoDrawable);
      }

      @Override
      public void display(GLAutoDrawable glAutoDrawable) {
        view.draw(canvas);
        String text = "Frame Rate: " + canvas.getAnimator().getLastFPS();
        textRenderer.drawText(glAutoDrawable, text, 10, canvas.getSurfaceHeight() - 50, 1, 0, 0, 20.0f);
      }

      @Override
      public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        view.reshape(glAutoDrawable, x, y, width, height);
        textRenderer.reshape(glAutoDrawable, canvas.getSurfaceWidth(), canvas.getSurfaceHeight());
        repaint();
      }
    });

    // Add an animator to the canvas.
    AnimatorBase animator = new FPSAnimator(canvas, 60);
    animator.setUpdateFPSFrames(60, null);
    animator.start();
  }

  private class KeyboardListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
          view.getDrone().moveUp();
          break;
        case KeyEvent.VK_DOWN:
          view.getDrone().moveDown();
          break;
        case KeyEvent.VK_LEFT:
          view.getDrone().moveLeft();
          break;
        case KeyEvent.VK_RIGHT:
          view.getDrone().moveRight();
          break;
        case KeyEvent.VK_A:
          view.getDrone().turnLeft();
          break;
        case KeyEvent.VK_D:
          view.getDrone().turnRight();
          break;
        case KeyEvent.VK_W:
          view.getDrone().turnUp();
          break;
        case KeyEvent.VK_S:
          view.getDrone().turnDown();
          break;
        case KeyEvent.VK_F:
          view.getDrone().tiltRight();
          break;
        case KeyEvent.VK_C:
          view.getDrone().tiltLeft();
          break;
        case KeyEvent.VK_SPACE:
          view.changeCamera();
          break;
        case KeyEvent.VK_MINUS:
          view.zoomOut();
          break;
        case 61: // case KeyEvent.VK_PLUS had some compatability issues.
          view.zoomIn();
          break;
      }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
  }

  private class MyTextRenderer {
    private TextRegionUtil textRegionUtil;
    private RenderState renderState;
    private RegionRenderer regionRenderer;
    private Font font;
    private int fontSet = FontFactory.JAVA;
    // 2nd pass texture size for antialiasing. Samplecount = 4 is usuallly enough
    private final int[] sampleCount = {4};
    // vao for the curve text renderer. This is because of a bug in the JOGL curve text rendering
    private IntBuffer textVAO; //text renderer VAO

    public MyTextRenderer(GLAutoDrawable glAutoDrawable) throws IOException {
      GL3 gl = glAutoDrawable.getGL().getGL3();

      // Set up the text rendering.
      textVAO = IntBuffer.allocate(1);
      gl.glGenVertexArrays(1, textVAO);
      gl.glBindVertexArray(textVAO.get(0));

      /*
       * JogAmp FontFactory will load a true type font
       *
       * fontSet = 0 loads
       * jogamp.graph.font.fonts.ubunto found inside jogl-fonts-p0.jar
       * http://jogamp.org/deployment/jogamp-current/atomic/jogl-fonts-p0.jar
       *
       * fontSet = 1 loads LucidaBrightRegular from the JRE
       */
      font = FontFactory.get(fontSet).getDefault();

      // Initialize OpenGL specific classes that know how to render the graph API shapes.
      renderState = RenderState.createRenderState(SVertex.factory());
      // Define a RED color to render our shape with/
      renderState.setColorStatic(1.0f, 0.0f, 0.0f, 1.0f);
      renderState.setHintMask(RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED);
      regionRenderer = RegionRenderer.create(renderState, RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);
      regionRenderer.init(gl, Region.MSAA_RENDERING_BIT);
      textRegionUtil = new TextRegionUtil(Region.MSAA_RENDERING_BIT);
    }

    public void drawText(GLAutoDrawable glAutoDrawable, String text, int x, int y, float r, float g, float b, float fontSize) {
      GL3 gl = glAutoDrawable.getGL().getGL3();

      gl.glClear(gl.GL_DEPTH_BUFFER_BIT);
      gl.glEnable(gl.GL_DEPTH_TEST);
      // Draw the shape using RegionRenderer and TextRegionUtil.
      // The RegionRenderer PMVMatrix helps us to place and size the text.
      if (!regionRenderer.isInitialized()) {
        regionRenderer.init(gl, Region.VBAA_RENDERING_BIT);

      }
      final PMVMatrix pmv = regionRenderer.getMatrix();
      pmv.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
      pmv.glLoadIdentity();
      pmv.glTranslatef(x, y, -999.0f);

      regionRenderer.enable(gl, true);
      gl.glBindVertexArray(textVAO.get(0));
      renderState.setColorStatic(r, g, b, 1.0f);
      textRegionUtil.drawString3D(gl, regionRenderer, font, fontSize, text, null, sampleCount);
      gl.glBindVertexArray(0);
      regionRenderer.enable(gl, false);
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
      GL3 gl = glAutoDrawable.getGL().getGL3();
      gl.glDeleteVertexArrays(1, textVAO);
    }

    public void reshape(GLAutoDrawable glAutoDrawable, int width, int height) {
      GL3 gl = glAutoDrawable.getGL().getGL3();
      regionRenderer.enable(gl, true);
      regionRenderer.reshapeOrtho(width, height, 0.1f, 1000.0f);
      regionRenderer.enable(gl, false);
    }
  }
}
