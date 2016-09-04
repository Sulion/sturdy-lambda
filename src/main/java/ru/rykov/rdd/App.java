package ru.rykov.rdd;

import groovy.lang.GroovyClassLoader;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import ru.rykov.rdd.lambda.LambdaHolder;
import scala.Tuple2;

import java.io.*;
import java.net.URI;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App implements Serializable
{
    private final URI dataSet;
    private final LambdaHolder holder;

    public static void main( String[] args ) throws IOException {
        String appName = "sturdy-lambda";
        String master = "local";
        SparkConf conf = new SparkConf().setAppName(appName).setMaster(master).setJars(new String[]{
                "/home/sulion/.m2/repository/org/codehaus/groovy/groovy-all/2.4.7/groovy-all-2.4.7.jar"
        });
        JavaSparkContext sc = new JavaSparkContext(conf);
        App app = new App(URI.create("file:///home/sulion/src/sturdy-lambda/data.txt"),
                URI.create("file:///home/sulion/src/sturdy-lambda/map.groovy"),
                URI.create("file:///home/sulion/src/sturdy-lambda/reduce.groovy"));
        System.out.println(app.calculate(sc));
    }


    public App(URI dataSet, URI mapFunc, URI reduceFunc) {
        this.dataSet = dataSet;
        GroovyClassLoader gcl = new GroovyClassLoader();
        String dataTransformerSource = String.format("class Transformer implements " +
                "ru.rykov.rdd.lambda.DataTransformer " +
                "{ %1$s \n %2$s}", fileContents(mapFunc), fileContents(reduceFunc));
        holder = new LambdaHolder(dataTransformerSource);
    }

    private String fileContents(URI fileName) {
        try {

            StringBuilder builder = new StringBuilder(100);

            Scanner scanner = new Scanner(new File(fileName)).useDelimiter("\\Z");
            while (scanner.hasNext()) {
                builder.append(scanner.next());
            }
            return builder.toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public int calculate(JavaSparkContext context) {

        JavaPairRDD<Integer, Integer> numPairs = context.textFile(
                dataSet.getPath()).map(line -> line.split("\t"))
                .mapToPair(arr -> new Tuple2<>(Integer.valueOf(arr[0]),
                        Integer.valueOf(arr[1])));
        return numPairs.map(p -> holder.map(p._1(), p._2()))
                .reduce(holder::reduce);
    }
}
