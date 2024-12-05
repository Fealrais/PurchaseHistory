package com.angelp.purchasehistory.web.clients;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class WebException extends RuntimeException{
    Integer errorResource;

    public WebException(Integer errorMessage) {
        super(errorMessage.toString());
        this.errorResource = errorMessage;
    }
}
