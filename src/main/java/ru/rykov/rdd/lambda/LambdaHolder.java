package ru.rykov.rdd.lambda;

import groovy.lang.GroovyClassLoader;

import java.io.Serializable;

/**
 * Created by sulion on 04.09.16.
 */
public class LambdaHolder implements Serializable, DataTransformer {
    private transient DataTransformer transformer;
    private final String code;
    private transient volatile boolean instantiated = false;

    public LambdaHolder(String code) {
        this.code = code;
    }

    private synchronized void instantiate() {
        if(instantiated)
            return;
        GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader());
        Class<DataTransformer> dataTransformerClass = gcl.parseClass(this.code);//check it compiles
        try {
            transformer = dataTransformerClass.newInstance();
        } catch (InstantiationException| IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        instantiated = true;//volatile write 00xxx
    }

    @Override
    public int map(Object a, Object b) {
        if(!instantiated) {//volatile read 00xxx
            instantiate();
        }
        return transformer.map(a,b);
    }

    @Override
    public int reduce(Object a, Object b) {
        if(!instantiated) {//volatile read 00xxx
            instantiate();
        }
        return transformer.reduce(a, b);
    }
}
