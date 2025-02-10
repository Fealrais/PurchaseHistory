package com.angelp.purchasehistory.data.model;

import android.app.AlarmManager;
import android.os.Parcel;
import android.os.Parcelable;
import com.angelp.purchasehistorybackend.models.enums.ScheduledPeriod;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZoneId;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledNotification implements Parcelable {
    private Long id;
    private long timestamp;
    private long period;
    private BigDecimal price;
    private Long categoryId;
    private Boolean enabled;
    private String note;


    public ScheduledNotification(ScheduledExpenseView expense) {
        this.id = expense.getId();
        this.timestamp = getEpochMilli(expense);
        this.price = expense.getPrice();
        this.period = getInterval(expense.getPeriod());
        this.categoryId = expense.getCategory() == null ? null : expense.getCategory().getId();
        this.enabled = expense.isEnabled();
        this.note = expense.getNote();
    }
    public boolean isRepeating() {
        return period != -1;
    }

    protected ScheduledNotification(Parcel in) {
        price = new BigDecimal(in.readString());
        note = in.readString();
        long l = in.readLong();
        categoryId = l == -1 ? null : l;
        note = in.readString();
        timestamp = in.readLong();
        period = in.readLong();
        id = in.readLong();
        enabled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(price.toString());
        dest.writeString(note);
        dest.writeLong(categoryId == null ? -1 : categoryId);
        dest.writeString(note);
        dest.writeLong(timestamp);
        dest.writeLong(period);
        dest.writeLong(id);
        dest.writeByte((byte) (enabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScheduledNotification> CREATOR = new Creator<>() {
        @Override
        public ScheduledNotification createFromParcel(Parcel in) {
            return new ScheduledNotification(in);
        }

        @Override
        public ScheduledNotification[] newArray(int size) {
            return new ScheduledNotification[size];
        }
    };
    private static long getInterval(ScheduledPeriod period) {
        return switch (period) {
            case DAILY -> AlarmManager.INTERVAL_DAY;
            case WEEKLY -> AlarmManager.INTERVAL_DAY * 7;
            case MONTHLY -> AlarmManager.INTERVAL_DAY * 30;
            case YEARLY -> AlarmManager.INTERVAL_DAY * 365;
            case NEVER -> -1L;
        };
    }
    private static long getEpochMilli(ScheduledExpenseView notification) {
        return notification.getPeriod().getNextTimestamp(notification.getTimestamp()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
