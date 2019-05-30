package com.alamkanak.weekview

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import java.util.*

internal class SavedState : View.BaseSavedState {

    var numberOfVisibleDays: Int = 0
    var firstVisibleDate: Calendar? = null

    constructor(superState: Parcelable) : super(superState)

    constructor(source: Parcel) : super(source) {
        numberOfVisibleDays = source.readInt()
        firstVisibleDate = source.readSerializable() as Calendar
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(numberOfVisibleDays)
        out.writeSerializable(firstVisibleDate)
    }

    @JvmField
    val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

        override fun createFromParcel(source: Parcel): SavedState {
            return SavedState(source)
        }

        override fun newArray(size: Int): Array<SavedState?> {
            return arrayOfNulls(size)
        }
    }

}
