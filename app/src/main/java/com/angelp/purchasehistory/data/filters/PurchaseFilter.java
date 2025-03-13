package com.angelp.purchasehistory.data.filters;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

import static com.angelp.purchasehistory.data.Constants.getDefaultFilter;

@Getter
@Setter
@AllArgsConstructor
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
    private List<CategoryFilter> categories = new ArrayList<>();
    private UUID userId;

    public PurchaseFilter() {
        setFrom(LocalDate.now().withDayOfMonth(1));
        setTo(LocalDate.now());
    }

    public PurchaseFilter(LocalDate singleDay) {
        this.from = singleDay;
        this.to = singleDay;
    }

    protected PurchaseFilter(Parcel in) {
        setFrom(getDateFromParcel(in));
        setTo(getDateFromParcel(in));

        this.categories = in.readParcelableList(this.categories, CategoryFilter.class.getClassLoader());
        String userString = in.readString();
        if (userString != null && !userString.isBlank())
            setUserId(UUID.fromString(userString));
    }

    public PurchaseFilter(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    public void setCategory(List<CategoryView> categories) {
        if (categories == null || categories.isEmpty()) {
            clearCategories();
            return;
        }
        this.categories = Arrays.stream(categories.toArray(new CategoryFilter[0])).collect(Collectors.toList());
    }

    public void setCategory(CategoryView category) {
        if (category == null) {
            clearCategories();
            return;
        }
        this.categories = Arrays.asList(new CategoryFilter(category));
    }
    public void clearCategories() {
        this.categories.clear();
    }

    public boolean isEmpty() {
        return from == null && to == null && (categories == null || categories.isEmpty()) && userId == null;
    }

    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder();
        if (from != null)
            builder.append("from=").append(from.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("&");
        if (to != null)
            builder.append("to=").append(to.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("&");
        if (categories != null && !categories.isEmpty())
            builder.append("categories=").append(categories.stream().map(String::valueOf).collect(Collectors.joining(","))).append("&");
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
        return from.format(dtf) + " - " + filterTo.format(dtf) + (categories.isEmpty() ? "" : "\nCategory:" + categories.stream().map(CategoryFilter::getName).collect(Collectors.joining(",")));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(from == null ? -1L : from.toEpochDay());
        dest.writeLong(to == null ? -1L : to.toEpochDay());
        dest.writeParcelableArray(categories.toArray(new CategoryFilter[0]), flags);
        dest.writeString(userId == null ? "" : userId.toString());
//        dest.writeParcelable(pageRequest);
    }

    private LocalDate getDateFromParcel(Parcel in) {
        long EpochDate = in.readLong();
        if (EpochDate != -1)
            return LocalDate.ofEpochDay(EpochDate);
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;

        PurchaseFilter that = (PurchaseFilter) object;

        if (!from.equals(that.from)) return false;
        if (!to.equals(that.to)) return false;
        if (!Objects.equals(categories, that.categories)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + (categories != null ? categories.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }

    public @NotNull PurchaseFilter copy() {
        PurchaseFilter purchaseFilter = new PurchaseFilter();
        purchaseFilter.setCategories(categories);
        purchaseFilter.setFrom(from);
        purchaseFilter.setTo(to);
        purchaseFilter.setUserId(userId);
        return purchaseFilter;
    }
}
