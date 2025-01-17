package sgraph;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import util.Light;
import util.Material;
import util.PolygonMesh;
import util.TextureImage;

/**
 * Created by ashesh on 4/12/2016.
 */
public class RTScenegraphRenderer implements IScenegraphRenderer {
    private List<Light> lights;
    /**
     * A map to store all the textures
     */
    private Map<String, TextureImage> textures;

    private static final float FUDGE = 0.05f;
    private static final int MAXIMUM_BOUNCE_THRESHOLD = 5;

    public RTScenegraphRenderer() {
        textures = new TreeMap<String,TextureImage>();
    }

    @Override
    public void setContext(Object obj) throws IllegalArgumentException {
        throw new IllegalArgumentException("Not valid for this renderer");
    }

    @Override
    public void initShaderProgram(util.ShaderProgram shaderProgram, Map<String, String> shaderVarsToVertexAttribs) {
        throw new IllegalArgumentException("Not valid for this renderer");

    }

    @Override
    public int getShaderLocation(String name) {
        throw new IllegalArgumentException("Not valid for this renderer");

    }

    @Override
    public void addMesh(String name, PolygonMesh mesh) throws Exception {

    }

    public void initLightsInShader(List<Light> lights) {
        throw new IllegalArgumentException("Not valid for this renderer");
    }

    @Override
    public void draw(INode root, Stack<Matrix4f> modelView) {
        int i,j;
        int width = 800;
        int height = 800;
        float FOVY = 120.0f;
        Ray rayView = new Ray();

        this.lights = root.getLightsInView(modelView);

        BufferedImage output = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        rayView.start = new Vector4f(0,0,0,1);
        for (i=0;i<width;i++)
        {
            for (j=0;j<height;j++)
            {
                /*
                 create ray in view coordinates
                 start point: 0,0,0 always!
                 going through near plane pixel (i,j)
                 So 3D location of that pixel in view coordinates is
                 x = i-width/2
                 y = j-height/2
                 z = -0.5*height/tan(FOVY)
                */
                rayView.direction = new Vector4f(i-0.5f*width,
                        j-0.5f*height,
                        -0.5f*height/(float)Math.tan(Math.toRadians(0.5*FOVY)),
                        0.0f);

                HitRecord hitR = new HitRecord();
                Color color = raycast(rayView, root, modelView, hitR, 0);

                output.setRGB(i,height-1-j,color.getRGB());
            }
        }

        OutputStream outStream = null;

        try {
            outStream = new FileOutputStream("output/raytrace.png");
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not write raytraced image!");
        }

        try {
            ImageIO.write(output,"png",outStream);
            outStream.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write raytraced image!");
        }

    }

    private Color raycast(Ray rayView, INode root, Stack<Matrix4f> modelView, HitRecord hitRecord, int bounce) {
        root.intersect(rayView,modelView,hitRecord);

        if (hitRecord.intersected()) {
            float absorption = hitRecord.material.getAbsorption();
            Color materialColor = shade(hitRecord.point, hitRecord.normal, hitRecord.material, hitRecord.textureName,
                    hitRecord.texcoord, root, modelView);

            // Reflections
            if (bounce <= MAXIMUM_BOUNCE_THRESHOLD && hitRecord.material.getReflection() > 0) {
                Vector4f reflectionRayPosition = new Vector4f(hitRecord.point);

                Vector3f rayDirection = new Vector3f(rayView.direction.x, rayView.direction.y, rayView.direction.z);
                rayDirection.normalize();
                Vector3f normal = new Vector3f(hitRecord.normal.x, hitRecord.normal.y, hitRecord.normal.z);
                normal.normalize();

                Vector4f reflectionRayDirection = new Vector4f(new Vector3f(rayDirection).reflect(normal), 0);
                reflectionRayDirection.normalize();

                reflectionRayPosition.add(new Vector4f(reflectionRayDirection).mul(FUDGE));

                Ray reflectionRay = new Ray();
                reflectionRay.start = reflectionRayPosition;
                reflectionRay.direction = reflectionRayDirection;

                HitRecord reflectionHitRecord = new HitRecord();

                float reflection = hitRecord.material.getReflection();
                Color reflectionColor = raycast(reflectionRay, root, modelView, reflectionHitRecord, bounce + 1);

                return blendColors(absorption, materialColor, reflection, reflectionColor);
            } else {
                return materialColor;
            }
        } else {
            return Color.BLACK;
        }
    }

    /**
     * Blends the material color and reflection color using absorption and reflection coefficients.
     */
    private Color blendColors(float absorption, Color materialColor, float reflection, Color reflectionColor) {
        float[] materialRGB = materialColor.getColorComponents(null);
        float materialRed = absorption * materialRGB[0];
        float materialGreen = absorption * materialRGB[1];
        float materialBlue = absorption * materialRGB[2];

        float[] reflectionRGB = reflectionColor.getColorComponents(null);
        float reflectionRed = reflection * reflectionRGB[0];
        float reflectionGreen = reflection * reflectionRGB[1];
        float reflectionBlue = reflection * reflectionRGB[2];

        float blendRed = materialRed + reflectionRed;
        float blendGreen = materialGreen + reflectionGreen;
        float blendBlue = materialBlue + reflectionBlue;

        blendRed = Math.max(0, Math.min(1.0f, blendRed));
        blendGreen = Math.max(0, Math.min(1.0f, blendGreen));
        blendBlue = Math.max(0, Math.min(1.0f, blendBlue));

        return new Color(blendRed, blendGreen, blendBlue);
    }

    private Color shade(Vector4f point, Vector4f normal, Material material,
                        String textureName, Vector2f texcoord, INode root, Stack<Matrix4f> modelView) {
        Vector3f color = new Vector3f(0,0,0);

        for (int i=0;i<lights.size();i++)
        {
            Vector3f lightVec;
            Vector3f spotdirection = new Vector3f(
                    lights.get(i).getSpotDirection().x,
                    lights.get(i).getSpotDirection().y,
                    lights.get(i).getSpotDirection().z);


            if (spotdirection.length()>0)
                spotdirection = spotdirection.normalize();

            if (lights.get(i).getPosition().w!=0) {
                lightVec = new Vector3f(
                        lights.get(i).getPosition().x - point.x,
                        lights.get(i).getPosition().y - point.y,
                        lights.get(i).getPosition().z - point.z);
            }
            else
            {
                lightVec = new Vector3f(
                        -lights.get(i).getPosition().x,
                        -lights.get(i).getPosition().y,
                        -lights.get(i).getPosition().z);
            }
            lightVec = lightVec.normalize();

            // Shadows
            Vector4f shadowRayPosition = new Vector4f(point);
            Vector4f shadowRayDirection = new Vector4f(lightVec, 0).normalize();
            shadowRayPosition.add(new Vector4f(shadowRayDirection).mul(FUDGE));
            Ray shadowRay = new Ray();
            shadowRay.start = shadowRayPosition;
            shadowRay.direction = shadowRayDirection;
            HitRecord hitRecord = new HitRecord();
            root.intersect(shadowRay, modelView, hitRecord);
            if (hitRecord.intersected()) {
                continue;
            }

        /* if point is not in the light cone of this light, move on to next light */
            if (new Vector3f(lightVec).negate().dot(spotdirection)<=Math.cos(Math.toRadians(lights.get(i).getSpotCutoff())))
                continue;


            Vector3f normalView = new Vector3f(normal.x,normal.y,normal.z).normalize();

            float nDotL = normalView.dot(lightVec);

            Vector3f viewVec = new Vector3f(point.x,point.y,point.z).negate();
            viewVec = viewVec.normalize();

            Vector3f reflectVec = new Vector3f(lightVec).negate().reflect(normalView);
            reflectVec = reflectVec.normalize();

            float rDotV = Math.max(reflectVec.dot(viewVec),0.0f);

            Vector3f ambient = new Vector3f(
                    material.getAmbient().x * lights.get(i).getAmbient().x,
                    material.getAmbient().y * lights.get(i).getAmbient().y,
                    material.getAmbient().z * lights.get(i).getAmbient().z);

            Vector3f diffuse = new Vector3f(
                    material.getDiffuse().x * lights.get(i).getDiffuse().x * Math.max(nDotL,0),
                    material.getDiffuse().y * lights.get(i).getDiffuse().y * Math.max(nDotL,0),
                    material.getDiffuse().z * lights.get(i).getDiffuse().z * Math.max(nDotL,0));
            Vector3f specular;
            if (nDotL>0) {
                specular = new Vector3f(
                        material.getSpecular().x * lights.get(i).getSpecular().x * (float) Math.pow(rDotV, material.getShininess()),
                        material.getSpecular().y * lights.get(i).getSpecular().y * (float) Math.pow(rDotV, material.getShininess()),
                        material.getSpecular().z * lights.get(i).getSpecular().z * (float) Math.pow(rDotV, material.getShininess()));
            }
            else
            {
                specular = new Vector3f(0,0,0);
            }
            color = new Vector3f(color).add(ambient).add(diffuse).add(specular);
        }

        if (textures.containsKey(textureName)) {
            Vector4f colorFromTexture = textures.get(textureName).getColor(texcoord.x, 1 - texcoord.y);
            color = color.mul(colorFromTexture.x, colorFromTexture.y, colorFromTexture.z);
        }

        color.x = Math.min(color.x,1);
        color.y = Math.min(color.y,1);
        color.z = Math.min(color.z,1);

        return new Color((int)(255*color.x),(int)(255*color.y),(int)(255*color.z));
    }

    @Override
    public void drawMesh(String name, Material material, String textureName, Matrix4f transformation) {
        throw new IllegalArgumentException("Not valid for this renderer");
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
        textures.put(name,image);
    }

    @Override
    public void dispose() {

    }
}
