import com.almworks.sqlite4java.SQLiteConnection

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/21/14
 * Time: 5:50 PM
 */
trait TraitSpace {



  def on_load(sql: SQLiteConnection)

  def on_save(sql: SQLiteConnection)

  def on_update()

  def on_join(user: TraitUser)

  def new_user(username: String, hash: String): TraitUser

}
