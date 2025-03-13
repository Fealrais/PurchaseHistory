package com.angelp.purchasehistory.data.filters;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class CategoryFilter extends CategoryView implements Parcelable, MultiSelectOption {
    private boolean selected = false;

    public CategoryFilter(CategoryView c) {
        super(c.getId(), c.getName(), c.getColor());
    }
    protected CategoryFilter(Parcel in) {
        setId(in.readLong());
        setName(in.readString());
        setColor(in.readString());
    }

    public static final Creator<CategoryFilter> CREATOR = new Creator<>() {
        @Override
        public CategoryFilter createFromParcel(Parcel in) {
            return new CategoryFilter(in);
        }

        @Override
        public CategoryFilter[] newArray(int size) {
            return new CategoryFilter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(getId());
        dest.writeString(getName());
        dest.writeString(getColor());
    }
    @Override
    public Drawable getDrawable() {
        return new ColorDrawable(Integer.parseInt(getColor()));
    }
    @Override
    public String getLabel() {
        return getName();
    }
    @Override
    public Boolean isSelected() {
        return selected;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryFilter that = (CategoryFilter) o;
        return Objects.equals(getId(), that.getId());
    }
}
