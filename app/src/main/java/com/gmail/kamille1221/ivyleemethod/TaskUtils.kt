package com.gmail.kamille1221.ivyleemethod

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by David on 2018-05-15.
 */
object TaskUtils {
	private const val PREFERENCE_NAME = "com.gmail.kamille1221.ivyleemethod"
	private const val SHOW_COMPLETED_TASK = "SHOW_COMPLETED_TASK"
	private var sharedPreferences: SharedPreferences? = null

	private fun getSharedPreferences(context: Context): SharedPreferences? {
		if (sharedPreferences == null) {
			sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
		}
		return sharedPreferences
	}

	fun setShowCompletedTask(context: Context, completed: Boolean) {
		val editor = getSharedPreferences(context)?.edit()
		if (editor != null) {
			editor.putBoolean(SHOW_COMPLETED_TASK, completed)
			editor.apply()
		}
	}

	fun isShowCompletedTask(context: Context): Boolean {
		return getSharedPreferences(context)?.getBoolean(SHOW_COMPLETED_TASK, true) ?: true
	}

	fun dateIntToString(date: Int): String {
		val calendar: Calendar = Calendar.getInstance()
		val year: Int = date / 10000
		val month: Int = date / 100 % 100 - 1
		val dayOfMonth: Int = date % 100
		calendar.set(Calendar.YEAR, year)
		calendar.set(Calendar.MONTH, month)
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
		val dateFormat = SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMM. dd. yyyy."), Locale.getDefault())
		return dateFormat.format(Date(calendar.timeInMillis))
	}
}
