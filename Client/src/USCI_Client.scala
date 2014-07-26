import com.esotericsoftware.kryonet.{Connection, Listener, Client}
import java.awt.GridLayout
import java.io._
import java.net.URL
import java.util.logging.{SimpleFormatter, FileHandler, Level, Logger}
import java.util.zip.ZipFile
import javax.swing._
import org.lwjgl.input.{Mouse, Keyboard}
import org.lwjgl.opengl.{GL11, Display}
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
  val fh = new FileHandler("logs/client.log")
  fh.setFormatter(new SimpleFormatter)
  log.addHandler(fh)
  def main(args: Array[String]) {
    new USCI_Client().run()
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
  @BeanProperty var font_height: Int = 32
  @BeanProperty var font_antialias: Boolean = true
}

class Client_settings{
  @BeanProperty var engine = new engine_properties
  @BeanProperty var window = new window_properties
  @BeanProperty var graphics = new graphics_properties
}

class USCI_Client(){
  var server = ""
  var console: Console = null
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
                if (conn != null){
                  conn.sendTCP(new PacketLogin(cred._1, ""))
                }
              }
            }
          }else{
            conn.sendTCP(new PacketLogin("", ""))
          }
        case msg: PacketConsoleMsg =>
          console.println(msg.msg)
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
    console = new Console(props.graphics.getFont_name, props.graphics.getFont_height, props.graphics.font_antialias,
      Display.getWidth, Display.getHeight)
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
        console.draw()
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
    val url = new URL(sprops.server.download)
    download(url, "servers" + File.separator + name)
    try {
      if (sprops.server.local){
        usci_server = new USCI_Server(name)
        usci_server.setup()
        new Thread(usci_server).start()
        var timeout = 0
        while (!usci_server.isReady && timeout < 120){
          Thread.sleep(1000)
          timeout += 1
        }
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

  def download(url: URL, dir: String){
    USCI_Client.log.info("Downloading %s from %s...".format(url.getFile, url))
    val conn = url.openConnection()
    conn.setReadTimeout(5000)
    conn.setConnectTimeout(5000)
    val last = conn.getLastModified
    val dirf = new File(dir)
    if(last < dirf.lastModified()){
      USCI_Client.log.info("Level up to date")
      return
    }

    try{
      val in = new BufferedInputStream(url.openStream(), 1024)
      val ddir = new File("downloading")
      ddir.mkdirs()
      val temp = File.createTempFile("download", ".tmp", ddir)
      val out = new BufferedOutputStream(new FileOutputStream(temp))
      val total = copyStream(in, out)
      out.close()
      USCI_Client.log.info("Downloaded %dBytes!".format(total))
      unpack(temp, dirf)
      USCI_Client.log.info("Extraction complete!")
    }catch{
      case e: Exception =>
        USCI_Client.log.log(Level.WARNING, "Failed to download and extract zip from %s".format(url), e)
    }
  }

  def copyStream(in: InputStream, out: OutputStream): Long = {
    val buffer = new Array[Byte](1024)
    var len = in.read(buffer)
    var tot = 0L
    while (len >= 0) {
      out.write(buffer, 0, len)
      tot += len
      len = in.read(buffer)
    }
    in.close()
    out.close()
    tot
  }

  def unpack(file: File, dir: File){
    USCI_Client.log.info("Extracting file %s to %s".format(file, dir))
    val zip = new ZipFile(file)
    val entries = zip.entries()
    while (entries.hasMoreElements){
      val entry = entries.nextElement()
      if(entry.isDirectory){
        val dirName = dir.getPath + File.separator + entry.getName
        val zdir = new File(dirName)
        zdir.mkdirs()
        USCI_Client.log.info("Extracting directory %s".format(zdir.toString))
      }else{
        val name = dir.getPath + File.separator + entry.getName
        if(name.endsWith(".yml") || name.endsWith(".py") || name.endsWith(".yml") || name.endsWith(".sqlite")){
          copyStream(zip.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(name)))
          USCI_Client.log.info("Extracting file %s, crc=%s".format(name, entry.getCrc))
        }
      }
    }
    zip.close()
  }

  def setupLwjgl(){
    System.setProperty("org.lwjgl.librarypath", new File(USCI_Server.getOsNatives).getAbsolutePath)
    USCI_Server.setupSqlite()
    USCI_Client.log.info("OS name: %s version %s".format(System.getProperty("os.name"), System.getProperty("os.version")))
    System.setProperty("java.library.path", new File("libraries").getAbsolutePath)
    USCI_Client.log.info("Libraries: " + System.getProperty("java.library.path"))
    USCI_Client.log.info("Natives: " + System.getProperty("org.lwjgl.librarypath"))

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
