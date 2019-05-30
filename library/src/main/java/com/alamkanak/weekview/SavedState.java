package com.alamkanak.weekview;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.Calendar;

class SavedState extends View.BaseSavedState {

    final int numberOfVisibleDays;

    @Nullable
    final Calendar firstVisibleDate;

    SavedState(Parcelable superState,
               int numberOfVisibleDays, @Nullable Calendar firstVisibleDate) {
        super(superState);
        this.numberOfVisibleDays = numberOfVisibleDays;
        this.firstVisibleDate = firstVisibleDate;
    }

    private SavedState(Parcel in) {
        super(in);
        numberOfVisibleDays = in.readInt();
        firstVisibleDate = (Calendar) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        super.writeToParcel(destination, flags);
        destination.writeInt(numberOfVisibleDays);
        destination.writeSerializable(firstVisibleDate);
    }

    public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

        public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
        }

        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }

    };

}
