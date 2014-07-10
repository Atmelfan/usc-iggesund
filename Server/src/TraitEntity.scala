import javax.vecmath.Vector3f

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/21/14
 * Time: 5:13 PM
 */
trait TraitEntity {
  def get_id: Int

  def on_update()

  def on_destroy()

  def get_sprite: String

  def get_position: Vector3f

  def get_velocity: Vector3f

}
