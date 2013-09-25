package com.matburt.mobileorg.test.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TestUtils {

    public static void writeStringAsFile(final String fileContents, String fileName) {
        try {
            FileWriter out = new FileWriter(new File(fileName));
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
        }
    }

    public static String readFileAsString(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(new File(fileName)));
            while ((line = in.readLine()) != null) stringBuilder.append(line);

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return stringBuilder.toString();
    }
}
