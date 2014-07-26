import java.awt.Font
import org.lwjgl.opengl.GL11
import scala.collection.mutable.ArrayBuffer

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/29/14
 * Time: 12:14 PM
 */
class Console(fontName: String, fontSize: Int, antialias: Boolean, var width: Int, var height: Int) {
  var font = new Font(fontName, Font.PLAIN, fontSize)
  var ttf = new TrueTypeFont(font, antialias)
  var console = ArrayBuffer[String]("")
  var color = 0x008F00FF
  var (x,y) = (0,0)

  def println(s: String){
    print(s)
  }

  def print(s: String){
    for (i <- 0 to s.length-1){
      val c = s.charAt(i)
      if(c == '\r' || c == '\n'){
        console += ""
      }else if(c == '\b'){
        val t = console(console.length-1)
        if(t.length >= 1)
          console(console.length-1) = t.substring(0, t.length-1)
        else if(console.length > 1)
          console.remove(console.length-1)
      }else{
        console(console.length-1) += c
      }
    }

  }

  def click(x: Int, y: Int){
    var line = y/ttf.getLineHeight

  }

  def clear(){
  }

  def draw(){
    var x = 1
    console.foreach{ line =>
      val r = (color >> 24).toByte
      val g = (color >> 16).toByte
      val b = (color >>  8).toByte
      val a = (color >>  0).toByte
      GL11.glColor4ub(r, g, b, a)
      ttf.drawString(-width, height - x*ttf.getLineHeight, line, 1, 1)
      x += 1
    }

  }
}
