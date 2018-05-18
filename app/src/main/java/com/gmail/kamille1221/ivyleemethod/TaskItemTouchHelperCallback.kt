package com.gmail.kamille1221.ivyleemethod

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

/**
 * Created by Kamille on 2018-05-03.
 **/
class TaskItemTouchHelperCallback(private val mItemMoveListener: TaskItemTouchHelperCallback.OnItemMoveListener): ItemTouchHelper.Callback() {
	override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
		val dragFlags: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN
		// val swipeFlags: Int = ItemTouchHelper.START or ItemTouchHelper.END
		return makeMovementFlags(dragFlags, 0)
	}

	override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
		if (viewHolder != null && target != null) {
			mItemMoveListener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
		}
		return true
	}

	override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
		super.onSelectedChanged(viewHolder, actionState)
		if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
			mItemMoveListener.onItemMoved()
		}
	}

	override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
	}

	override fun isLongPressDragEnabled(): Boolean {
		return true
	}

	interface OnItemMoveListener {
		fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
		fun onItemMoved()
	}
}