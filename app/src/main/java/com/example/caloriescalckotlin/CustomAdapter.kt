package com.example.caloriescalckotlin

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.ArrayList

class CustomAdapter(context: Context, private val items: ArrayList<String?>) : ArrayAdapter<String>(context, R.layout.list_item, items) {

    var filteredItems: ArrayList<String?> = ArrayList(items)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.list_item, parent, false)

        val listItem = view.findViewById<TextView>(R.id.listItemTextView)

        if (position % 2 == 0) {
            val color = Color.argb(25, 0, 102, 94)
            listItem.setBackgroundColor(color)
        } else {
            val color = Color.argb(0, 0, 102, 94)
            listItem.setBackgroundColor(color)
        }

        if (position < filteredItems.size) {
            val item = filteredItems[position]
            listItem.text = item ?: ""
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val searchText = constraint?.toString()?.toLowerCase()
                val tempList = if (searchText.isNullOrEmpty()) {
                    ArrayList(items)
                } else {
                    items.filter { it?.toLowerCase()?.contains(searchText) ?: false } as ArrayList<String?>
                }
                results.values = tempList
                results.count = tempList.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems = results?.values as ArrayList<String?>
                notifyDataSetChanged()
            }
        }
    }
}
