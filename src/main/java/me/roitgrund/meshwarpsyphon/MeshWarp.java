package me.roitgrund.meshwarpsyphon;

import jsyphon.JSyphonClient;
import jsyphon.JSyphonImage;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MeshWarp {

    private static final float ASPECT_RATIO = 4 / 3;

    public static void main(String[] args) throws IOException, LWJGLException {
        // Parse warp mesh from file passed as CLI argument.
        MeshWarpNode[][] mesh = MeshWarpNode.parseMesh(Paths.get(args[0]));

        // Set up OpenGL with a 4:3 aspect ratio.
        Display.setDisplayMode(new DisplayMode(300, 225));
        Display.create();
        Display.setTitle("meshwarp-syphon");

        // Set up Syphon to read test server output.
        JSyphonClient client = new JSyphonClient();
        client.init();
        client.setServerName("TestServer");

        // Loop over input frames available from Syphon.
        while (!Display.isCloseRequested()) {
            if (!client.hasNewFrame()) {
                continue;
            }

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            /**
             * Use Syphon output as texture.
             * In this case Syphon output is always a 225x225 texture.
             */
            JSyphonImage img = client.newFrameImageForContext();
            int texId = img.textureName();
            int texWidth = img.textureWidth();
            int texHeight = img.textureHeight();
            GL11.glEnable(GL31.GL_TEXTURE_RECTANGLE);
            GL11.glBindTexture(GL31.GL_TEXTURE_RECTANGLE, texId);

            /**
             * Set up scene geometry.
             * As specified in http://paulbourke.net/dataformats/meshwarp/, width range is (-1.333, 1.333) and
             * height range is (-1, 1). This matches the minimum and maximum coordinates found in the example mesh:
             * http://paulbourke.net/dataformats/meshwarp/xyuv.txt
             */
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(-ASPECT_RATIO, ASPECT_RATIO, -1, 1, 1, -1);

            /**
             * Draw rectangles in between mesh nodes.
             * Lifted verbatim from http://paulbourke.net/dataformats/meshwarp/ EXCEPT that OpenGL seems to expect
             * (0, textureWidth) / (0, textureHeight) coordinates in glTexCoord2f calls, so we multiply u and v by
             * width and height respectively.
             */
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glBegin(GL11.GL_QUADS);
            for (int i = 0; i < mesh.length - 1; i++) {
                for (int j = 0; j < mesh[0].length - 1; j++) {
                    if (mesh[i][j].i < 0 || mesh[i + 1][j].i < 0 || mesh[i + 1][j + 1].i < 0 || mesh[i][j + 1].i < 0) {
                        continue;
                    }

                    GL11.glColor3f(mesh[i][j].i, mesh[i][j].i, mesh[i][j].i);
                    GL11.glTexCoord2f(mesh[i][j].u * texWidth, mesh[i][j].v * texHeight);
                    GL11.glVertex2f(mesh[i][j].x, mesh[i][j].y);

                    GL11.glColor3f(mesh[i + 1][j].i, mesh[i + 1][j].i, mesh[i + 1][j].i);
                    GL11.glTexCoord2f(mesh[i + 1][j].u * texWidth, mesh[i + 1][j].v * texHeight);
                    GL11.glVertex2f(mesh[i + 1][j].x, mesh[i + 1][j].y);

                    GL11.glColor3f(mesh[i + 1][j + 1].i, mesh[i + 1][j + 1].i, mesh[i + 1][j + 1].i);
                    GL11.glTexCoord2f(mesh[i + 1][j + 1].u * texWidth, mesh[i + 1][j + 1].v * texHeight);
                    GL11.glVertex2f(mesh[i + 1][j + 1].x, mesh[i + 1][j + 1].y);

                    GL11.glColor3f(mesh[i][j + 1].i, mesh[i][j + 1].i, mesh[i][j + 1].i);
                    GL11.glTexCoord2f(mesh[i][j + 1].u * texWidth, mesh[i][j + 1].v * texHeight);
                    GL11.glVertex2f(mesh[i][j + 1].x, mesh[i][j + 1].y);
                }
            }
            GL11.glEnd();
            Display.update();
        }

        client.stop();
        Display.destroy();
    }

    private static final class MeshWarpNode {
        public final float x;
        public final float y;
        private final float u;
        private final float v;
        public final float i;

        private MeshWarpNode(float x, float y, float u, float v, float i) {
            this.x = x;
            this.y = y;
            this.u = u;
            this.v = v;
            this.i = i;
        }

        public static MeshWarpNode[][] parseMesh(Path path) throws IOException {
            List<String> lines = Files.readAllLines(path);
            if (Integer.parseInt(lines.get(0).trim()) != 2) {
                throw new IllegalArgumentException();
            }

            int nx = Integer.parseInt(lines.get(1).split("\\s")[0]);
            int ny = Integer.parseInt(lines.get(1).split("\\s")[1]);

            MeshWarpNode[][] mesh = new MeshWarpNode[nx][ny];

            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny; j++) {
                    String[] splitLine = lines.get(2 + ny * i + j).split("\\s");
                    mesh[i][j] = new MeshWarpNode(
                            Float.parseFloat(splitLine[0]),
                            Float.parseFloat(splitLine[1]),
                            Float.parseFloat(splitLine[2]),
                            Float.parseFloat(splitLine[3]),
                            Float.parseFloat(splitLine[4]));
                }
            }
            return mesh;
        }
    }
}
