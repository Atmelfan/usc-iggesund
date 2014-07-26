import com.esotericsoftware.kryonet.{Listener, Connection}

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/28/14
 * Time: 10:52 AM
 */

class TraitUser{
  private var interface: Connection = null
  final def set_interface(interface: Connection){
    this.interface = interface
    interface.addListener(new Listener{
      final override def received(conn: Connection, packet: AnyRef){
        packet match {
          case keyboard: PacketKeyboard =>
            event_keyboard(keyboard.key, keyboard.char, keyboard.state)
          case mouse: PacketMouse =>
            event_mouse(mouse.key, mouse.state, mouse.x, mouse.y)
          case _ =>
        }
        super.received(conn, packet)
      }

      final override def connected(conn: Connection){
        super.connected(conn)
      }

      final override def disconnected(conn: Connection){
        super.disconnected(conn)
      }

      final override def idle(conn: Connection){
        super.disconnected(conn)
      }
    })
  }

  final def create_entity(entity: TraitEntity){
    if(interface != null)
      interface.sendTCP(new PacketCreateEntity(entity))
  }

  final def update_entity(entity: TraitEntity){
    if(interface != null)
      interface.sendTCP(new PacketUpdateEntity(entity))
  }

  final def destroy_entity(entity: TraitEntity){
    if(interface != null)
      interface.sendTCP(new PacketDestroyEntity(entity))
  }

  final def println(msg: String){
    if(interface != null)
      interface.sendTCP(new PacketConsoleMsg(msg))
  }

  final def disconnect(msg: String){
    if(interface != null)
      interface.close()
  }

  def connected(){

  }

  def event_keyboard(key: Int, char: Char, state: Boolean){

  }

  def event_mouse(button: Int, state: Boolean, x: Int, y: Int){

  }
}
