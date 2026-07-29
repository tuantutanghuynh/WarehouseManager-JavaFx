package com.warehousemanager.exceptions;

public class AppException extends RuntimeException {

    // Constuctor nhận thông bso lỗi đơn thuần
    public AppException(String message) {
        super(message);
    }

    // constructor nhaajn thông báo lỗi kèm nguyên nhân gốc
    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

}
