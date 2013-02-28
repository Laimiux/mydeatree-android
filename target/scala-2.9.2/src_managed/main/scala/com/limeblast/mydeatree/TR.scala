package com.limeblast.mydeatree
import _root_.android.app.{Activity, Dialog}
import _root_.android.view.View

case class TypedResource[T](id: Int)
case class TypedLayout(id: Int)

object TR {
  val share_icon = TypedResource[android.widget.ImageView](R.id.share_icon)
  val idea_public = TypedResource[android.widget.ImageView](R.id.idea_public)
  val private_header_idea_public = TypedResource[android.widget.ImageView](R.id.private_header_idea_public)
  val new_idea_button = TypedResource[android.widget.Button](R.id.new_idea_button)
  val idea_text = TypedResource[android.widget.TextView](R.id.idea_text)
  val idea_submit_button = TypedResource[android.widget.Button](R.id.idea_submit_button)
  val idea_public_check_box = TypedResource[android.widget.CheckBox](R.id.idea_public_check_box)
  val header_linear = TypedResource[android.widget.LinearLayout](R.id.header_linear)
  val idea_header_container = TypedResource[android.widget.LinearLayout](R.id.idea_header_container)
  val link_to_register = TypedResource[android.widget.TextView](R.id.link_to_register)
  val submit_button = TypedResource[android.widget.Button](R.id.submit_button)
  val public_idea_list = TypedResource[android.widget.ListView](R.id.public_idea_list)
  val public_idea_favorite_layout = TypedResource[android.widget.LinearLayout](R.id.public_idea_favorite_layout)
  val textView1 = TypedResource[android.widget.TextView](R.id.textView1)
  val idea_owner = TypedResource[android.widget.TextView](R.id.idea_owner)
  val favorite_txt = TypedResource[android.widget.TextView](R.id.favorite_txt)
  val sign_up_button = TypedResource[android.widget.Button](R.id.sign_up_button)
  val private_header_idea_title = TypedResource[android.widget.TextView](R.id.private_header_idea_title)
  val share_txt = TypedResource[android.widget.TextView](R.id.share_txt)
  val idea_cancel_button = TypedResource[android.widget.Button](R.id.idea_cancel_button)
  val textView = TypedResource[android.widget.TextView](R.id.textView)
  val title_img = TypedResource[android.widget.ImageView](R.id.title_img)
  val forgot_password_text = TypedResource[android.widget.TextView](R.id.forgot_password_text)
  val username_edit = TypedResource[android.widget.EditText](R.id.username_edit)
  val sort_button = TypedResource[android.widget.Button](R.id.sort_button)
  val idea_title = TypedResource[android.widget.TextView](R.id.idea_title)
  val email_edit = TypedResource[android.widget.EditText](R.id.email_edit)
  val password2_edit = TypedResource[android.widget.EditText](R.id.password2_edit)
  val private_header_idea_date = TypedResource[android.widget.TextView](R.id.private_header_idea_date)
  val login_linear = TypedResource[android.widget.LinearLayout](R.id.login_linear)
  val status_title = TypedResource[android.widget.TextView](R.id.status_title)
  val status_text = TypedResource[android.widget.TextView](R.id.status_text)
  val forgot_password_link = TypedResource[android.widget.TextView](R.id.forgot_password_link)
  val password1_edit = TypedResource[android.widget.EditText](R.id.password1_edit)
  val public_idea_share_layout = TypedResource[android.widget.LinearLayout](R.id.public_idea_share_layout)
  val password_edit = TypedResource[android.widget.EditText](R.id.password_edit)
  val idea_date = TypedResource[android.widget.TextView](R.id.idea_date)
  val public_idea_date = TypedResource[android.widget.TextView](R.id.public_idea_date)
  val private_header_idea_text = TypedResource[android.widget.TextView](R.id.private_header_idea_text)
  val cancel_button = TypedResource[android.widget.Button](R.id.cancel_button)
  val favorite_icon = TypedResource[android.widget.ImageView](R.id.favorite_icon)
  val login_button = TypedResource[android.widget.Button](R.id.login_button)
  val idea_title_edit = TypedResource[android.widget.EditText](R.id.idea_title_edit)
  val footer_linear = TypedResource[android.widget.LinearLayout](R.id.footer_linear)
  val title_edit = TypedResource[android.widget.EditText](R.id.title_edit)
  val idea_text_edit = TypedResource[android.widget.EditText](R.id.idea_text_edit)
  val text_edit = TypedResource[android.widget.EditText](R.id.text_edit)
  val pager = TypedResource[android.support.v4.view.ViewPager](R.id.pager)
 object layout {
  val idea_edit_layout = TypedLayout(R.layout.idea_edit_layout)
 val idea_new = TypedLayout(R.layout.idea_new)
 val login_layout = TypedLayout(R.layout.login_layout)
 val main_controls = TypedLayout(R.layout.main_controls)
 val main_layout = TypedLayout(R.layout.main_layout)
 val need_user_layout = TypedLayout(R.layout.need_user_layout)
 val new_idea_notification = TypedLayout(R.layout.new_idea_notification)
 val private_idea_empty_header = TypedLayout(R.layout.private_idea_empty_header)
 val private_idea_entry = TypedLayout(R.layout.private_idea_entry)
 val private_idea_header = TypedLayout(R.layout.private_idea_header)
 val private_idea_list_layout = TypedLayout(R.layout.private_idea_list_layout)
 val private_ideas_layout = TypedLayout(R.layout.private_ideas_layout)
 val public_idea_entry = TypedLayout(R.layout.public_idea_entry)
 val public_ideas_layout = TypedLayout(R.layout.public_ideas_layout)
 val register_layout = TypedLayout(R.layout.register_layout)
 val search_ideas = TypedLayout(R.layout.search_ideas)
 }
}
trait TypedViewHolder {
  def findViewById( id: Int ): View
  def findView[T](tr: TypedResource[T]) = findViewById(tr.id).asInstanceOf[T]
}
trait TypedView extends View with TypedViewHolder
trait TypedActivityHolder extends TypedViewHolder
trait TypedActivity extends Activity with TypedActivityHolder
trait TypedDialog extends Dialog with TypedViewHolder
object TypedResource {
  implicit def layout2int(l: TypedLayout) = l.id
  implicit def view2typed(v: View) = new TypedViewHolder { 
    def findViewById( id: Int ) = v.findViewById( id )
  }
  implicit def activity2typed(a: Activity) = new TypedViewHolder { 
    def findViewById( id: Int ) = a.findViewById( id )
  }
  implicit def dialog2typed(d: Dialog) = new TypedViewHolder { 
    def findViewById( id: Int ) = d.findViewById( id )
  }
}
