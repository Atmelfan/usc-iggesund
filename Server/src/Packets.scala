import com.esotericsoftware.kryo.Kryo

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/28/14
 * Time: 11:14 AM
 */
object Packets{
  def register(kryo: Kryo){
    kryo.register(classOf[scala.runtime.BoxedUnit])
    kryo.register(classOf[PacketLogin])
    kryo.register(classOf[PacketLoginRequest])
    kryo.register(classOf[PacketKeyboard])
    kryo.register(classOf[PacketMouse])
    kryo.register(classOf[PacketConsole])


  }
}

case class PacketLogin(username: String, hash: String){
  def this(){
    this("","")
  }
}

case class PacketLoginRequest(salt: String){
  def this(){
    this("")
  }
}

case class PacketKeyboard(key: Int, char: Char, state: Boolean){
  def this(){
    this(0, 0, false)
  }
}

case class PacketMouse(key: Int, state: Boolean, x: Int, y: Int){
  def this(){
    this(0, false, 0, 0)
  }
}

case class PacketConsole(x: Int, y: Int){
  def this(){
    this(0, 0)
  }
}

case class PacketConsoleMsg(msg: String){
  def this(){
    this("")
  }
}


case class PacketCreateEntity(var id: Int, sprite: String){

  def this(){
    this(0, "")
  }

  def this(entity: TraitEntity){
    this(entity.get_id, entity.get_sprite)
  }
}

case class PacketUpdateEntity(var id: Int, position_x: Float,
                              position_y: Float, position_a: Float,
                              velocity_x: Float, velocity_y: Float,
                              velocity_a: Float){
  def this(){
    this(0,0,0,0,0,0,0)
  }

  def this(entity: TraitEntity){
    this(entity.get_id, entity.get_position.x, entity.get_position.y, entity.get_position.z,
      entity.get_velocity.x, entity.get_velocity.y, entity.get_velocity.z)
  }
}

case class PacketEntityAnimation(var id: Int, anim: String){
  def this(){
    this(0, "")
  }

  def this(entity: TraitEntity, anim: String){
    this(entity.get_id, anim)
  }
}