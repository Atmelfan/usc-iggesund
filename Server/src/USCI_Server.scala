import com.almworks.sqlite4java.{SQLite, SQLiteConnection}
import com.esotericsoftware.kryonet.{Connection, Listener, Server}
import com.sun.org.apache.xml.internal.security.utils.Base64
import java.io.File
import java.security.SecureRandom
import java.util.logging.{SimpleFormatter, FileHandler, Level, Logger}
import org.python.core.PyException
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
  val fh = new FileHandler("logs/server.log")
  fh.setFormatter(new SimpleFormatter)
  log.addHandler(fh)
  def main(args: Array[String]) {
    log.info("Space: %s".format(args(0)))
    setupSqlite()
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

  def setupSqlite(){
    SQLite.setLibraryPath(getOsNatives)

  }

  def getOsNatives: String = {
    val os = System.getProperty("os.name").toLowerCase
    if (os.contains("win")){
      "libraries/natives/windows"
    }else if (os.contains("nix") || os.contains("nux")){
      "libraries/natives/linux"
    }else if (os.contains("mac")){
      "libraries/natives/macosx"
    }else if (os.contains("sol") || os.contains("sun")){
      "libraries/natives/solaris"
    }else{
      println("I have no idea what operating system this is! Hopefully linux natives work...")
      "libraries/natives/linux"
    }
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
      try{
        packet match {
          case login: PacketLogin =>
            val name = if(login.username.isEmpty) conn.getRemoteAddressTCP.toString else login.username
            val user = space.new_user(name, "")
            if (user != null){
              users += conn -> user
              conn.setName(name)
              user.set_interface(conn)
              space.on_join(user)
              USCI_Server.log.info("%s logged in...".format(name))
            }else{
              conn.close()
            }
          case _ =>
        }
      }catch{
        case pye: PyException =>
          USCI_Server.log.log(Level.WARNING, "Exception when calling python, check your world script...", pye)
        case _ =>
          USCI_Server.log.log(Level.SEVERE, "Unexpected exception!")
          System.exit(0)
      }
      super.received(conn, packet)
    }

    override def connected(conn: Connection){
      if (!ready){
        conn.sendTCP(new PacketConsoleMsg("Have some patience, jeez..."))
        conn.close()
      }
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
      USCI_Server.log.info("%s disconnected...".format(conn.toString))
      if (props.server.auto_shutdown && server.getConnections.size <= 0){
        USCI_Server.log.info("No users online, shutting down...")
        running = false
      }
      super.disconnected(conn)
    }


  })

  var ready = false
  var space: TraitSpace = null
  @volatile var running = true
  var users = mutable.Map[Connection, TraitUser]()
  var props = YamlUtil.readYamlFile("servers" + File.separator + server_dir + File.separator + "server.yml", classOf[Server_settings])
  if (props == null){
    props = new Server_settings
    YamlUtil.writeYamlFile(server_dir + File.separator + "server.yml", props)
    USCI_Server.log.warning("No \'server.yml\' found, created a default...")

  }



  def setup(){
    server.start()
    val save = new SQLiteConnection(new File("servers" + File.separator + server_dir + File.separator + "save.sqlite"))
    space = JythonFactory.create(classOf[TraitSpace],
      "servers" + File.separator + server_dir + File.separator + "Server.py", "test").asInstanceOf[TraitSpace]

    server.bind(props.connection.port)
    save.open(false)
    USCI_Server.log.info("Loading save...")
    space.on_load(save)
    save.dispose()
    USCI_Server.log.info("done!")
  }

  def run(){
    USCI_Server.log.info("Server running on port %d...".format(props.connection.port))
    val save = new SQLiteConnection(new File("servers" + File.separator + server_dir + File.separator + "save.sqlite"))
    save.open(true)
    USCI_Server.log.info("Backing up save...")
    val backup = save.initializeBackup(new File("servers" + File.separator + server_dir + File.separator + "save.old.sqlite"))
    backup.backupStep(-1)
    backup.dispose()
    USCI_Server.log.info("done!")

    try {
      USCI_Server.log.info("Loading save...")
      space.on_load(save)
      // TODO: remove when finished debugging on_join
      Thread.sleep(5000L)//Make it look like it's actually doing something...
      USCI_Server.log.info("done!")
      USCI_Server.log.info("Ready!")
      space.on_update()//Guarantee that it has updated atleast once before anyone joins...
      ready = true
      while (running) {
        Thread.sleep(50)
        space.on_update()
      }
      USCI_Server.log.info("Saving...")
      space.on_save(save)
      save.dispose()
      USCI_Server.log.info("done!")
    } catch {
      case e: Exception =>
        USCI_Server.log.log(Level.SEVERE, "Unexpected exception:", e)
    }finally {
      server.stop()
    }



    USCI_Server.log.info("Server shutting down...")
    //System.exit(0)
  }

  def isReady = ready

}
