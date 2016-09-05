package ru.rykov.rdd;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import ru.rykov.rdd.lambda.LambdaHolder;
import scala.Tuple2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Scanner;

/**
 * Hello world!
 */
public class App implements Serializable {
    public static final String APP_NAME = "sturdy-lambda";
    public static final String LAMBDA_HOLDER_PATTERN = "class Transformer implements " +
            "ru.rykov.rdd.lambda.DataTransformer " +
            "{ %1$s \n %2$s}";
    private final URI dataSet;
    private final LambdaHolder holder;

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println(String.format("Usage: %1$s <datafile> <map groovy script> <reduce groovy script>",
                    APP_NAME));
            System.exit(1);
        }

        JavaSparkContext sc = new JavaSparkContext(new SparkConf());
        App app = new App(URI.create(args[0]), URI.create(args[1]), URI.create(args[2]));
        System.out.println(app.calculate(sc));
    }


    public App(URI dataSet, URI mapFunc, URI reduceFunc) {
        this.dataSet = dataSet;
        String dataTransformerSource = String.format(LAMBDA_HOLDER_PATTERN,
                fileContents(mapFunc),
                fileContents(reduceFunc));
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
