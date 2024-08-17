package com.example.purchasehistory.data.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseFilter {
    private LocalDate from;
    private LocalDate to;
    private Long categoryId;
    private UUID userId;
    private PageRequest pageRequest;

    public boolean isEmpty() {
        return from == null  && to ==null && categoryId == null && userId == null;
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
        if (pageRequest != null)
            builder.append(pageRequest);
        return builder.toString();
    }
}
