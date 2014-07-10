/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/29/14
 * Time: 12:14 PM
 */
class Console(width: Int, height: Int) {
  var buffer = new Array[Int](width*height)

  def resize(width: Int, height: Int){
    //Create new buffer and copy old buffer into it

  }

  def getChar(index: Int): Char = {
    //Return lower 16bits (1char)
    ((buffer(index) >> 0) & 0x0000FFFF).toChar
  }

  def getFlags(index: Int): Byte = {
    ((buffer(index) >> 16) & 0xFF).toByte
  }

  def getColor(index: Int): Byte = {
    ((buffer(index) >> 24) & 0x0F).toByte
  }

  def getBackground(index: Int): Byte = {
    ((buffer(index) >> 28) & 0x0F).toByte
  }
}
