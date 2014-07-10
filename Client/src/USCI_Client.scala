import com.esotericsoftware.kryonet.{Connection, Listener, Client}
import com.sun.org.apache.xml.internal.security.utils.Base64
import java.awt.GridLayout
import java.io._
import java.security.MessageDigest
import java.util.logging.{Level, Logger}
import javax.swing._
import org.lwjgl.input.{Mouse, Keyboard}
import org.lwjgl.LWJGLException
import org.lwjgl.opengl.{GL11, DisplayMode, Display}
import org.lwjgl.util.glu.GLU
import scala.beans.BeanProperty

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/21/14
 * Time: 2:31 AM
 */

object USCI_Client {
  val log = Logger.getLogger("Client")
  def main(args: Array[String]) {
    new USCI_Client().run()
  }

  val md = MessageDigest.getInstance("SHA-1")
  def hashPassword(password: String, salt: String): String = {
    Base64.encode(md.digest((password+salt).getBytes("UTF-8")))
  }
}

class window_properties(){
  @BeanProperty var title: String = "USC Iggesund"
  @BeanProperty var height: Int = 720
  @BeanProperty var width: Int = 1280
  @BeanProperty var fullscreen: Boolean = false
}

class engine_properties(){
  @BeanProperty var default: String = "_start"
}

class graphics_properties(){
  @BeanProperty var font_name: String = "whatever"
  @BeanProperty var font_width: Int = 8
  @BeanProperty var font_height: Int = 16
  @BeanProperty var override_resize: Boolean = true
}

class Client_settings{
  @BeanProperty var engine = new engine_properties
  @BeanProperty var window = new window_properties
  @BeanProperty var graphics = new graphics_properties
}

class USCI_Client(){
  var server = ""
  //Load config file or create a new
  var client: Client = new Client()
  Packets.register(client.getKryo)
  client.addListener(new Listener{

    override def received(conn: Connection, packet: AnyRef){
      packet match {
        case loginreq: PacketLoginRequest =>
          if (!loginreq.salt.isEmpty){
            USCI_Client.log.info("%s requires login, salt = %s".format(server, loginreq.salt))
            new Thread(){
              setDaemon(true)
              start()
              override def run(){
                val cred = password()
                conn.sendTCP(new PacketLogin(cred._1, USCI_Client.hashPassword(cred._2, loginreq.salt)))
              }
            }
          }else{
            conn.sendTCP(new PacketLogin("user", ""))
          }

        case _ =>
      }
      super.received(conn, packet)
    }

    override def disconnected(conn: Connection){

      super.disconnected(conn)
    }

  })

  var usci_server: USCI_Server = null
  var props = YamlUtil.readYamlFile("config.yml", classOf[Client_settings])
  if (props == null){
    props = new Client_settings
    YamlUtil.writeYamlFile("config.yml", props)
  }

  def run(){
    var stop = false
    setupLwjgl()
    Renderer.initDisplay(props.window.width, props.window.height, props.window.title)
    val splash = GLutil.getTexture("resources/textures/splash.png")
    val h = splash.height.toFloat / 2
    val w = splash.width.toFloat / 2
    splash.bind(){
      GLutil.glBegin(GL11.GL_QUADS){
        GL11.glTexCoord2f(0f, 1f); GL11.glVertex2f(-w, -h)
        GL11.glTexCoord2f(1f, 1f); GL11.glVertex2f( w, -h)
        GL11.glTexCoord2f(1f, 0f); GL11.glVertex2f( w,  h)
        GL11.glTexCoord2f(0f, 0f); GL11.glVertex2f(-w,  h)
      }
    }
    Renderer.updateDisplay()
    if(!connect(props.engine.default))
      System.exit(-1)
    try{
      while (!stop){
        stop = Renderer.updateDisplay()
        input()


      }
    }catch{
      case e: Exception => USCI_Client.log.log(Level.SEVERE, "Unexpected flux capacitor malfunction!", e)
    }finally {
      USCI_Client.log.info("Client shutting down...")
      client.stop()
      //If singleplayer is configured properly the server will shutdown by itself
      Display.destroy()
    }


  }

  def input(){
    while (Keyboard.next()) {
      val key = Keyboard.getEventKey
      val char = Keyboard.getEventCharacter
      val state = Keyboard.getEventKeyState
      if (key == Keyboard.KEY_F1){
        Renderer.toggleFullscreen()
      } else{
        client.sendTCP(new PacketKeyboard(key, char, state))
      }

    }
    while (Mouse.next()) {
      val key = Mouse.getEventButton
      val state = Mouse.getEventButtonState
      val x = Mouse.getEventX
      val y = Mouse.getEventY
      client.sendTCP(new PacketMouse(key, state, x, y))
    }
  }

  def connect(name: String): Boolean = {
    server = name
    val sprops = YamlUtil.readYamlFile("servers" + File.separator + name + File.separator + "server.yml", classOf[Server_settings])
    if (sprops == null){
      YamlUtil.writeYamlFile("servers" + File.separator + name + File.separator + "server.yml", new Server_settings)
      USCI_Client.log.warning("Failed to read settings for %s!".format(name))
      return false
    }

    val url = sprops.server.download
    if (url != null && !url.equals("")){
      download(url)
    }
    try {
      if (sprops.server.local){
        usci_server = new USCI_Server(name)
        usci_server.setup()
        new Thread(usci_server)
      }
    }catch{
      case e: Exception => USCI_Client.log.log(Level.WARNING, "Failed to start %s!".format(name), e)
      return false
    }

    try{
      client.start()
      client.connect(5000, sprops.connection.ip, sprops.connection.port)
    }catch{
      case e: Exception => USCI_Client.log.log(Level.WARNING, "Failed to connect to %s!".format(name), e)
      client.stop()
      return false
    }
    true
  }

  def download(url: String){

  }

  def setupLwjgl(){
    System.setProperty("org.lwjgl.librarypath", new File(getOsNatives).getAbsolutePath)
    System.setProperty("java.library.path", new File("libraries").getAbsolutePath)
    USCI_Client.log.info("OS Natives: " + System.getProperty("org.lwjgl.librarypath"))


  }

  def getOsNatives: String = {
    val os = System.getProperty("os.name").toLowerCase
    USCI_Client.log.info("OS name: %s version %s".format(os, System.getProperty("os.version")))
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


  def password(): (String, String) = {
    val userPanel = new JPanel()
    userPanel.setLayout(new GridLayout(2,2))

    //Labels for the textfield components
    val usernameLbl = new JLabel("Username:")
    val passwordLbl = new JLabel("Password:")

    val username = new JTextField()
    val passwordFld = new JPasswordField()

    //Add the components to the JPanel
    userPanel.add(usernameLbl)
    userPanel.add(username)
    userPanel.add(passwordLbl)
    userPanel.add(passwordFld)
    passwordLbl.setEnabled(false)
    passwordFld.setEnabled(false)

    //As the JOptionPane accepts an object as the message
    //it allows us to use any component we like - in this case
    //a JPanel containing the dialog components we want
    var input = JOptionPane.showConfirmDialog(null, userPanel, "Enter username & password:"
      ,JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)

    (username.getText, new String(passwordFld.getPassword))
  }

}
