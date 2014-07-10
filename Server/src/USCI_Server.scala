import com.esotericsoftware.kryonet.{Connection, Listener, Server}
import com.sun.org.apache.xml.internal.security.utils.Base64
import java.io.File
import java.security.SecureRandom
import java.util.logging.{Level, Logger}
import org.python.core.PyObject
import org.python.util.PythonObjectInputStream
import scala.beans.BeanProperty
import scala.collection.mutable

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/21/14
 * Time: 2:34 AM
 */

object USCI_Server {
  val log = Logger.getLogger("Server")
  def main(args: Array[String]) {
    log.info("Space: %s".format(args(0)))
    val s = new USCI_Server(args(0))
    s.setup()
    s.run()
  }

  val r = new SecureRandom()
  def getSalt: String = {
    val salt = new Array[Byte](32)
    r.nextBytes(salt)
    Base64.encode(salt)
  }
}

class connection_properties(){
  @BeanProperty var ip = "localhost"
  @BeanProperty var port = 43210

}

class server_properties(){
  @BeanProperty var title = "A server!"
  @BeanProperty var description = "The server is a lie!"
  @BeanProperty var download = "yoursite.whatever/directory.zip"
  @BeanProperty var local = true
  @BeanProperty var requires_login = true
  @BeanProperty var auto_shutdown = true
}

class Server_settings{
  @BeanProperty var server = new server_properties
  @BeanProperty var connection = new connection_properties
}

class USCI_Server(server_dir: String) extends Runnable{
  var server: Server = new Server()
  Packets.register(server.getKryo)
  server.addListener(new Listener {

    override def received(conn: Connection, packet: AnyRef){
      packet match {
        case login: PacketLogin =>
          val user = space.on_join(login.username, "")
          if (user != null){
            users += conn -> user
            conn.setName(login.username)
            conn.addListener(new Listener{
              override def received(conn: Connection, packet: AnyRef){
                packet match {
                  case keyboard: PacketKeyboard =>
                    user.event_keyboard(keyboard.key, keyboard.char, keyboard.state)
                  case mouse: PacketMouse =>
                    user.event_mouse(mouse.key, mouse.state, mouse.x, mouse.y)
                  case _ =>

                }
                super.received(conn, packet)
              }
            })
            USCI_Server.log.info("%s logged in...".format(login.username))
          }else{
            conn.close()
          }
        case mouse: PacketMouse =>
        case _ =>
      }
      super.received(conn, packet)
    }

    override def connected(conn: Connection){
      USCI_Server.log.info("New connection from %s!".format(conn.getRemoteAddressTCP.getHostName))
      if (props.server.requires_login){
        val salt = USCI_Server.getSalt
        conn.sendTCP(new PacketLoginRequest(salt))
      }else{
        conn.sendTCP(new PacketLoginRequest(""))
      }

      super.connected(conn)
    }

    override def disconnected(conn: Connection){
      users -= conn
      if (props.server.auto_shutdown && server.getConnections.size <= 0){
        USCI_Server.log.info("No users online, shutting down...")
        running = false
        server.stop()
      }
      super.disconnected(conn)
    }


  })

  var space: TraitSpace = null
  var running = true
  var users = mutable.Map[Connection, TraitUser]()
  var props = YamlUtil.readYamlFile("servers" + File.separator + server_dir + File.separator + "server.yml", classOf[Server_settings])
  if (props == null){
    props = new Server_settings
    YamlUtil.writeYamlFile(server_dir + File.separator + "server.yml", props)
  }

  def setup(){
    server.start()

    try{
      space = JythonFactory.create(classOf[TraitSpace],
        "servers" + File.separator + server_dir + File.separator + "Server.py", "test").asInstanceOf[TraitSpace]
      space.on_load()
      server.bind(props.connection.port)
    }catch{
      case e: Exception => USCI_Server.log.log(Level.SEVERE, "Unexpected error!", e)
        server.stop()
        throw e
    }

    USCI_Server.log.info("Server running on port %d...".format(props.connection.port))
  }

  def run(){
    while (running) {
      //space.on_update()
    }
    USCI_Server.log.info("Server shutting down...")
    System.exit(0)
  }

}
