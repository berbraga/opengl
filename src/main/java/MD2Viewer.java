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
import util.TextureLoader;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MD2Viewer {

  private long window;
  private MD2.Model md2Model;
  private int textureId;


  public void run() throws IOException {
    System.out.println("Hello LWJGL " + Version.getVersion() + "!");

    init();
//    md2Model = MD2.loadMD2("knight.md2"); // Supõe-se que esta função está corretamente definida na classe MD2.
//    System.out.println("arquivo MD2: " + md2Model.toString());
//    System.out.println("arquivo MD2 rodando");
    loop();
//    BufferedImage imggato = TextureLoader.loadImage("texturaGato.jpeg");
//    BufferedImage gatorgba = new BufferedImage(imggato.getWidth(), imggato.getHeight(), BufferedImage.TYPE_INT_ARGB);
//    gatorgba.getGraphics().drawImage(imggato, 0, 0, null);
//    textureId = TextureLoader.loadTexture(imggato);

//    textureId = loadTexture("knight.bmp");

    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);
    glfwTerminate();
    glfwSetErrorCallback(null).free();
  }
  private void init() throws IOException {
    GLFWErrorCallback.createPrint(System.err).set();
    if (!glfwInit())
      throw new IllegalStateException("Unable to initialize GLFW");

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

    window = glfwCreateWindow(800, 600, "MD2 Model Viewer", NULL, NULL);
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window");

    glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
        glfwSetWindowShouldClose(window, true);
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

    // Carrega a textura

    md2Model = MD2.loadMD2("knight.md2"); // Supõe-se que esta função está corretamente definida na classe MD2.
    System.out.println("arquivo MD2: " + md2Model.toString());
    System.out.println("arquivo MD2 rodando");
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
        buffer.put((byte) ((pixel >> 16) & 0xFF));
        buffer.put((byte) ((pixel >> 8) & 0xFF));
        buffer.put((byte) (pixel & 0xFF));
        buffer.put((byte) ((pixel >> 24) & 0xFF));
      }
    }

    buffer.flip();


    int textureID = glGenTextures();
    if (textureID == 0) {
      throw new RuntimeException("Failed to generate a new OpenGL texture object.");
    }
    System.out.println("fgasdlfjashdfjklhsadfa");
    glBindTexture(GL_TEXTURE_2D, textureID);

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

    return textureID;
  }

  private void loop() throws IOException {
    GL.createCapabilities();
    glClearColor(1f, 1f, 1f, 1f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
    md2Model = MD2.loadMD2("knight.md2"); // Supõe-se que esta função está corretamente definida na classe MD2.
    System.out.println("arquivo MD2: " + md2Model.toString());
    System.out.println("arquivo MD2 rodando");
    BufferedImage imggato = TextureLoader.loadImage("texturaGato.jpeg");
    glGenTextures();
    BufferedImage gatorgba = new BufferedImage(imggato.getWidth(), imggato.getHeight(), BufferedImage.TYPE_INT_ARGB);
    gatorgba.getGraphics().drawImage(imggato, 0, 0, null);
    textureId = TextureLoader.loadTexture(imggato);

    while (!glfwWindowShouldClose(window)) {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
      glMatrixMode(GL_PROJECTION);
      glLoadIdentity();

      glViewport(0,0,0,0);
      glMatrixMode(GL_MODELVIEW);
      glTranslatef(0.0f, 0.0f, -5.0f);
      glRotatef(45.0f, 1.0f, 0.0f, 0.0f);
      glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
      drawMD2Model();
      glfwSwapBuffers(window);
      glfwPollEvents();
    }
  }

  private void drawMD2Model() {
    if (md2Model == null || md2Model.f == null || md2Model.edge == null) {
      System.out.println("Model data is not loaded or incomplete.");
      return;
    }

    glEnable(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, 1);
    glBegin(GL_TRIANGLES);
    for (int i = 0; i < md2Model.tri.length; i++) {
      MD2.Triangle tri = md2Model.tri[i];
      for (int j = 0; j < 3; j++) {
        MD2.Vertex v = tri.v[j];
        MD2.PositionNormal pn = md2Model.f[0].pn[v.pn_index];
        glTexCoord2f(v.tc.s, v.tc.t);  // Set texture coordinates
        glVertex3f(pn.x, pn.y, pn.z);
      }
    }
    glEnd();
  }
  public static void main(String[] args) {
    try {
      new MD2Viewer().run();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

