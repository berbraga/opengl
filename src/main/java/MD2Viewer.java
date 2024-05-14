import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MD2Viewer {

  private long window;
  private MD2.Model md2Model;
  private int textureId;
  private float rotationAngle = 0.0f;
  public void run() throws IOException {
    System.out.println("Hello LWJGL " + Version.getVersion() + "!");
    init();
    loop();

    // Clean up
    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);
    glfwTerminate();
    glfwSetErrorCallback(null).free();
  }

  private void init() throws IOException {
    GLFWErrorCallback.createPrint(System.err).set();
    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

    window = glfwCreateWindow(800, 600, "MD2 Model Viewer", NULL, NULL);
    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
        glfwSetWindowShouldClose(window, true);
      }
    });

    try (MemoryStack stack = stackPush()) {
      IntBuffer pWidth = stack.mallocInt(1);
      IntBuffer pHeight = stack.mallocInt(1);
      glfwGetWindowSize(window, pWidth, pHeight);
      GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
      glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
    }

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1); // Enable v-sync
    GL.createCapabilities();

    // Load MD2 model
    md2Model = MD2.loadMD2("knight.md2");

    // Load texture
    textureId = loadTexture("knight.bmp");

    glfwShowWindow(window);
  }

  private int loadTexture(String filePath) throws IOException {
    BufferedImage image = ImageIO.read(new FileInputStream(filePath));
    int[] pixels = new int[image.getWidth() * image.getHeight()];
    image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

    ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        int pixel = pixels[y * image.getWidth() + x];
        buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
        buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
        buffer.put((byte) (pixel & 0xFF));         // Blue
        buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
      }
    }
    buffer.flip();

    int textureID = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, textureID);

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

    return textureID;
  }

  private void loop() {
    while (!glfwWindowShouldClose(window)) {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear the frame/depth buffer
      glEnable(GL_DEPTH_TEST);
      glDepthFunc(GL_LEQUAL);

      // Set up the viewport and projection matrix
      glViewport(0, 100, 300, 600);
      glMatrixMode(GL_PROJECTION);
      glLoadIdentity();
//      gluPerspective(45.0, (double) 800 / 600, 0.1, 100.0);

      glMatrixMode(GL_MODELVIEW);
      glLoadIdentity();
//      gluLookAt(0.0, 0.0, 10.0, // Camera position
//        0.0, 0.0, 0.0,  // Look at point
//        0.0, 1.0, 0.0); // Up vector
      glRotatef(rotationAngle, -2.0f, 1.0f, 1.0f);
      drawMD2Model();
      glfwSwapBuffers(window);
      glfwPollEvents();
      // Atualizar o ângulo de rotação
      rotationAngle += 0.5f;  // Ajuste este valor conforme necessário para controlar a velocidade de rotação
      if (rotationAngle > 360.0f) {
        rotationAngle -= 360.0f;  // Mantém o ângulo dentro de um intervalo de 0 a 360 graus
      }
    }
  }

  private void drawMD2Model() {
    if (md2Model == null || md2Model.f == null || md2Model.edge == null) {
      System.out.println("Model data is not loaded or incomplete.");
      return;
    }

    glEnable(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glBegin(GL_TRIANGLES);
    for (int i = 0; i < md2Model.tri.length; i++) {
      MD2.Triangle tri = md2Model.tri[i];
      for (int j = 0; j < 3; j++) {
        MD2.Vertex v = tri.v[j];
        MD2.PositionNormal pn = md2Model.f[0].pn[v.pn_index];
        glTexCoord2f(v.tc.s, v.tc.t);
        glVertex3f(pn.x, pn.y, pn.z);
      }
    }
    glEnd();
    glDisable(GL_TEXTURE_2D);
  }

  public static void main(String[] args) {
    try {
      new MD2Viewer().run();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
