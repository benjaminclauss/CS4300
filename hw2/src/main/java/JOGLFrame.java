import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.PMVMatrix;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.IntBuffer;

public class JOGLFrame extends JFrame {
    private View view;
    private MyTextRenderer textRenderer;
    private GLCanvas canvas;
    private static final int INITIAL_WIDTH = 800;
    private static final int INITIAL_HEIGHT = 800;
    private int mouseX, mouseY;

    public JOGLFrame(String title) {
        // Set up JFrame.
        super(title);
        setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // View class is driver of OpenGL.
        view = new View();

        GLProfile glp = GLProfile.getGL2GL3();
        GLCapabilities caps = new GLCapabilities(glp);
        canvas = new GLCanvas(caps);

        add(canvas);

        EventListener listener = new EventListener();
        canvas.addGLEventListener(listener);
        canvas.addMouseListener(listener);
        canvas.addMouseMotionListener(listener);
        canvas.addMouseWheelListener(listener);

        // Add an animator to the canvas.
        AnimatorBase animator = new FPSAnimator(canvas, 300);
        animator.setUpdateFPSFrames(100, null);
        animator.start();
    }

    class EventListener extends MouseAdapter implements GLEventListener {
        @Override
        public void init(GLAutoDrawable glAutoDrawable) { // This is called the first time this canvas is created.
            try {
                view.initialize(glAutoDrawable);
                textRenderer = new MyTextRenderer(glAutoDrawable);
                glAutoDrawable.getGL().setSwapInterval(1);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(JOGLFrame.this, e.getMessage(), "Error while loading.", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void dispose(GLAutoDrawable glAutoDrawable) { // This is called when the canvas is destroyed.
            view.dispose(glAutoDrawable);
            textRenderer.dispose(glAutoDrawable);
        }

        @Override
        public void display(GLAutoDrawable glAutoDrawable) { // This is called every time this window must be redrawn.
            view.draw(glAutoDrawable);
        }

        @Override
        public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) { // This is called every time this canvas is resized.
            view.reshape(glAutoDrawable, x, y, width, height);
            textRenderer.reshape(glAutoDrawable,canvas.getSurfaceWidth(),canvas.getSurfaceHeight());
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mouseX = e.getX();
            mouseY = canvas.getHeight() - e.getY();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            view.translate(e.getX() - mouseX, canvas.getHeight() - e.getY() - mouseY);
            mouseX = e.getX();
            mouseY = canvas.getHeight() - e.getY();
            canvas.repaint();
        }
    }

    private class MyTextRenderer {
        private TextRegionUtil textRegionUtil;
        private RenderState renderState;
        private RegionRenderer regionRenderer;
        private com.jogamp.graph.font.Font font;
        private int fontSet = FontFactory.JAVA;
        // 2nd pass texture size for antialiasing. Samplecount = 4 is usuallly enough
        private final int[] sampleCount = {4};
        // VAO for the curve text renderer. This is because of a bug in the JOGL curve text rendering.
        private IntBuffer textVAO; //text renderer VAO

        public MyTextRenderer(GLAutoDrawable glAutoDrawable) throws IOException {
            GL3 gl = glAutoDrawable.getGL().getGL3();

            // Set up the text rendering.
            textVAO = IntBuffer.allocate(1);
            gl.glGenVertexArrays(1,textVAO);
            gl.glBindVertexArray(textVAO.get(0));
            /**
             *  JogAmp FontFactory will load a true type font
             *
             *  fontSet = 0 loads
             *  jogamp.graph.font.fonts.ubunto found inside jogl-fonts-p0.jar
             *  http://jogamp.org/deployment/jogamp-current/atomic/jogl-fonts-p0.jar
             *
             *  fontSet = 1 loads LucidaBrightRegular from the JRE
             */
            font = FontFactory.get(fontSet).getDefault();

            // Initialize OpenGL specific classes that know how to render the graph API shapes.
            renderState = RenderState.createRenderState(SVertex.factory());
            // Define a RED color to render our shape with.
            renderState.setColorStatic(1.0f, 0.0f, 0.0f, 1.0f);
            renderState.setHintMask(RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED);
            regionRenderer = RegionRenderer.create(renderState, RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);
            regionRenderer.init(gl, Region.MSAA_RENDERING_BIT);
            textRegionUtil = new TextRegionUtil(Region.MSAA_RENDERING_BIT);
        }

        public void drawText(GLAutoDrawable glAutoDrawable, String text, int x, int y, float r, float g, float b, float fontSize) {
            GL3 gl = glAutoDrawable.getGL().getGL3();

            gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
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
