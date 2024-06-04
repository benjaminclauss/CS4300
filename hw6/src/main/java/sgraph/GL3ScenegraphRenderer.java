package sgraph;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.texture.Texture;

import org.joml.Matrix4f;
import util.IVertexData;
import util.Light;
import util.TextureImage;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.*;

/**
 * This is a scene graph renderer implementation that works specifically with the JOGL library
 * It mandates OpenGL 3 and above.
 * @author Amit Shesh
 */
public class GL3ScenegraphRenderer implements IScenegraphRenderer {
    /**
     * The JOGL specific rendering context
     */
    protected GLAutoDrawable glContext;
    /**
     * A table of shader locations and variable names
     */
    protected util.ShaderLocationsVault shaderLocations;
    /**
     * A table of shader variables -> vertex attribute names in each mesh
     */
    protected Map<String,String> shaderVarsToVertexAttribs;

    /**
     * A map to store all the textures
     */
    protected Map<String, TextureImage> textures;
    /**
     * A table of renderers for individual meshes
     */
    protected Map<String,util.ObjectInstance> meshRenderers;

    /**
     * A variable tracking whether shader locations have been set. This must be done before
     * drawing!
     */
    private boolean shaderLocationsSet;
    /**
     * Default white texture
     */
    protected final static String WHITE_TEXTURE = "white";

    public GL3ScenegraphRenderer() {
        meshRenderers = new HashMap<>();
        shaderLocations = new util.ShaderLocationsVault();
        shaderLocationsSet = false;
        textures = new HashMap<>();
    }

    /**
     * Specifically checks if the passed rendering context is the correct JOGL-specific
     * rendering context {@link com.jogamp.opengl.GLAutoDrawable}
     * @param obj the rendering context (should be {@link com.jogamp.opengl.GLAutoDrawable})
     * @throws IllegalArgumentException if given rendering context is not {@link com.jogamp.opengl.GLAutoDrawable}
     */
    @Override
    public void setContext(Object obj) throws IllegalArgumentException {
        if (obj instanceof GLAutoDrawable) {
            glContext = (GLAutoDrawable) obj;
        }
        else {
            throw new IllegalArgumentException("Context not of type GLAutoDrawable");
        }
    }

    /**
     * Add a mesh to be drawn later.
     * The rendering context should be set before calling this function, as this function needs it
     * This function creates a new
     * {@link util.ObjectInstance} object for this mesh
     * @param name the name by which this mesh is referred to by the scene graph
     * @param mesh the {@link util.PolygonMesh} object that represents this mesh
     * @throws Exception
     */
    @Override
    public <K extends IVertexData> void addMesh(String name, util.PolygonMesh<K> mesh) throws Exception {
        if (!shaderLocationsSet)
            throw new Exception("Attempting to add mesh before setting shader variables. Call initShaderProgram first");
        if (glContext==null)
            throw new Exception("Attempting to add mesh before setting GL context. Call setContext and pass it a GLAutoDrawable first.");

        if (meshRenderers.containsKey(name))
            return;

        //verify that the mesh has all the vertex attributes as specified in the map
        if (mesh.getVertexCount()<=0)
            return;
        K vertexData = mesh.getVertexAttributes().get(0);
      GL3 gl = glContext.getGL().getGL3();

      for (Map.Entry<String,String> e:shaderVarsToVertexAttribs.entrySet()) {
            if (!vertexData.hasData(e.getValue()))
                throw new IllegalArgumentException("Mesh does not have vertex attribute "+e.getValue());
        }
      util.ObjectInstance obj = new util.ObjectInstance(gl,
              shaderLocations,shaderVarsToVertexAttribs,mesh,name);

      meshRenderers.put(name,obj);
    }

    @Override
    public void addTexture(String name,String path)
    {
        TextureImage image = null;
        String imageFormat = path.substring(path.indexOf('.')+1);
        try {
            image = new TextureImage(path,imageFormat,name);
        } catch (IOException e) {
            throw new IllegalArgumentException("Texture "+path+" cannot be read!");
        }

        if (glContext != null) {
            Texture texture = image.getTexture();
            GL3 gl = glContext.getGL().getGL3();
            texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        }

        textures.put(name, image);
    }

    /**
     * Begin rendering of the scene graph from the root
     * @param root
     * @param modelView
     */
    @Override
    public void draw(INode root, Stack<Matrix4f> modelView) {
        GL3 gl = glContext.getGL().getGL3();

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        int location;
        location = shaderLocations.getLocation("image");

        if (location < 0) {
            throw new IllegalArgumentException("No shader variable for \" texture \"");
        } else {
            gl.glUniform1i(location, 0);
        }
        root.draw(this, modelView);
    }

    @Override
    public void dispose() {
        for (util.ObjectInstance s : meshRenderers.values()) {
            s.cleanup(glContext);
        }
    }

    /**
     * Draws a specific mesh.
     * If the mesh has been added to this renderer, it delegates to its correspond mesh renderer
     * This function first passes the material to the shader. Currently it uses the shader variable
     * "vColor" and passes it the ambient part of the material. When lighting is enabled, this method must
     * be overriden to set the ambient, diffuse, specular, shininess etc. values to the shader
     * @param name
     * @param material
     * @param transformation
     */
    @Override
    public void drawMesh(String name, util.Material material, String textureName, final Matrix4f transformation) {
        if (meshRenderers.containsKey(name)) {
            GL3 gl = glContext.getGL().getGL3();

            FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);
            FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
            int location;

            location = shaderLocations.getLocation("texturematrix");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" texturematrix \"");
            } else {
                Matrix4f textureTransform = new Matrix4f();
                gl.glUniformMatrix4fv(location, 1, false, textureTransform.get(fb16));
            }

            // Set the color (using material) for all vertices to be drawn for this object.
            location = shaderLocations.getLocation("material.ambient");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" material.ambient \"");
            } else {
                gl.glUniform3fv(location,1, material.getAmbient().get(fb4));
            }

            location = shaderLocations.getLocation("material.diffuse");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" material.diffuse \"");
            } else {
                gl.glUniform3fv(location,1, material.getDiffuse().get(fb4));
            }

            location = shaderLocations.getLocation("material.specular");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" material.specular \"");
            } else {
                gl.glUniform3fv(location,1, material.getSpecular().get(fb4));
            }

            location = shaderLocations.getLocation("material.shininess");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" material.shininess \"");
            } else {
                gl.glUniform1f(location, material.getShininess());
            }

            location = shaderLocations.getLocation("modelview");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" modelview \"");
            } else {
                gl.glUniformMatrix4fv(location, 1, false, transformation.get(fb16));
            }

            location = shaderLocations.getLocation("normalmatrix");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" normalmatrix \"");
            } else {
                Matrix4f normalmatrix = new Matrix4f(transformation);
                normalmatrix = normalmatrix.invert().transpose();
                gl.glUniformMatrix4fv(location, 1, false, normalmatrix.get(fb16));
            }

            if (textures.containsKey(textureName)) {
                textures.get(textureName).getTexture().bind(gl);
            }
            else if (textures.containsKey(WHITE_TEXTURE)) {
                textures.get(WHITE_TEXTURE).getTexture().bind(gl);
            } else {
                throw new IllegalArgumentException("No default \"white\" texture found. Please add accordingly.");
            }

            meshRenderers.get(name).draw(glContext);
        }
    }

    /**
     * Queries the shader program for all variables and locations, and adds them to itself
     * @param shaderProgram
     */
    @Override
    public void initShaderProgram(util.ShaderProgram shaderProgram,Map<String,String> shaderVarsToVertexAttribs) {
        Objects.requireNonNull(glContext);
        GL3 gl = glContext.getGL().getGL3();

        shaderLocations = shaderProgram.getAllShaderVariables(gl);
        this.shaderVarsToVertexAttribs = new HashMap<String,String>(shaderVarsToVertexAttribs);
        shaderLocationsSet = true;
    }

    @Override
    public int getShaderLocation(String name) {
        return shaderLocations.getLocation(name);
    }

    @Override
    public void light(List<Light> lights) {
        GL3 gl = glContext.getGL().getGL3();

        FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);
        int location;

        location = shaderLocations.getLocation("numLights");
        if (location < 0) {
            throw new IllegalArgumentException("No shader variable for \" numLights \"");
        } else {
            gl.glUniform1i(location, lights.size());
        }

        for (int i = 0; i < lights.size(); i++) {
            String name = "light[" + i + "]";
            Light light = lights.get(i);
            location = shaderLocations.getLocation(name + ".position");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" " + name + ".position" + " \"");
            } else {
                gl.glUniform4fv(location, 1, light.getPosition().get(fb4));
            }

            location = shaderLocations.getLocation(name + ".ambient");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" " + name + ".ambient" + " \"");
            } else {
                gl.glUniform3fv(location, 1, light.getAmbient().get(fb4));
            }

            location = shaderLocations.getLocation(name + ".diffuse");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" " + name + ".diffuse" + " \"");
            } else {
                gl.glUniform3fv(location, 1, light.getDiffuse().get(fb4));
            }

            location = shaderLocations.getLocation(name + ".specular");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" " + name + ".specular" + " \"");
            } else {
                gl.glUniform3fv(location, 1, light.getSpecular().get(fb4));
            }

            location = shaderLocations.getLocation(name + ".spotDirection");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" " + name + ".spotDirection" + " \"");
            } else {
                gl.glUniform4fv(location, 1, light.getSpotDirection().get(fb4));
            }

            location = shaderLocations.getLocation(name + ".cosSpotCutoff");
            if (location < 0) {
                throw new IllegalArgumentException("No shader variable for \" " + name + ".cosSpotCutoff" + " \"");
            } else {
                float cosSpotCutoff = (float) Math.cos(Math.toRadians(light.getSpotCutoff()));
                gl.glUniform1f(location, cosSpotCutoff);
            }
        }
    }



}