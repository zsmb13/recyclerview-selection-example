package co.zsmb.example.rvselector

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.row_person.view.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val adapter = MyAdapter()
        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter

        val tracker = SelectionTracker.Builder<Long>(
                "my_selection_id",
                recyclerView,
                StableIdKeyProvider(recyclerView),
                MyDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage()
        ).build()

        adapter.tracker = tracker

        tracker.addObserver(object : SelectionTracker.SelectionObserver<Any>() {
            override fun onItemStateChanged(key: Any, selected: Boolean) {
                super.onItemStateChanged(key, selected)
                Log.d("SELECTION", "Update: key $key, selected $selected")
            }
        })

        button.setOnClickListener {
            val selectionStr = tracker.selection.joinToString()
            toast(selectionStr)
        }
    }

}

data class Person(val id: Long, val name: String)

class MyAdapter : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    lateinit var tracker: SelectionTracker<Long>

    val items = (1L..26L).map { Person(it, ('A' + it.toInt()).toString().repeat(10)) }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int) = items[position].id

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        if (tracker.isSelected(item.id)) {
            holder.itemView.setBackgroundColor(0xFFBBBBBB.toInt())
        }
        else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF.toInt())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.row_person, parent, false)
        return MyViewHolder(view)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvId = itemView.tvId
        val tvName = itemView.tvName

        fun getItemDetails() = MyItemDetails(adapterPosition, itemId)

        fun bind(person: Person) {
            tvId.text = person.id.toString()
            tvName.text = person.name
        }

    }

}

class MyDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val holder = recyclerView.getChildViewHolder(view)
            if (holder is MyAdapter.MyViewHolder) {
                return holder.getItemDetails()
            }
        }
        return null
    }

}

class MyItemDetails(private val position: Int, private val selectionKey: Long) : ItemDetailsLookup.ItemDetails<Long>() {
    override fun getPosition(): Int = position
    override fun getSelectionKey(): Long? = selectionKey
}
