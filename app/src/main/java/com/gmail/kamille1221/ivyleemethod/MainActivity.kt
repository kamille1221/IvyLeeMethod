package com.gmail.kamille1221.ivyleemethod

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import io.realm.exceptions.RealmMigrationNeededException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by Kamille on 2018-05-03.
 **/
class MainActivity: AppCompatActivity(), TaskAdapter.OnStartDragListener {
	private lateinit var mAdapter: TaskAdapter
	private lateinit var mItemTouchHelper: ItemTouchHelper
	private var showCompletedTask: Boolean = true
	private var realm: Realm by Delegates.notNull()
	private var realmConfig: RealmConfiguration by Delegates.notNull()
	private var selectedDate: Int = -1

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		Fabric.with(this, Crashlytics())
		initRealm()

		showCompletedTask = TaskUtils.isShowCompletedTask(this)

		val actionBar: Toolbar = toolbar
		setSupportActionBar(actionBar)

		cvTasks.setOnDateChangeListener { _, year, month, dayOfMonth ->
			selectedDate = year * 10000 + (month + 1) * 100 + dayOfMonth
			refreshTasks(selectedDate)
		}

		rvTasks.setHasFixedSize(true)
		rvTasks.layoutManager = LinearLayoutManager(this)
		refreshTasks(-1)

		fabAdd.setOnClickListener { addTask() }

		val mCallback = TaskItemTouchHelperCallback(mAdapter)
		mItemTouchHelper = ItemTouchHelper(mCallback)
		mItemTouchHelper.attachToRecyclerView(rvTasks)
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu_main, menu)
		val showAllTaskMenu: MenuItem? = menu?.findItem(R.id.action_show_all_task)
		showAllTaskMenu?.isChecked = TaskUtils.isShowCompletedTask(this)
		showAllTaskMenu?.icon = if (TaskUtils.isShowCompletedTask(this)) {
			getDrawable(R.drawable.ic_show_accent_24dp)
		} else {
			getDrawable(R.drawable.ic_hide_accent_24dp)
		}
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		return when (item?.itemId) {
			R.id.action_show_all_task -> {
				showCompletedTask = !showCompletedTask
				item.isChecked = showCompletedTask
				item.icon = if (showCompletedTask) {
					getDrawable(R.drawable.ic_show_accent_24dp)
				} else {
					getDrawable(R.drawable.ic_hide_accent_24dp)
				}
				TaskUtils.setShowCompletedTask(this, item.isChecked)
				refreshTasks(-1)
				true
			}
			R.id.action_about -> {
				startActivity(Intent(this, AboutActivity::class.java))
				true
			}
			else ->
				super.onOptionsItemSelected(item)
		}
	}

	override fun onStartDrag(holder: TaskAdapter.TaskHolder) {
		mItemTouchHelper.startDrag(holder)
	}

	private fun refreshTasks(date: Int) {
		val taskAdapter: TaskAdapter = if (date < 0) {
			TaskAdapter(this, getTasks(showCompletedTask), realm)
		} else {
			TaskAdapter(this, getTasks(showCompletedTask, date), realm)
		}
		mAdapter = taskAdapter
		mAdapter.notifyDataSetChanged()
		rvTasks.adapter = mAdapter
	}

	private fun getTasks(showCompletedTask: Boolean): RealmResults<Task> {
		return if (showCompletedTask) {
			realm.where(Task::class.java).findAll().sort("priority", Sort.ASCENDING)
		} else {
			realm.where(Task::class.java).equalTo("completed", false).findAll().sort("priority", Sort.ASCENDING)
		}
	}

	private fun getTasks(showCompletedTask: Boolean, date: Int): RealmResults<Task> {
		return if (showCompletedTask) {
			var result: RealmResults<Task> = realm.where(Task::class.java).lessThanOrEqualTo("date", date).findAll().sort("priority", Sort.ASCENDING)
			if (result.size > 6) {
				result = realm.where(Task::class.java).equalTo("date", date).findAll().sort("priority", Sort.ASCENDING)
				if (result.size > 6) {
					result.subList(0, 6) as RealmResults<Task>
				} else {
					result
				}
			} else {
				result
			}
		} else {
			var result: RealmResults<Task> = realm.where(Task::class.java).equalTo("completed", false).lessThanOrEqualTo("date", date).findAll().sort("priority", Sort.ASCENDING)
			if (result.size > 6) {
				result = realm.where(Task::class.java).equalTo("completed", false).equalTo("date", date).findAll().sort("priority", Sort.ASCENDING)
				if (result.size > 6) {
					result.subList(0, 6) as RealmResults<Task>
				} else {
					result
				}
			} else {
				result
			}
		}

	}

	private fun initRealm() {
		Realm.init(this)
		realmConfig = RealmConfiguration.Builder().build()
		realm = try {
			Realm.getInstance(realmConfig)
		} catch (e: RealmMigrationNeededException) {
			e.printStackTrace()
			Realm.deleteRealm(realmConfig)
			Realm.getInstance(realmConfig)
		}
	}

	private fun commitRealm(completed: Boolean, title: String, content: String, date: Int) {
		realm.beginTransaction()
		val id: Int = realm.where(Task::class.java).max("id")?.toInt() ?: 1
		val task = realm.createObject(Task::class.java, id + 1)
		task.completed = completed
		task.title = title
		task.content = content
		task.priority = 0
		task.date = date
		realm.commitTransaction()
	}

	private fun addTask() {
		var date: Int = if (selectedDate > 0) {
			selectedDate
		} else {
			val today: Calendar = Calendar.getInstance()
			today.get(Calendar.YEAR) * 10000 + (today.get(Calendar.MONTH) + 1) * 100 + today.get(Calendar.DAY_OF_MONTH)
		}
		val tasks = getTasks(false, date)
		if (tasks.size >= 6) {
			Toast.makeText(this, R.string.toast_max_tasks, Toast.LENGTH_SHORT).show()
			return
		}
		val resource: Int = R.layout.dialog_add_task
		val view = this.layoutInflater.inflate(resource, null)
		val builder = AlertDialog.Builder(this)
		builder.setTitle(getString(R.string.title_new_task))
		builder.setView(view)
		builder.setPositiveButton(getString(R.string.save), null)
		builder.setNegativeButton(getString(R.string.cancel), null)
		view.llCompleted.visibility = View.GONE
		view.etDate.setText(TaskUtils.dateIntToString(date))
		view.etDate.setOnClickListener {
			val selectedYear: Int = date / 10000
			val selectedMonth: Int = date / 100 % 100 - 1
			val selectedDayOfMonth: Int = date % 100
			val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
				date = year * 10000 + (month + 1) * 100 + dayOfMonth
				view.etDate.setText(TaskUtils.dateIntToString(date))
			}, selectedYear, selectedMonth, selectedDayOfMonth)
			datePickerDialog.show()
		}
		val alertDialog: AlertDialog = builder.create()
		alertDialog.setOnShowListener { dialog ->
			val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			positiveButton.setOnClickListener {
				val title: String = view.etTitle.text.toString()
				val content: String = view.etContent.text.toString()
				if (TextUtils.isEmpty(title)) {
					Toast.makeText(this, getString(R.string.toast_empty_task), Toast.LENGTH_SHORT).show()
				} else {
					commitRealm(false, title, content, date)
					dialog.dismiss()
				}
			}
		}
		alertDialog.show()
	}
}
