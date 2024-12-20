
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import ut.Helper;


public class Client {

    public static int div = 32;
    static Properties config = Helper.readPropertiesFile("config/config.ini");
    static int numRows = Integer.parseInt(config.getProperty("num_rows"));
    static int p = (int) (numRows);
    public static long q = (long) p *div;

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public static long interpolate(long[] result1, long[] result2, long[] result3, int numBits, int bits) {
        long value1 = result1[0];
        long value2 = result2[0];
        long value3 =result3[0];
       long difference = Math.abs(value1-value2-value3);
        //long result = ((difference%p)+p)%p;
        long result=difference;

        return result;
    }




    public static long[] sendToServer(Object data, ObjectOutputStream outputStream, ObjectInputStream  inputStream) {
        long [] results = null;
        try {
            // sending data to server
            outputStream.writeObject(data);
            // receiving data from server
            results = (long[]) inputStream.readObject();
        } catch (IOException ex) {
            log.log(Level.SEVERE, ex.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return results;
    }



    public static int[] readFromServer(ObjectInputStream inputStream) {
        int[] results = null;
        try {
            // receiving data from server
            results = (int[]) inputStream.readObject();
        } catch (IOException ex) {
            log.log(Level.SEVERE, ex.getMessage());
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Class not found when reading results: " + e.getMessage(), e);
        }
        return results;
    }


    public static long[] readFromServer3(ObjectInputStream inputStream) {
        long[] results = null;
        try {
            // receiving data from server
            results = (long[]) inputStream.readObject();
            System.out.println(results.length);
        } catch (IOException ex) {
            log.log(Level.SEVERE, ex.getMessage());
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Class not found when reading results: " + e.getMessage(), e);
        }
        return results;
    }




    /**
     * Return number of rows matching the input query
     * @throws IOException
     */

    public static void processQuery() throws IOException, ExecutionException, InterruptedException {

        // reading properties file for parameters
        Properties config = Helper.readPropertiesFile("config/config.ini");
        int numBits = Integer.parseInt(config.getProperty("num_bits"));
        int numRows = Integer.parseInt(config.getProperty("num_rows"));
        String server1IP = config.getProperty("server1_ip");
        int server1Port = Integer.parseInt(config.getProperty("server1_port"));
        String server2IP = config.getProperty("server2_ip");
        int server2Port = Integer.parseInt(config.getProperty("server2_port"));
        String server3IP = config.getProperty("server3_ip");
        int server3Port = Integer.parseInt(config.getProperty("server3_port"));
        int iter = Integer.parseInt(config.getProperty("iteration"));

        // create sockets for server
        Socket socketServer1 = new Socket(server1IP, server1Port);
        ObjectOutputStream outputStreamServer1 = new ObjectOutputStream(socketServer1.getOutputStream());
        ObjectInputStream inputStreamServer1 = new ObjectInputStream(socketServer1.getInputStream());

        Socket socketServer2 = new Socket(server2IP, server2Port);
        ObjectOutputStream outputStreamServer2 = new ObjectOutputStream(socketServer2.getOutputStream());
        ObjectInputStream inputStreamServer2 = new ObjectInputStream(socketServer2.getInputStream());

        Socket socketServer3 = new Socket(server3IP, server3Port);
        ObjectOutputStream outputStreamServer3 = new ObjectOutputStream(socketServer3.getOutputStream());
        ObjectInputStream inputStreamServer3 = new ObjectInputStream(socketServer3.getInputStream());

        System.out.println("Server3");
        System.out.println("Local port:" + socketServer3.getLocalPort());
        System.out.println("Remote port:" + socketServer3.getPort());

        // Ask for query input
        Scanner scanner = new Scanner(System.in);
        ArrayList<Double> totalProcessingTimeList = new ArrayList<>();
        ArrayList<Double> totalNetworkTimeList = new ArrayList<>();
        for (int m = 0; m < iter; m++) {
            System.out.print("Enter your message: ");
            long query = Long.parseLong(scanner.nextLine());
            int query_org;
            query = query_org = 31;


            ArrayList<Instant> processingTime = new ArrayList<>();
            ArrayList<Instant> networkTime = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            @SuppressWarnings("unchecked")
            Future<long[]> futureServer1;
            Future<long[]> futureServer2;
            Future<long[]> futureServer3;

            // server tables T0/T1
            int[][] T0 = new int[numBits][2];
            int[][] T1 = new int[numBits][2];
            byte[][] T = new byte[numBits][2];

            Random random = new Random();
            int temp = numBits - 1;
            byte bitValue;

            int i;
            int q1=136;
            processingTime.add(Instant.now());
            for ( i = temp; i >= 0; i--) {
                bitValue = (byte) (query & 1);
                T[i][1] = bitValue;
                T[i][0] = (byte) (1 - bitValue);

                for (int b = 0; b < 2; b++) {
                    byte currentBit = T[i][b];
                    int n = 100;
                    if (currentBit == 1) {
                        T0[i][b] = (byte) ((random.nextInt(n) + 1));
                        T1[i][b] = (byte) ((T0[i][b] - 1));
                    } else {
                        int[] z = {0, n};
                        int l = 1;
                        int totalValues = (n - l + 1 + 1);
                        int randomIndex = random.nextInt(totalValues);

                        if (randomIndex != 0) {
                            T0[i][b] = (byte) ((l + random.nextInt(n)));
                        } else {
                            T0[i][b] = (byte) (z[random.nextInt(z.length)]);
                        }

                        T1[i][b] = T0[i][b];
                    }
                }
                query >>= 1;
            }


            processingTime.add(Instant.now());
            networkTime.add(Instant.now());

// Submit tasks to send T0 and T1 to their respective servers
            futureServer1 = executor.submit(() -> sendToServer(T0, outputStreamServer1, inputStreamServer1));
            futureServer2 = executor.submit(() -> sendToServer(T1, outputStreamServer2, inputStreamServer2));
            long[] result1 = futureServer1.get();
            long[] result2 = futureServer2.get();

            //from Server 3
            futureServer3 = executor.submit(() -> readFromServer3(inputStreamServer3));
            long[] result3 = futureServer3.get();
            networkTime.add(Instant.now());
            processingTime.add(Instant.now());

            //CALCULATION
            long interpolationResult = (long) interpolate(result1, result2, result3, numRows, numBits);
            System.out.println("The number of items matching " + interpolationResult);

            processingTime.add(Instant.now());
            System.out.println("Processing time:" + Helper.getTotalTime(processingTime));
            totalProcessingTimeList.add(Helper.getTotalTime(processingTime));
            totalNetworkTimeList.add(Helper.getTotalTime(networkTime));

            executor.shutdown();
        }


        // Calculate Average Processing and Network Time
        double sumProcessingTime = 0;
        double sumNetworkTime = 0;
        for (double processingTime1 : totalProcessingTimeList) {
            sumProcessingTime += processingTime1;
        }
        for (double networkTime1 : totalNetworkTimeList) {
            sumNetworkTime += networkTime1;
        }

        double averageProcessingTime = sumProcessingTime / iter;
        double averageNetworkTime = sumNetworkTime / iter;
        System.out.println("Average processing time " + averageProcessingTime);
        System.out.println("Temporary network time " + averageNetworkTime);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // return number of rows matching the input query
        processQuery();
    }
}

