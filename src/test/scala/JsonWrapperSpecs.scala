import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import reflect.BeanInfo

@BeanInfo class SimpleObject(val name: String, val id: Int)

class JsonWrapperSpecs extends FunSpec with ShouldMatchers {
  describe("Json Wrapper Tests") {


    val string = "{\"name\":\"simple\",\"id\":1}"
    val simpleObj = new SimpleObject("simple", 1)
    /*
    it("Test Object Conversion to JSON") {
      val jsonString = JsonWrapper.convertObjectToJson(simpleObj)
      assert(string.equals(jsonString))
    }

    it("Test JSON conversion to JSON") {
      val simpleObj2 = JsonWrapper.getMainObject(string, classOf[SimpleObject])
      assert(simpleObj.name.equals(simpleObj2.name))
      assert(simpleObj.id == simpleObj2.id)
    }
    */
  }
}
