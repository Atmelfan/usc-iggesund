import org.lwjgl.opengl.GL11
import java.io.FileInputStream
import de.matthiasmann.twl.utils.PNGDecoder
import java.nio.ByteBuffer
import de.matthiasmann.twl.utils.PNGDecoder.Format

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/21/14
 * Time: 12:18 PM
 */
object GLutil {
  def glBegin[T](mode: Int)(body: => T): T = {
    GL11.glBegin(mode)
    try {
      body
    }finally {
      GL11.glEnd()
    }
  }

  var textures = collection.mutable.Map[String, texture]()
  def loadTexture(path: String, param: Int = GL11.GL_REPEAT, filter: Int = GL11.GL_NEAREST): texture = {

    val in = new FileInputStream(path)
    try {
      val decoder = new PNGDecoder(in)

      //System.out.println("width="+decoder.getWidth)
      //System.out.println("height="+decoder.getHeight)

      val buf = ByteBuffer.allocateDirect(4*decoder.getWidth*decoder.getHeight)
      decoder.decode(buf, decoder.getWidth*4, Format.RGBA)
      buf.flip()

      val id = GL11.glGenTextures()
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, id)
      GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, decoder.getWidth, decoder.getHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, param)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, param)

      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter)

      texture(id, decoder.getWidth, decoder.getHeight)
    } catch{
      case e: Exception => println("Failed to load texture %s".format(path))
      null
    } finally{
      in.close()
    }

  }

  def getTexture(path: String): texture = {
    textures.getOrElseUpdate(path, loadTexture(path))
  }

  case class texture(id: Int, width: Int, height: Int){
    def bind[T](target: Int = GL11.GL_TEXTURE_2D)(body: => T): T = {
      GL11.glBindTexture(target, id)
      try {
        body
      }finally {
        GL11.glBindTexture(target, 0)
      }
    }

  }
}
