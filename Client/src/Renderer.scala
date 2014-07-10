import java.util.logging.{Logger, Level}
import java.util.regex.Pattern
import org.lwjgl.LWJGLException
import org.lwjgl.opengl.{GL20, DisplayMode, GL11, Display}
import org.lwjgl.util.glu.GLU
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
  def getSprite(name: String): Sprite = {
    null
  }

  val dummyTexture = new GLutil.texture(0,0,0)//Dummy texture returned for reasons...
  def getTexture(name: String): GLutil.texture = {
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


class Font(name: String){
  val regex = Pattern.compile("\\[(?'args'[0-9;]*)(?'esc'H|J|m|s|u)")
  def draw(x: Int, y: Int, msg: String, color: Int = 0xFFFFFFFF){
    val r = ((color >> 24) & 0xFF).toByte
    val g = ((color >> 16) & 0xFF).toByte
    val b = ((color >>  8) & 0xFF).toByte
    val a = ((color >>  0) & 0xFF).toByte
    GL11.glColor4b(r, g, b, a)
    /* Supported ANSI codes:
     * ESC<x>;<y>H  Move cursor to x, y
     * ESC<n>J      Clear screen, for only n==2 is supported (Note! Does not move cursor to 1,1)
     * ESC<n>m      Sets graphics mode(color, blinking etc...)
     * ESCs         Save cursor position
     * ESCu         Restore cursor position
     */
    var end = 0

  }

}
