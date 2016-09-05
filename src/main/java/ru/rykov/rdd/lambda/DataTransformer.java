package ru.rykov.rdd.lambda;

/**
 * Created by sulion on 04.09.16.
 */
public interface DataTransformer {
    int map(Object a, Object b);
    int reduce(Object a, Object b);
}
