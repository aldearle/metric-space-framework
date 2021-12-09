package testloads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class GetSiftData implements GetDataResource {

    private static float[] getDataFromNextLine(LineNumberReader fr) throws Exception {
        float[] data = new float[128];
        int dim = 0;
        Scanner s1 = new Scanner(fr.readLine());
        s1.useDelimiter(",");

        while (s1.hasNextFloat()) {
            float n = s1.nextFloat();
            data[dim++] = n;
        }

        s1.close();

        if (dim < 128) {
            throw new Exception(dim + " is not enough data");
        }
        return data;
    }

    private static int getIdFromNextLine(LineNumberReader fr) throws Exception {
        final String line = fr.readLine();
        Scanner s = new Scanner(line);
        if (!"#objectKey".equals(s.next())) {
            s.close();
            if (line != null) {
                System.out.println("last line: " + line);
            }
            throw new Exception("no more data");
        }
        s.next();
        int id = s.nextInt();
        s.close();
        return id;
    }

    private static int[] getNNIdsFromNextLine(String line) {
        int[] res = new int[100];
        Scanner s = new Scanner(line);
        s.useDelimiter("[:,]\\s");
        for (int i = 0; i < 100; i++) {
            s.nextFloat();
            res[i] = s.nextInt();
        }
        s.close();
        return res;
    }

    private static int getQid(String line) {
        Scanner s = new Scanner(line);
        @SuppressWarnings("unused")
        String x = s.findInLine("\\(Q");
        String y = s.findInLine("[0-9]+");
        s.close();
        return Integer.parseInt(y);
    }

    private static int getQueryIdFromNextLine(LineNumberReader fr) throws Exception {
        final String line = fr.readLine();
        Scanner s = new Scanner(line);
        if (!"#objectKey".equals(s.next())) {
            s.close();
            if (line != null) {
                System.out.println("last line: " + line);
            }
            throw new Exception("no more data");
        }
        s.next();
        String id = s.next();

        s.close();
        if (id.charAt(0) != 'Q') {
            throw new Exception("query id not found");
        }
        return Integer.parseInt(id.substring(1));
    }

    private static double getThresholdFromNextLine(String line) {
        Scanner s = new Scanner(line);
        String end = s.findInLine("[0-9]+\\.[0-9]+:\\s[0-9]+$");
        String[] dub = end.split(":");
        s.close();
        return Double.parseDouble(dub[0]);
    }

    private static void writeObjectDataFile(Map<Integer, float[]> data, Iterator<Integer> keyIt, FileOutputStream fos)
            throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        Map<Integer, float[]> m = new TreeMap<>();
        for (int i = 0; i < 1000; i++) {
            final Integer id = keyIt.next();
            float[] f = data.get(id);
            m.put(id, f);
            if (i == 999) {
                System.out.println("writing " + id);
            }
        }
        oos.writeObject(m);
    }

    @SuppressWarnings("boxing")
    static Map<Integer, float[]> getSiftTextData(File f, boolean queryFile) throws IOException {
        Map<Integer, float[]> res = new TreeMap<>();
        LineNumberReader fr = new LineNumberReader(new FileReader(f));
        boolean finished = false;
        while (!finished) {
            try {
                int id = queryFile ? getQueryIdFromNextLine(fr) : getIdFromNextLine(fr);
                if (id % 1000 == 0) {
                    // System.out.println("getting " + id);
                }

                float[] data = getDataFromNextLine(fr);
                res.put(id, data);
            } catch (Exception e) {
                // System.out.println("finshed: " + e.getMessage());
                finished = true;
            }
        }

        fr.close();
        return res;
    }

    private String dataFilePath;
    private String queryFilePath;
    private String gtFilePath;
    private String objectDataPath;

    public GetSiftData(String filePath) {
        this.dataFilePath = filePath + "sift-1M.data";
        this.queryFilePath = filePath + "queryset-sift-1000.data";
        this.gtFilePath = filePath + "ground-truth-1000000_my.txt";
        this.objectDataPath = filePath + "extracted/";
    }

    public Map<Integer, float[]> getData(int noOfFiles) throws IOException, ClassNotFoundException {
        Map<Integer, float[]> m = new TreeMap<>();
        for (int file = 0; file < noOfFiles; file++) {
            FileInputStream fis = new FileInputStream(this.objectDataPath + file + ".obj");
            ObjectInputStream ois = new ObjectInputStream(fis);

            @SuppressWarnings("unchecked")
            Map<Integer, float[]> m1 = (Map<Integer, float[]>) ois.readObject();
            ois.close();
            m.putAll(m1);
        }
        return m;
    }

    /**
     * returns a list of 100 nearest neighbour ids for each query id
     */
    @Override
    @SuppressWarnings("boxing")
    public Map<Integer, int[]> getNNIds() throws IOException {
        Map<Integer, int[]> res = new TreeMap<>();
        LineNumberReader fr = new LineNumberReader(new FileReader(this.gtFilePath));
        fr.readLine();
        fr.readLine();

        boolean finished = false;
        while (!finished) {
            try {
                fr.readLine();
                int qid = getQid(fr.readLine());
                int[] nnids = getNNIdsFromNextLine(fr.readLine());
                res.put(qid, nnids);
            } catch (Exception e) {
                finished = true;
            }
        }

        fr.close();
        return res;
    }

    @Override
    public Map<Integer, float[]> getQueries() throws IOException, ClassNotFoundException {
        File f = new File(this.queryFilePath);
        return getSiftTextData(f, true);
    }

    @Override
    @SuppressWarnings("boxing")
    public Map<Integer, Double> getThresholds() throws IOException {
        Map<Integer, Double> res = new TreeMap<>();
        LineNumberReader fr = new LineNumberReader(new FileReader(this.gtFilePath));
        fr.readLine();// throw out two header lines
        fr.readLine();

        boolean finished = false;
        while (!finished) {
            try {
                fr.readLine(); // throw out first of every three lines
                int qid = getQid(fr.readLine());
                double thresh = getThresholdFromNextLine(fr.readLine());

                res.put(qid, thresh);
            } catch (Exception e) {
                finished = true;
            }
        }
        fr.close();
        if (res.size() != 1000) {
//			throw new RuntimeException("no threshold file");
        }
        return res;
    }

    @Override
    public void writeObjectDataFiles() throws IOException {
        File f = new File(this.dataFilePath);
        if (f.exists()) {
            //
        } else {
            f.mkdir();
        }
        Map<Integer, float[]> data = getSiftTextData(f, false);
        Iterator<Integer> keyIt = data.keySet().iterator();
        for (int file = 0; file < 1000; file++) {
            FileOutputStream fos = new FileOutputStream(this.objectDataPath + file + ".obj");
            writeObjectDataFile(data, keyIt, fos);
            fos.close();
        }
    }

    @Override
    public Map<Integer, float[]> getData() throws IOException, ClassNotFoundException {
        return getData(1000);
    }
}