package com.alamkanak.weekview

import android.os.Parcel
import android.os.Parcelable
import android.view.View.BaseSavedState
import java.util.Calendar

internal class SavedState : BaseSavedState {

    var numberOfVisibleDays: Int = 0
    var firstVisibleDate: Calendar? = null

    constructor(superState: Parcelable) : super(superState)

    constructor(
        superState: Parcelable,
        numberOfVisibleDays: Int,
        firstVisibleDate: Calendar?
    ) : super(superState) {
        this.numberOfVisibleDays = numberOfVisibleDays
        this.firstVisibleDate = firstVisibleDate
    }

    constructor(source: Parcel) : super(source) {
        numberOfVisibleDays = source.readInt()
        firstVisibleDate = source.readSerializable() as? Calendar
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(numberOfVisibleDays)
        out.writeSerializable(firstVisibleDate)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel) = SavedState(source)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}
