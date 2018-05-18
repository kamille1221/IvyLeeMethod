package com.gmail.kamille1221.ivyleemethod

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Kamille on 2018-05-15.
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

	class VersionAsyncTask(private val packageManager: PackageManager, private val packageName: String, private val btnReference: WeakReference<Button>): AsyncTask<Void, Void, Boolean>() {
		private lateinit var current: String
		private lateinit var store: String
		override fun doInBackground(vararg params: Void?): Boolean {
			if (BuildConfig.DEBUG) {
				return false
			}
			current = packageManager.getPackageInfo(packageName, 0).versionName
			store = getMarketVersionFast(packageName)
			return true
		}

		override fun onPostExecute(result: Boolean) {
			super.onPostExecute(result)
			val btnUpdate: Button? = btnReference.get()
			btnUpdate?.visibility = if (result && current != store) {
				View.VISIBLE
			} else {
				View.GONE
			}
		}

		/**
		 * references by http://gun0912.tistory.com/8
		 */
		private fun getMarketVersionFast(packageName: String): String {
			var mData = ""
			var mVer: String
			try {
				val mUrl = URL("https://play.google.com/store/apps/details?id=$packageName")
				val mConnection = mUrl.openConnection() as HttpURLConnection
				mConnection.connectTimeout = 5000
				mConnection.useCaches = false
				mConnection.doOutput = true
				if (mConnection.responseCode == HttpURLConnection.HTTP_OK) {
					val mReader = BufferedReader(InputStreamReader(mConnection.inputStream))
					while (true) {
						val line = mReader.readLine() ?: break
						mData += line
					}
					mReader.close()
				}
				mConnection.disconnect()
			} catch (ex: Exception) {
				ex.printStackTrace()
				return ""
			}

			val startToken = "softwareVersion\">"
			val endToken = "<"
			val index = mData.indexOf(startToken)
			if (index == -1) {
				mVer = ""
			} else {
				mVer = mData.substring(index + startToken.length, index + startToken.length + 100)
				mVer = mVer.substring(0, mVer.indexOf(endToken)).trim { it <= ' ' }
			}
			return mVer
		}
	}
}
