package me.roitgrund.meshwarpsyphon;

import jsyphon.JSyphonServer;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class TestServer {

    public static void main(String[] args) throws LWJGLException {
        JSyphonServer server = new JSyphonServer();
        Display.setDisplayMode(new DisplayMode(225, 225));
        Display.create();
        Display.setTitle("test server");

        server.initWithName("TestServer");

        IntBuffer intBuff =
                ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();

        ByteBuffer dummy = null;

        while (!Display.isCloseRequested()) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            GL11.glPointSize(40f);
            GL11.glBegin(GL11.GL_POINTS);
            GL11.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
            GL11.glVertex3f(0.0f, 0.0f, 0.0f);
            GL11.glEnd();

            int target = GL31.GL_TEXTURE_RECTANGLE;

            GL11.glEnable(target);

            GL11.glGenTextures(intBuff);

            GL11.glBindTexture(target, intBuff.get(0));

            GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(target, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(target, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

            GL11.glTexImage2D(target, 0, GL11.GL_RGBA8, 225, 225, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, dummy);

            GL11.glCopyTexSubImage2D(target, 0, 0, 0, 0, 0, 225, 225);

            server.publishFrameTexture(intBuff.get(0), target, 0, 0, 225, 225, 225, 225, false);

            GL11.glDeleteTextures(intBuff.get(0));

            intBuff.clear();
            intBuff.rewind();

            Display.update();
        }

        server.stop();
        Display.destroy();
    }
}
