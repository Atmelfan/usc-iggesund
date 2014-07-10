import com.esotericsoftware.kryonet.Connection

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/28/14
 * Time: 10:52 AM
 */

trait TraitUser {
  def event_keyboard(key: Int, char: Char, state: Boolean)

  def event_mouse(button: Int, state: Boolean, x: Int, y: Int)
}
