package com.example.smartsteps.Async;


public interface AsyncTaskCallback<T> {
    void handleResponse(T response);
    void handleFault(Exception e);
}