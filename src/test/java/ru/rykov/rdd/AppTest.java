package ru.rykov.rdd;


import io.netty.util.internal.ThreadLocalRandom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.util.Random;

/**
 * Unit test for simple App.
 */
public class AppTest
{

    public void testApp() throws IOException {
        String filename = "data.txt";
        File file = new File(filename);
        file.createNewFile();
        for(int i = 0; i < 400; i++){
            try(Writer writer = Channels.newWriter(new FileOutputStream(
                    file.getAbsoluteFile(), true).getChannel(), "UTF-8")) {
                StringBuilder builder = new StringBuilder(10000*4);
                for(int j = 0; j < 10000; j++)
                {
                    builder.append(randInt(50, 100)).append("\t").append(randInt(50,100)).append("\n");
                }
                writer.append(builder);
            }
        }
    }

    public static int randInt(int min, int max) {

        Random rand = ThreadLocalRandom.current();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
