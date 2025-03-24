import service.DataProcessor;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        DataProcessor dataProcessor = new DataProcessor();
        dataProcessor.Process("src/main/resources/Location.json","src/main/resources/Metadata.json");

    }
}