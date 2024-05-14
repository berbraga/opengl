import java.io.IOException;
import java.io.RandomAccessFile;

public class MD2Model {
  private String filePath;
  private int numFrames;
  private int frameSize;
  private int numVertices;
  private int[][] faces;

  public MD2Model(String filePath) {
    this.filePath = filePath;
    loadHeader();
  }

  private void loadHeader() {
    try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
      MD2Header header = readHeader(file);
      numFrames = header.numFrames;
      frameSize = header.frameSize;
      numVertices = header.numVertices;
      loadFaces(file, header.offsetTriangles, header.numTriangles);
    } catch (IOException e) {
      System.err.println("Failed to load MD2 model: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void loadFaces(RandomAccessFile file, int offset, int count) throws IOException {
    faces = new int[count][3];
    file.seek(offset);
    for (int i = 0; i < count; i++) {
      faces[i][0] = file.readUnsignedShort();
      faces[i][1] = file.readUnsignedShort();
      faces[i][2] = file.readUnsignedShort();

    }
  }

  public float[][] loadFrameVertices(int frameIndex) {
    float[][] frameVertices = null;
    try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
      file.seek(MD2Header.SIZE + frameIndex * frameSize);
      frameVertices = readFrameVertices(file, numVertices);
    } catch (IOException e) {
      System.err.println("Failed to load frame: " + e.getMessage());
      e.printStackTrace();
    }
    return frameVertices;
  }
  public int[][] getFaces() {
    return faces;
  }
  private float[][] readFrameVertices(RandomAccessFile file, int numVertices) throws IOException {
    float[][] frameVertices = new float[numVertices][3];
    for (int i = 0; i < numVertices; i++) {
      frameVertices[i][0] = (file.read() & 0xFF) / 64.0f;
      frameVertices[i][1] = (file.read() & 0xFF) / 64.0f;
      frameVertices[i][2] = (file.read() & 0xFF) / 64.0f;
      file.skipBytes(1); // Pular o índice de normal
    }
    return frameVertices;
  }

  private MD2Header readHeader(RandomAccessFile file) throws IOException {
    byte[] buffer = new byte[MD2Header.SIZE];
    file.readFully(buffer);
    return new MD2Header(buffer);
  }

  static class MD2Header {
    public static final int SIZE = 68; // Tamanho do cabeçalho em bytes

    int ident, version, skinWidth, skinHeight, frameSize, numSkins, numVertices, numTexCoords, numTriangles, numGLCommands, numFrames;
    int offsetSkins, offsetTexCoords, offsetTriangles, offsetFrames, offsetGLCommands, offsetEnd;

    public MD2Header(byte[] buffer) {
      ident = readInt(buffer, 0);
      version = readInt(buffer, 4);
      skinWidth = readInt(buffer, 8);
      skinHeight = readInt(buffer, 12);
      frameSize = readInt(buffer, 16);
      numSkins = readInt(buffer, 20);
      numVertices = readInt(buffer, 24);
      numTexCoords = readInt(buffer, 28);
      numTriangles = readInt(buffer, 32);
      numGLCommands = readInt(buffer, 36);
      numFrames = readInt(buffer, 40);
      offsetSkins = readInt(buffer, 44);
      offsetTexCoords = readInt(buffer, 48);
      offsetTriangles = readInt(buffer, 52);
      offsetFrames = readInt(buffer, 56);
      offsetGLCommands = readInt(buffer, 60);
      offsetEnd = readInt(buffer, 64);
    }

    private int readInt(byte[] buffer, int offset) {
      return ((buffer[offset] & 0xFF) << 24) |
        ((buffer[offset + 1] & 0xFF) << 16) |
        ((buffer[offset + 2] & 0xFF) << 8) |
        (buffer[offset + 3] & 0xFF);
    }
  }
}
