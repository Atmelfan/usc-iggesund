import com.esotericsoftware.kryonet.{Connection, Listener}
import scala.collection.immutable.Map
/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 7/26/14
 * Time: 11:49 PM
 */
class ClientSpace(renderer: Renderer) extends Listener{

  override def received(conn: Connection, packet: AnyRef){
    packet match {
      case create: PacketCreateEntity =>
        val entity = new ClientEntity(renderer, create)
        addEntity(create.id, entity)
      case destroy: PacketDestroyEntity =>
        removeEntity(destroy.id)
      case update: PacketUpdateEntity =>
        exeEntity(update.id){ entity =>
          entity.position_x = update.position_x
          entity.position_y = update.position_y
          entity.position_a = update.position_a
          entity.velocity_x = update.velocity_x
          entity.velocity_y = update.velocity_y
          entity.velocity_a = update.velocity_a
        }
      case animation: PacketEntityAnimation =>
        exeEntity(animation.id){ entity =>
          entity.setAnimation(animation.anim)
        }
    }
    super.received(conn, packet)
  }

  var entities = Map[Int, ClientEntity]()

  def addEntity(id: Int, entity: ClientEntity){
    entities += id -> entity
  }

  def getEntity(id: Int): ClientEntity = {
    entities.getOrElse(id, null)
  }

  def exeEntity(id: Int)(exist: (ClientEntity) => Unit){
    val entity = entities.getOrElse(id, null)
    if (entity != null){
      exist(entity)
    }
  }

  def removeEntity(id: Int){
    entities -= id
  }

  def updateEntities(dt: Float){
    entities.foreach{entity =>
      entity._2.update(dt)
    }
  }

  def draw(dt: Float){
    entities.foreach{entity =>
      entity._2.draw(dt)
    }
  }

}
