package com.gmail.kamille1221.ivyleemethod

import android.app.AlertDialog
import android.content.Context
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import kotlinx.android.synthetic.main.card_task.view.*
import java.util.*

/**
 * Created by David on 2018-05-03.
 **/

class TaskAdapter(mContext: Context, private var mTasks: RealmResults<Task>, var realm: Realm, autoUpdate: Boolean = true): RealmRecyclerViewAdapter<Task, RecyclerView.ViewHolder>(mContext, mTasks as OrderedRealmCollection<Task>?, autoUpdate), TaskItemTouchHelperCallback.OnItemMoveListener {
	companion object {
		private const val CLICK: Int = 0
		private const val LONG_CLICK: Int = 1
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val task: Task? = mTasks[position]
		if (task != null) {
			(holder as TaskHolder).bindTask(task)
		}
	}

	override fun getItemCount(): Int {
		return mTasks.size
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return TaskHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_task, parent, false)).listen { position, _, clickType ->
			val task: Task? = mTasks[position]
			if (task != null) {
				when (clickType) {
					CLICK -> updateRealm(task.id, !task.completed, task.title, task.content, task.date)
					LONG_CLICK -> deleteRealm(task.id)
				}
			}
		}
	}

	override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
		Collections.swap(mTasks, fromPosition, toPosition)
		notifyItemMoved(fromPosition, toPosition)
		return true
	}

	private fun <T: RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, clickType: Int) -> Unit): T {
		itemView.setOnClickListener {
			event.invoke(adapterPosition, itemViewType, CLICK)
		}
		itemView.setOnLongClickListener {
			event.invoke(adapterPosition, itemViewType, LONG_CLICK)
			true
		}
		return this
	}

	private fun updateRealm(id: Int, completed: Boolean, title: String, content: String, date: Int) {
		realm.beginTransaction()
		val task: Task? = realm.where(Task::class.java).equalTo("id", id).findFirst()
		if (task != null) {
			task.completed = completed
			task.title = title
			task.content = content
			task.date = date
			realm.copyToRealmOrUpdate(task)
			realm.commitTransaction()
		}
	}

	private fun deleteRealm(id: Int) {
		val builder = AlertDialog.Builder(context)
		builder.setTitle(context.getString(R.string.title_delete_task))
		builder.setMessage(context.getString(R.string.message_delete_task))
		builder.setPositiveButton(context.getString(R.string.delete), { _, _ ->
			realm.beginTransaction()
			val deleteResult: RealmResults<Task> = realm.where(Task::class.java).equalTo("id", id).findAll()
			deleteResult.deleteAllFromRealm()
			realm.commitTransaction()
		})
		builder.setNegativeButton(context.getString(R.string.cancel), null)
		builder.create().show()
	}

	inner class TaskHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		fun bindTask(task: Task) {
			itemView.cbComplete.isChecked = task.completed
			if (task.completed) {
				itemView.tvTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
			} else {
				itemView.tvTitle.paintFlags = Paint.HINTING_OFF
			}
			itemView.tvTitle.text = task.title
			itemView.tvDate.text = TaskUtils.dateIntToString(task.date)
		}
	}

	interface OnStartDragListener {
		fun onStartDrag(holder: TaskHolder)
	}
}
