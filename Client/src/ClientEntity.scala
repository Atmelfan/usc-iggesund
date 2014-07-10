/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/27/14
 * Time: 9:17 PM
 */
class ClientEntity {
  var sprite = ""
  var position_x = 0f
  var position_y = 0f
  var position_z = 0f
  var velocity_x = 0f
  var velocity_y = 0f
  var velocity_z = 0f
  var t = 0f

  def update(dt: Float)
  {
    position_x += velocity_x*dt
    position_y += velocity_y*dt
    position_z += velocity_z*dt
  }

  def draw(dt: Float){
    t += dt

  }

  def setAnimation(anim: String){
    t = 0f

  }

}
