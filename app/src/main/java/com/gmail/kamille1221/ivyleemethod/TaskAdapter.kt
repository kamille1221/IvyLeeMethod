package com.gmail.kamille1221.ivyleemethod

import android.app.AlertDialog
import android.content.Context
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import kotlinx.android.synthetic.main.card_task.view.*
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import java.util.*

/**
 * Created by Kamille on 2018-05-03.
 **/
class TaskAdapter(mContext: Context, private var mTasks: RealmResults<Task>, var realm: Realm, autoUpdate: Boolean = true): RealmRecyclerViewAdapter<Task, RecyclerView.ViewHolder>(mContext, mTasks as OrderedRealmCollection<Task>?, autoUpdate), TaskItemTouchHelperCallback.OnItemMoveListener {
	private var tasks: List<Task>? = null

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
		return TaskHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_task, parent, false)).listen { position, _ ->
			val task: Task? = mTasks[position]
			if (task != null) {
				showTaskDetail(task)
			}
		}
	}

	override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
		if (tasks == null) {
			tasks = mTasks.toList()
		}
		if (fromPosition < toPosition) {
			for (i in fromPosition until toPosition) {
				Collections.swap(tasks, i, i + 1)
			}
		} else {
			for (i in fromPosition downTo toPosition + 1) {
				Collections.swap(tasks, i, i - 1)
			}
		}
		notifyItemMoved(fromPosition, toPosition)
		return true
	}

	override fun onItemMoved() {
		if (tasks != null && tasks!!.isNotEmpty()) {
			for (i in 0 until tasks!!.size) {
				val task: Task = tasks!![i]
				updateRealm(task.id, task.completed, task.title, task.content, i, task.date)
			}
		}
	}

	private fun <T: RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
		itemView.setOnClickListener {
			event.invoke(adapterPosition, itemViewType)
		}
		return this
	}

	private fun showTaskDetail(task: Task) {
		val resource: Int = R.layout.dialog_add_task
		val view = LayoutInflater.from(context).inflate(resource, null)
		val builder = AlertDialog.Builder(context)
		builder.setTitle(context.getString(R.string.title_modify_task))
		builder.setView(view)
		builder.setPositiveButton(context.getString(R.string.save), null)
		builder.setNeutralButton(context.getString(R.string.delete), null)
		builder.setNegativeButton(context.getString(R.string.cancel), null)
		view.etTitle.setText(task.title)
		view.etContent.setText(task.content)
		view.swCompleted.isChecked = task.completed
		view.llCompleted.visibility = View.VISIBLE
		val alertDialog: AlertDialog = builder.create()
		alertDialog.setOnShowListener { dialog ->
			val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			positiveButton.setOnClickListener {
				val completed: Boolean = view.swCompleted.isChecked
				val title: String = view.etTitle.text.toString()
				val content: String = view.etContent.text.toString()
				if (TextUtils.isEmpty(title)) {
					Toast.makeText(context, context.getString(R.string.toast_empty_task), Toast.LENGTH_SHORT).show()
				} else {
					updateRealm(task.id, completed, title, content, task.priority, task.date)
					dialog.dismiss()
				}
			}
			val neutralButton: Button = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
			neutralButton.setOnClickListener {
				val deleteBuilder = AlertDialog.Builder(context)
				deleteBuilder.setTitle(context.getString(R.string.title_delete_task))
				deleteBuilder.setMessage(context.getString(R.string.message_delete_task))
				deleteBuilder.setPositiveButton(context.getString(R.string.delete), { _, _ ->
					realm.beginTransaction()
					val deleteResult: RealmResults<Task> = realm.where(Task::class.java).equalTo("id", task.id).findAll()
					deleteResult.deleteAllFromRealm()
					realm.commitTransaction()
					alertDialog.dismiss()
				})
				deleteBuilder.setNegativeButton(context.getString(R.string.cancel), null)
				deleteBuilder.create().show()
			}
		}
		alertDialog.show()
	}

	@Synchronized
	private fun updateRealm(id: Int, completed: Boolean, title: String, content: String, priority: Int, date: Int) {
		realm.beginTransaction()
		val task: Task? = realm.where(Task::class.java).equalTo("id", id).findFirst()
		if (task != null) {
			task.completed = completed
			task.title = title
			task.content = content
			task.priority = priority
			task.date = date
			realm.copyToRealmOrUpdate(task)
			realm.commitTransaction()
		}
	}

	inner class TaskHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		fun bindTask(task: Task) {
			if (task.completed) {
				itemView.tvTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
			} else {
				itemView.tvTitle.paintFlags = Paint.HINTING_OFF
			}
			itemView.cbComplete.isChecked = task.completed
			itemView.cbComplete.setOnCheckedChangeListener { _, isChecked ->
				if (isChecked) {
					itemView.tvTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
				} else {
					itemView.tvTitle.paintFlags = Paint.HINTING_OFF
				}
				if (task.completed != isChecked) {
					updateRealm(task.id, !task.completed, task.title, task.content, task.priority, task.date)
				}
			}
			itemView.tvTitle.text = task.title
			itemView.tvDate.text = TaskUtils.dateIntToString(task.date)
		}
	}

	interface OnStartDragListener {
		fun onStartDrag(holder: TaskHolder)
	}
}
