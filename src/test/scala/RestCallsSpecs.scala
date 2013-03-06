import com.limeblast.mydeatree._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class RestCallsSpecs extends FunSpec with ShouldMatchers {
  describe("REST Calls Tests") {
    val user = "temporary"
    val pw = "laimis"
    AppSettings.USERNAME = user
    AppSettings.PASSWORD = pw
    it("Check if GET works correctly") {
      //val ideas =  RESTCalls.retrieveObjects(AppSettings.IDEA_URL, classOf[Idea])
    }
  }
}

