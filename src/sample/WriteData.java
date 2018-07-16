package sample;

import java.io.*;
import java.util.LinkedList;

public class WriteData {
    public static final String FILE = "rezultatai.txt";
    private static final String UTF8 = "UTF-8";

    // duomenų įrašymas į failą
    public static void writeData(LinkedList<String> data) {
        try (BufferedWriter file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE), UTF8))) {

            for (String item : data) {
                file.write(item);
                file.newLine();
            }
        } catch (IOException e) {
            System.out.println("Can't write data to file");
        }
    }
}
