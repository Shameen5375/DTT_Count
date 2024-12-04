/* import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import ut.Helper;

public class Client {

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static int interpolate(short[] result1, short[] result2, int numRows, int numBits) {
        int count = 0;
        for (int i = 0; i < numRows; i++) {
            if ((Math.abs(result1[i] - result2[i])) == numBits) {
                count++;
            }
        }
        return count;

    }

    public static short[] sendToServer(Object data, ObjectOutputStream outputStream,
                                       ObjectInputStream inputStream) {
        short[] results = null;
        try {
            // sending data to server
            outputStream.writeObject(data);

            // receiving data from server
            results = (short[]) inputStream.readObject();
        } catch (IOException ex) {
            log.log(Level.SEVERE, ex.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    /**
     * Return number of rows matching the input query
     *
     * @throws IOException
     *//*

    public static void processQuery() throws IOException, ExecutionException, InterruptedException {

        // reading properties file for parameters
        Properties config = Helper.readPropertiesFile("config/config.ini");
        int numBits = Integer.parseInt(config.getProperty("num_bits"));
        int numRows = Integer.parseInt(config.getProperty("num_rows"));
        String server1IP = config.getProperty("server1_ip");
        int server1Port = Integer.parseInt(config.getProperty("server1_port"));
        String server2IP = config.getProperty("server2_ip");
        int server2Port = Integer.parseInt(config.getProperty("server2_port"));
        int clientPort = Integer.parseInt(config.getProperty("client_port"));

        // create sockets for server
        Socket socketServer1 = new Socket(server1IP, server1Port);
        ObjectOutputStream outputStreamServer1 = new ObjectOutputStream(socketServer1.getOutputStream());
        ObjectInputStream inputStreamServer1 = new ObjectInputStream(socketServer1.getInputStream());

        Socket socketServer2 = new Socket(server2IP, server2Port);
        ObjectOutputStream outputStreamServer2 = new ObjectOutputStream(socketServer2.getOutputStream());
        ObjectInputStream inputStreamServer2 = new ObjectInputStream(socketServer2.getInputStream());
        int iter = Integer.parseInt(config.getProperty("iteration"));


        // Ask for query input
        Scanner scanner = new Scanner(System.in);
        for (int m = 0; m < iter; m++) {
            //System.out.print("Enter your message: ");
            long query = Long.parseLong(scanner.nextLine());
            int query_org;
            query = query_org = 163;
//            query = query_org = 4;
            short[] result1 = null;
            short[] result2 = null;
            boolean chooseServer;
            ArrayList<Instant> processingTime = new ArrayList<>();
            ArrayList<Instant> networkTime = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<short[]>[] futures = (Future<short[]>[]) new Future<?>[2];

            // server tables T0/T1
            byte[][] T0 = new byte[numBits][2];
            byte[][] T1 = new byte[numBits][2];
            byte[][] T = new byte[numBits][2];
            Random random = new Random();


            // Prepare tables T0/T1 with random integers between 0 and 127


            int temp = numBits - 1;
            byte bitValue;
            int n = 100;
            int[] z = {0, n};
            int l = 1;
            int totalValues = (n - l + 2);
            int randomIndex = random.nextInt(totalValues);
            //Constraint DTT
            System.out.println("Starting Constraint DTT");
            // processingTime.add(Instant.now());
            Instant start=Instant.now();
            for (int i = temp; i >= 0; i--) {
                bitValue = (byte) (query & 1);
                T[i][1] = bitValue;
                T[i][0] = (byte) ( bitValue ^1 );

                for (int b = 0; b < 2; b++) {
                    byte currentBit = T[i][b];
                    if (currentBit == 1) {
                        T0[i][b] = (byte) ((random.nextInt(n) + 1));
                        T1[i][b] = (byte) ((T0[i][b] + 1));
                    } else {
                        if (randomIndex != 0) {T0[i][b] = (byte) ((l + random.nextInt(n)));}
                        else { T0[i][b] = (byte) (z[random.nextInt(z.length)]);}

                        T1[i][b] = T0[i][b];
                    }
                }
                query >>= 1;
            }
            Instant end=Instant.now();
            System.out.println("Time" + Duration.between(start,end).toNanos()/1000.0);
            // Prepare tables T0/T1 with random integers between 0 and 127
         */
/*   Instant s1= Instant.now();
           for (int i = 0; i < numBits; i++) {
                T0[i][0] = (byte) (random.nextInt(100) + 1);
                T0[i][1] = (byte) (random.nextInt(100) + 1);
                T1[i][0] = T0[i][0];
                T1[i][1] = T0[i][1];
            }

            // Store query as additive shares in T0/T1
            processingTime.add(Instant.now());
            int temp = numBits - 1;
            byte bitValue;
            for (int i = temp; i >= 0; i--) {
                bitValue = (byte) (query & 1);
                T0[i][bitValue] -= 1;
                query >>= 1;
            }
            Instant e1= Instant.now();

            System.out.println("Table Generation:"+Duration.between(s1,e1).toMillis()); *//*


            //MOD DTT:

            */
/* int temp = numBits - 1;
            byte bitValue;
            int q =127;
            System.out.println("Starting Mod DTT");
            Random random1 = new Random(12345); // Use any integer as a seed value

            processingTime.add(Instant.now());
            for (int i = temp; i >= 0; i--) {
                bitValue = (byte) (query & 1);
                T[i][1]=bitValue;
                T[i][0] = (byte) (bitValue ^ 1);
                for (int b = 0; b < 2; b++) {
                    T0[i][b] = (byte) (random1.nextInt(q)); // random value between 0 and 126
                    T1[i][b] = (byte) ((T0[i][b] - T[i][b] + q) % q);
                }
                query >>= 1;
            }
*//*


            // processingTime.add(Instant.now());

            // send T0/T1 shares to servers
            networkTime.add(Instant.now());
            chooseServer = random.nextBoolean();
            if (chooseServer) {
                futures[0] = executor.submit(() -> sendToServer(T0, outputStreamServer1, inputStreamServer1));
                futures[1] = executor.submit(() -> sendToServer(T1, outputStreamServer2, inputStreamServer2));
            } else {
                futures[0] = executor.submit(() -> sendToServer(T0, outputStreamServer2, inputStreamServer2));
                futures[1] = executor.submit(() -> sendToServer(T1, outputStreamServer1, inputStreamServer1));
            }

            // waiting for threads to complete
            result1 = futures[0].get();
            result2 = futures[1].get();

            networkTime.add(Instant.now());

            // interpolate server results
            Instant s= Instant.now();
            processingTime.add(Instant.now());
            System.out.println("The number of items matching query:" + query_org + " is " +
                    interpolate(result1, result2, numRows, numBits));
            processingTime.add(Instant.now());
            Instant e= Instant.now();

            System.out.println("Interpolation:"+Duration.between(s,e).toMillis());

            // calculating processing/network time
            System.out.println("Processing time:" + Helper.getTotalTime(processingTime));
            System.out.println("Network time:" + Helper.getTotalTime(networkTime));

            executor.shutdown();
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // return number of rows matching the input query
        processQuery();

    }
}
*/
