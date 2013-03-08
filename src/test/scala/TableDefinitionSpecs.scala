import com.limeblast.scaliteorm.TableDefinition
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class TableDefinitionSpecs extends FunSpec with ShouldMatchers {
  val TABLE_NAME = "simple_table"
  val COLUMN_ONE = "column"
  val COLUMN_TWO = "column2"


  val column_one_def = "TEXT"
  val column_two_def = "TEXT not null"

  val db = "CREATE TABLE " + TABLE_NAME + " ( " + COLUMN_ONE + " " + column_one_def + ", " + COLUMN_TWO + " " + column_two_def + " );"

  describe("Database Table Definitions") {
    val table = new TableDefinition(TABLE_NAME)
    table insert (COLUMN_TWO -> column_two_def, COLUMN_ONE -> column_one_def)
    it(table.toString) {
       assert(db.equals(table.toString))
    }
  }
}
