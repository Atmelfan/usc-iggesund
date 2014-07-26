/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/27/14
 * Time: 9:17 PM
 */
class ClientEntity(renderer: Renderer) {
  var position_x = 0f
  var position_y = 0f
  var position_a = 0f
  var velocity_x = 0f
  var velocity_y = 0f
  var velocity_a = 0f

  def this(renderer: Renderer, packet: PacketCreateEntity){
    this(renderer)
    position_x = packet.position_x
    position_y = packet.position_y
    position_a = packet.position_a
    velocity_x = packet.velocity_x
    velocity_y = packet.velocity_y
    velocity_a = packet.velocity_a
  }

  def update(dt: Float)
  {
    position_x += velocity_x*dt
    position_y += velocity_y*dt
    position_a += velocity_a*dt
  }

  def draw(dt: Float){
  }

  def setAnimation(anim: String){

  }

}
