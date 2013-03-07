import com.limeblast.androidhelpers.ProviderHelper
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class ProviderHelperSpecs extends FunSpec with ShouldMatchers {
  describe("Provider Helper Specs") {
    val test_map = Map("NAME1" -> 1,
      "NAME2" -> "sample", "NAME3" -> true)

    val intTester = "COLUMN_NAME=10"
    it("makeWhereClause tests String, Int") {
      val where = ProviderHelper.makeWhereClause(("COLUMN_NAME", 10))
      assert(where.equals(intTester))
    }

    val stringTester = "COLUMN_NAME='sample'"
    it("makeWhereClause tests String, String") {
      val where = ProviderHelper.makeWhereClause(("COLUMN_NAME", "sample"))
      assert(where.equals(stringTester))
    }

    val booleanTester = "COLUMN_NAME=1"
    it("makeWhereClause tests String, Boolean") {
      val where = ProviderHelper.makeWhereClause(("COLUMN_NAME", true))
      assert(where.equals(booleanTester))
    }

    val mapTester = "NAME3=1 AND NAME2='sample' AND NAME1=1"
    it("makeWhereClause should produce a string from a Map[String,Any]") {
      val where = ProviderHelper.makeWhereClause(Map("NAME1" -> 1,
        "NAME2" -> "sample", "NAME3" -> true))

      assert(where.equals(mapTester))
    }

    it(ProviderHelper.makeWhereClause(test_map)) {

    }


  }
}