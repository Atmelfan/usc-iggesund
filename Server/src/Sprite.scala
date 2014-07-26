import java.util
import scala.beans.BeanProperty

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/21/14
 * Time: 11:54 AM
 */

class sprite_size{
  @BeanProperty var height = 0f
  @BeanProperty var width = 0f
}

class sprite_atlas(){
  @BeanProperty var image = "default.png"
  @BeanProperty var height = 1
  @BeanProperty var width = 1
}

class sprite_shader{
  @BeanProperty var vertex = "default.vsh"
  @BeanProperty var fragment = "default.fsh"
}

class sprite_animation(){
  @BeanProperty var start = 0
  @BeanProperty var end = 0
  @BeanProperty var framerate = 0
  @BeanProperty var repeat = false

}

class Sprite {
  @BeanProperty var size = new sprite_size
  @BeanProperty var atlas = new sprite_atlas
  @BeanProperty var shader = new sprite_shader
  @BeanProperty var animation = "idle"
  @BeanProperty var animations = new util.HashMap[String, sprite_animation]()
}
