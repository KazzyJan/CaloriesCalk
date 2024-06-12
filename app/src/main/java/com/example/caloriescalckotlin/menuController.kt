import android.content.Context
import android.content.Intent
import android.view.View
import com.example.caloriescalckotlin.*

class MenuController(private val context: Context) {
    fun onMenuItemClicked(menuItem: String) {
        when (menuItem) {
            "calculate" -> {
                val intent = Intent(context, Calculate::class.java)
                context.startActivity(intent)
            }
            "account" -> {
                val intent = Intent(context, Account::class.java)
                context.startActivity(intent)
            }
            "productList" -> {
                val intent = Intent(context, ProductList::class.java)
                context.startActivity(intent)
            }
            "statistic" -> {
                val intent = Intent(context, Statistic::class.java)
                context.startActivity(intent)
            }
            "signOut" -> {
                var loging = Loging()
                loging.singOut()
                val intent = Intent(context, Loging::class.java)
                context.startActivity(intent)
            }
        }
    }
}
