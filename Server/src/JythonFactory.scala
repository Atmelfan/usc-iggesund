import org.python.util.PythonInterpreter
import org.python.core.{Py, PyObject}
import scala.collection.mutable

/** #
  * # #  #                  *             # # #  # #               *           # # # #  # # #             *         # # # # #  # # # #           *       # # # # # #  # # # # #         *     # # # # # #    # # # # # #       *   # # # # # #        # # # # # #     *                        # # # # # #    * # # # # # #
  * # # # # # #        # # # # # #     *     # # # # # #    # # # # # #       *       # # # # #  # # # # # #         *         # # # #  # # # # #           *           # # #  # # # #             *             # #  # # #               *               #  # #                 *                  #                   * (c) GPA Robotics 2013, Science has no time for safety!
  * Project: space
  * User: gustav
  * Date: 12/22/13
  * Time: 12:40 AM
  */
object JythonFactory {
  var classcache = mutable.HashMap[String, PyObject]()

  def create(interfaceType: Object, s: String, args: Array[PyObject] = new Array[PyObject](0)): Object = {
    val pyclass = classcache.getOrElseUpdate(s, loadPyClass(s))
    val pyobject = pyclass.__call__(args)
    pyobject.__tojava__(Class.forName(interfaceType.toString.substring(
      interfaceType.toString.indexOf(" ") + 1)))
  }

  def create(interfaceType: Object, s: String, arg: AnyRef): Object = {
    val pyclass = classcache.getOrElseUpdate(s, loadPyClass(s))
    val pyobject = pyclass.__call__(Py.java2py(arg))
    pyobject.__tojava__(Class.forName(interfaceType.toString.substring(
      interfaceType.toString.indexOf(" ") + 1)))
  }

  private def loadPyClass(s: String): PyObject = {
    val interpreter = new PythonInterpreter()
    interpreter.execfile(s)
    val name = s.substring(s.lastIndexOf("/") + 1, s.lastIndexOf("."))
    println(name)
    interpreter.get(name)
  }

}
