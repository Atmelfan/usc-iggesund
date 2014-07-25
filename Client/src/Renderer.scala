import de.matthiasmann.twl.utils.PNGDecoder
import de.matthiasmann.twl.utils.PNGDecoder.Format
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.util.logging.Level
import org.lwjgl.LWJGLException
import org.lwjgl.opengl.{GL20, DisplayMode, GL11, Display}
import org.lwjgl.util.glu.GLU
import scala.collection.mutable
import scala.io.Source

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 7/5/14
 * Time: 8:20 PM
 */
object Renderer{
  var width = 0
  var height = 0
  var fullscreen = false

  def initDisplay(width: Int, height: Int, title: String){
    this.width = width
    this.height = height
    try {
      Display.setDisplayMode(new DisplayMode(width, height))
      Display.setResizable(true)
      Display.setTitle(title)
      Display.create()
      GL11.glEnable(GL11.GL_TEXTURE_2D)
      GL11.glEnable(GL11.GL_BLEND)
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
      resized()
    } catch {
      case e: LWJGLException => USCI_Client.log.log(Level.SEVERE, "Failed to create OpenGL window!", e)
        System.exit(0)
    }
  }

  def toggleFullscreen(){
    fullscreen = !fullscreen
    if (fullscreen){
      Display.setDisplayMode(Display.getDesktopDisplayMode)
      Display.setFullscreen(true)
    } else {
      Display.setDisplayMode(new DisplayMode(width, height))
      Display.setFullscreen(false)
    }
  }

  def updateDisplay(): Boolean = {
    Display.update()
    if (Display.wasResized()){
      resized()
    }
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f )
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    Display.isCloseRequested
  }

  def resized(){
    //println("resized!")
    val height: Int = Display.getHeight
    val width: Int = Display.getWidth
    GL11.glViewport(32,32, width, height)
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glLoadIdentity()
    GLU.gluOrtho2D(-width, width, -height, height)
  }
}

class Renderer {
  val textureCache = mutable.Map[String, Texture]()
  def getTexture(name: String): Texture = {
    textureCache.getOrElseUpdate(name, new Texture(name))
    null
  }
}

class Shader(vsh: String, fsh: String){
  val vsh_id = compile(vsh, GL20.GL_VERTEX_SHADER)
  val fsh_id = compile(vsh, GL20.GL_FRAGMENT_SHADER)
  val id = link(vsh_id, fsh_id)

  def bind[T](body: => T): T = {
    GL20.glUseProgram(id)
    try {
      body
    }finally {
      GL20.glUseProgram(0)
    }
  }

  def compile(name: String, typ: Int): Int = {
    var source = ""
    try {
      source = Source.fromFile(name).getLines().mkString
    }catch {
      case e: Exception =>
        USCI_Client.log.log(Level.WARNING, "Failed to load shader source from %s".format(name), e)
      return 0
    }

    val id = GL20.glCreateShader(typ)
    GL20.glShaderSource(id, source)
    GL20.glCompileShader(id)
    if(GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 0){
      GL20.glDeleteShader(id)
      println("Failed to compile shader \"%s\":\n%s".format(name, GL20.glGetShaderInfoLog(id, 256)))
      return 0
    }
    id
  }

  def link(vshi: Int, fshi: Int): Int = {
    val id = GL20.glCreateProgram()
    GL20.glAttachShader(id, vshi)
    GL20.glAttachShader(id, fshi)
    GL20.glLinkProgram(id)
    if(GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == 0){
      GL20.glDeleteProgram(id)
      println("Failed to link shader \"%s\" & \"%s\":\n%s".format(vsh, fsh, GL20.glGetShaderInfoLog(id, 256)))
      return 0
    }
    id
  }

  def destroy(){
    GL20.glDetachShader(id, vsh_id)
    GL20.glDetachShader(id, fsh_id)
    GL20.glDeleteProgram(id)
    GL20.glDeleteShader(vsh_id)
    GL20.glDeleteShader(fsh_id)
  }
}

class Texture(path: String, param: Int = GL11.GL_REPEAT, filter: Int = GL11.GL_NEAREST){
  val (id, height, width) = {
    val in = new FileInputStream(path)
    try {
      val decoder = new PNGDecoder(in)

      //System.out.println("width="+decoder.getWidth)
      //System.out.println("height="+decoder.getHeight)

      val buf = ByteBuffer.allocateDirect(4*decoder.getWidth*decoder.getHeight)
      decoder.decode(buf, decoder.getWidth*4, Format.RGBA)
      buf.flip()

      val tid = GL11.glGenTextures()
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, tid)
      GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, decoder.getWidth, decoder.getHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, param)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, param)

      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter)

      (tid, decoder.getWidth, decoder.getHeight)
    }catch{
      case e: Exception => println("Failed to load texture %s".format(path))
    }finally{
      in.close()
    }
    (0,0,0)
  }

  def bind[T](target: Int = GL11.GL_TEXTURE_2D)(body: => T): T = {
    GL11.glBindTexture(target, id)
    try {
      body
    }finally {
      GL11.glBindTexture(target, 0)
    }
  }

  def destroy(){
    GL11.glDeleteTextures(id)
  }

}


class Font(name: String){

  //Each texture contains 256 characters
  def cache = mutable.Map[Int, Texture]()



  def draw(x: Int, y: Int, msg: String, color: Int = 0xFFFFFFFF){
    val r = ((color >> 24) & 0xFF).toByte
    val g = ((color >> 16) & 0xFF).toByte
    val b = ((color >>  8) & 0xFF).toByte
    val a = ((color >>  0) & 0xFF).toByte
    GL11.glColor4b(r, g, b, a)
    /* Supported ANSI codes:
     * ESC<x>;<y>H  Move cursor to x, y
     * ESC<n>J      Clear screen, only n==2 is supported (Note! Moves cursor to 1,1)
     * ESC<n>m      Sets graphics mode(color, blinking etc...)
     * ESCs         Save cursor position
     * ESCu         Restore cursor position
     */

  }

}
