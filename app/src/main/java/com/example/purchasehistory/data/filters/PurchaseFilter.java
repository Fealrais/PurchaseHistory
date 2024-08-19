package com.example.purchasehistory.data.filters;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;

import static com.example.purchasehistory.data.Constants.getDefaultFilter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseFilter implements Parcelable {
    public static final Creator<PurchaseFilter> CREATOR = new Creator<>() {
        @Override
        public PurchaseFilter createFromParcel(Parcel in) {
            return new PurchaseFilter(in);
        }

        @Override
        public PurchaseFilter[] newArray(int size) {
            return new PurchaseFilter[size];
        }
    };
    private final DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    private LocalDate from;
    private LocalDate to;
    private Long categoryId;
    //    private PageRequest pageRequest;
    private UUID userId;

    protected PurchaseFilter(Parcel in) {
        setFrom(getDateFromParcel(in));
        setTo(getDateFromParcel(in));
        long categoryId = in.readLong();
        if (categoryId >= 0)
            setCategoryId(categoryId);
        String userString = in.readString();
        if (userString != null && !userString.isBlank())
            setUserId(UUID.fromString(userString));
    }

    public boolean isEmpty() {
        return from == null && to == null && categoryId == null && userId == null;
    }

    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder();
        if (from != null)
            builder.append("from=").append(from.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("&");
        if (to != null)
            builder.append("to=").append(to.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("&");
        if (categoryId != null)
            builder.append("categoryId=").append(categoryId).append("&");
        if (userId != null)
            builder.append("userId=").append(userId).append("&");
//        if (pageRequest != null)
//            builder.append(pageRequest);
        return builder.toString();
    }

    public String getReadableString() {
        PurchaseFilter def = getDefaultFilter();
        LocalDate from = getFrom() != null ? getFrom() : def.getFrom();
        LocalDate filterTo = getTo() != null ? getTo() : def.getTo();
        return "Filtered by period of:\n" + from.format(dtf) + " - " + filterTo.format(dtf) + "\n" + (getCategoryId() == null ? "" : "And by category");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(from == null ? -1L : from.toEpochDay());
        dest.writeLong(to == null ? -1L : to.toEpochDay());
        dest.writeLong(categoryId == null ? -1L : categoryId);
        dest.writeString(userId == null ? "" : userId.toString());
//        dest.writeParcelable(pageRequest);
    }

    private LocalDate getDateFromParcel(Parcel in) {
        long EpochDate = in.readLong();
        if (EpochDate != -1)
            return LocalDate.ofEpochDay(EpochDate);
        return null;
    }
}
