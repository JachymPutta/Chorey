package com.chorey.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chorey.R
import com.chorey.data.model.HomeModel

class MenuRecyclerViewAdapter: RecyclerView.Adapter<MenuRecyclerViewAdapter.ViewHolder>()  {

    var onItemClick: ((HomeModel) -> Unit)? = null
    var homes: ArrayList<HomeModel> = arrayListOf();


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pointsView: TextView
        val membersView: TextView
        val homeNameView: TextView
        val iconView: ImageView

        init {
            // Define click listener for the ViewHolder's View.
            iconView = view.findViewById(R.id.homesListIcon)
            homeNameView = view.findViewById(R.id.homesListName)
            membersView = view.findViewById(R.id.homesListMembers)
            pointsView = view.findViewById(R.id.homesListPoints)

            view.setOnClickListener {
                onItemClick?.invoke(homes[adapterPosition])
            }
        }
    }

    /**
     * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.menu_recycler_row, parent, false)

        return ViewHolder(view)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return homes.size
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [ViewHolder.itemView] to reflect the item at the given
     * position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.iconView.setImageResource()
        holder.homeNameView.setText(homes[position].homeId)
//        holder.membersView.setText(homes[position].members.size)
//        holder.pointsView.setText(homes[position].chores.size)
       holder.membersView.setText(R.string.app_name)
        holder.pointsView.setText(R.string.app_name)
    }

}