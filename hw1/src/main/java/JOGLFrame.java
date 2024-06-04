import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;

/**
 * Main Java frame for digital clock.
 */
public class JOGLFrame extends JFrame {
  private View view;
  private static final int INITIAL_WIDTH = 800;
  private static final int INITIAL_HEIGHT = 400;

  public JOGLFrame(String title) {
    // Set up JFrame.
    super(title);
    setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // View class is driver of OpenGL.
    view = new View();

    GLProfile glp = GLProfile.getGL2GL3();
    GLCapabilities capabilities = new GLCapabilities(glp);
    GLCanvas canvas = new GLCanvas(capabilities);

    add(canvas);

    canvas.addGLEventListener(new GLEventListener() {
      @Override
      public void init(GLAutoDrawable glAutoDrawable) { // This is called the first time this canvas is created.
        try {
          // View is initialized.
          view.initialize(glAutoDrawable);
          glAutoDrawable.getGL().setSwapInterval(0);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(JOGLFrame.this, e.getMessage(), "Error while loading", JOptionPane.ERROR_MESSAGE);
        }
      }

      @Override
      public void dispose(GLAutoDrawable glAutoDrawable) { // This is called when the canvas is destroyed.
        view.dispose(glAutoDrawable);
      }

      @Override
      public void display(GLAutoDrawable glAutoDrawable) { // This is called every time this window must be redrawn.
        view.draw(glAutoDrawable);
      }

      @Override
      public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) { // This is called every time this canvas is resized.
        view.reshape(glAutoDrawable, x, y, width, height);
        repaint();
      }
    });

    // Add an animator to the canvas.
    AnimatorBase animator = new FPSAnimator(canvas, 300);
    animator.setUpdateFPSFrames(100, null);
    animator.start();
  }
}
