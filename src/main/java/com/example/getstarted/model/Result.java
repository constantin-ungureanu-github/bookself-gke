package com.example.getstarted.model;

import java.util.List;

public class Result<K> {

    public String cursor;
    public List<K> result;

    public Result(final List<K> result, final String cursor) {
        this.result = result;
        this.cursor = cursor;
    }

    public Result(final List<K> result) {
        this.result = result;
        this.cursor = null;
    }
}
