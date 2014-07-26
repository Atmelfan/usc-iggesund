import java.io.{FileWriter, FileReader}
import java.util.logging.{Logger, Level}
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.{DumperOptions, Yaml}

/**
 * (c) GPA Robotics 2013, Science has no time for safety!
 * Project: usc-iggesund
 * User: gustav
 * Date: 6/21/14
 * Time: 2:03 PM
 */
object YamlUtil {

  def readYamlFile[T >: Null](s: String, clazz: Class[T]): T = {
    try{

      val yaml = new Yaml(new Constructor(clazz))
      yaml.load(new FileReader(s)).asInstanceOf[T]
    }catch{
      case e: Exception => Logger.getGlobal.log(Level.SEVERE, "Failed to read %s!".format(s), e)
      null
    }
  }

  def writeYamlFile(s: String, obj: AnyRef){
    try{
      val options = new DumperOptions()
      options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
      val yaml = new Yaml(options)
      yaml.dump(obj, new FileWriter(s))
    }catch{
      case e: Exception => Logger.getGlobal.log(Level.SEVERE, "Failed to write to %s!".format(s), e)
    }
  }
}
