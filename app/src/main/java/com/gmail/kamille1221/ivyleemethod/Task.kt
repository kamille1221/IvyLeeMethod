package com.gmail.kamille1221.ivyleemethod

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by David on 2018-05-03.
 **/

open class Task: RealmObject() {
	@PrimaryKey
	open var id: Int = -1
	open var completed: Boolean = false
	open var title: String = ""
	open var content: String = ""
	open var date: Int = 19700101
}
