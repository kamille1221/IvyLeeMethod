package com.gmail.kamille1221.ivyleemethod

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity: AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_about)

		tvInspiration.movementMethod = LinkMovementMethod.getInstance()

		tvLicense.setOnClickListener { startActivity(Intent(this, OssLicensesMenuActivity::class.java)) }
	}
}
