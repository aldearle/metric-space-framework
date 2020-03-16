package testloads;

import java.io.IOException;
import java.util.Map;

public interface GetDataResource {

    Map<Integer, float[]> getData() throws IOException, ClassNotFoundException;

    Map<Integer, int[]> getNNIds() throws IOException;

    Map<Integer, float[]> getQueries() throws IOException, ClassNotFoundException;

    Map<Integer, Double> getThresholds() throws IOException;

    void writeObjectDataFiles() throws IOException;
}
