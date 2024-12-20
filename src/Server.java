

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Logger;

import ut.Helper;


public class Server {

    public static int div = 32;
    static Properties config = Helper.readPropertiesFile("config/config.ini");
    static int numRows = Integer.parseInt(config.getProperty("num_rows"));
    static int p = (int) (numRows);
    public static long q = (long) p * div;
    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);



    public static long[][] processQueryWithPrecompute(
            ExecutorService executor, Future<?>[] futures,
            int rowsPerThread, int remainder, int numThreads,
            int numBits, long[] serverData, int[][] query,
            long[] results, long[] remvalues, long[] totalsum, int[] precomputedPrefix) throws Exception {

        for (int t = 0; t < numThreads; t++) {
            final int start = t * rowsPerThread + Math.min(t, remainder);
            final int end = start + rowsPerThread + (t < remainder ? 1 : 0);

            int finalT = t;
            futures[t] = executor.submit(() -> {
                long partialSum =0;

                for (int i = start; i < end; i++) {
                    long data = serverData[i];
                    long data_org = data;

                    int leadingZeros = Long.numberOfLeadingZeros(data) - (64 - numBits);
                    long result= (precomputedPrefix[leadingZeros]);

                    for (int j = numBits - 1; j >= leadingZeros; j--) {
                        byte bitValue = (byte) (data & 1);
                        result += (query[j][bitValue]);
                        data >>= 1;
                    }
                   // long s= ((result%q)+q)%q;
                    //long  quotient = (s+q)/div;
                    long s= result;
                    long  quotient = (s)/div;
                    long remn = s%div;

                    results[i] = quotient;
                    remvalues[i] = remn;

                    partialSum += quotient;
                    serverData[i] = data_org;
                }
                totalsum[finalT] = partialSum;
            });
        }

        for (Future<?> future : futures) {
            future.get();
        }

        return (long[][]) new long[][]{totalsum, remvalues};
    }

    public static long[][] processQueryWithPrecompute2(
            ExecutorService executor, Future<?>[] futures,
            int rowsPerThread, int remainder, int numThreads,
            int numBits, long[] serverData, int[][] query,
            long[] results, long[] remvalues, long[] totalsum, int[] precomputedPrefix) throws Exception {

        for (int t = 0; t < numThreads; t++) {
            final int start = t * rowsPerThread + Math.min(t, remainder);
            final int end = start + rowsPerThread + (t < remainder ? 1 : 0);

            int finalT = t;
            futures[t] = executor.submit(() -> {
                long partialSum =0;

                for (int i = start; i < end; i++) {
                    long data = serverData[i];
                    long data_org = data;

                    int leadingZeros = Long.numberOfLeadingZeros(data) - (64 - numBits);
                    long result= (precomputedPrefix[leadingZeros]);

                    for (int j = numBits - 1; j >= leadingZeros; j--) {
                        byte bitValue = (byte) (data & 1);
                        result += (query[j][bitValue]);
                        data >>= 1;
                    }
                   // long s= ((result%q)+q)%q;
                    long s= result;
                    long  quotient = (s)/div;
                    long remn = s%div;

                    results[i] = quotient;
                    remvalues[i] = remn;

                    partialSum += quotient;
                    serverData[i] = data_org;
                }
                totalsum[finalT] = partialSum;
            });
        }

        for (Future<?> future : futures) {
            future.get();
        }

        return (long[][]) new long[][]{totalsum, remvalues};
    }

    public static void main(String[] args) throws Exception {
        // reading server number
        if (args.length != 1) {
            System.out.println("Please provide the server number (1 or 2 or 3)");
            return;
        }

        int  serverNumber = Integer.parseInt(args[0]);
        System.out.println("Server Number: " + serverNumber); // Debugging line

        Properties config = Helper.readPropertiesFile("config/config.ini");
        int numBits = Integer.parseInt(config.getProperty("num_bits"));
        int numThreads = Integer.parseInt(config.getProperty("num_threads"));
        int numRows = Integer.parseInt(config.getProperty("num_rows"));
        String serverIP = config.getProperty("server" + serverNumber + "_ip");
        int serverPort = Integer.parseInt(config.getProperty("server" + serverNumber + "_port"));
        int clientPort = Integer.parseInt(config.getProperty("client_port"));
        String clientIP = config.getProperty("client_ip");
        String dataPath = config.getProperty("csv_file_path");
        int iter = Integer.parseInt(config.getProperty("iteration"));

        // reading server data:
        long[][] data = Helper.loadDataValues(config.getProperty("csv_file_path"), numRows, 2);
        long[] serverData = Arrays.stream(data).mapToLong(row -> row[0]).toArray();
        long[] valueData = Arrays.stream(data).mapToLong(row -> row[1]).toArray();
        ArrayList<Double> totalServerTimeList = new ArrayList<>();
        try {
            System.out.println("Server" + serverNumber + " Listening........");

            int[][] query;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            long[] results = new long[numRows];
            long[] remValues = new long[numRows];
            long[]  totalsum= new long[numRows];

            if (serverNumber == 1) {
                System.out.println("Loaded " + serverData.length + " rows of data");

                // for server 3
                String server3IP = config.getProperty("server3_ip");
                int server3Port = Integer.parseInt(config.getProperty("server3_port"));
                System.out.println(server3Port);
                Socket socketServer3 = new Socket(server3IP, server3Port);

                ObjectOutputStream outputStreamServer3 = new ObjectOutputStream(socketServer3.getOutputStream());
                System.out.println(server3Port);
                ObjectInputStream inputStreamServer3 = new ObjectInputStream(socketServer3.getInputStream());
                System.out.println(server3Port);
                System.out.println("Server3");
                System.out.println("Local port:"+socketServer3.getLocalPort());
                System.out.println("Remote port:"+socketServer3.getPort());

                ServerSocket ss = new ServerSocket(serverPort);
                Socket socketServer = ss.accept();
                System.out.println("Connected!");
                ObjectInputStream inputStream = new ObjectInputStream(socketServer.getInputStream());
                ObjectOutputStream objectOutputStreamclient = new ObjectOutputStream(socketServer.getOutputStream());
                System.out.println("Client");

                for (int i = 0; i < iter; i++) {
                    // initialization
                    ArrayList<Instant> processingTime = new ArrayList<>();

                    int rowsPerThread = numRows / numThreads;
                    int remainder = numRows % numThreads;
                    Future<?>[] futures = new Future<?>[numThreads];
                    int[] precomputedPrefix = new int[numBits + 1];

                    query = (int[][]) inputStream.readObject();
                   // System.out.println("Received from client for iteration i:" + i);

                    processingTime.add(Instant.now());
                    precomputedPrefix[1] = (int) query[0][0];
                    for (int m = 1; m < numBits; m++) {
                        precomputedPrefix[m + 1] = (int) (precomputedPrefix[m] + (query[m][0]));
                    }
                    // Call processQueryWithPrecompute and get the results

                    long[] [] processedData = processQueryWithPrecompute(
                            executor, futures, rowsPerThread, remainder, numThreads, numBits,
                            serverData, query, results, remValues, totalsum, precomputedPrefix);

                    totalsum = processedData[0];
                    remValues = processedData[1];

                    processingTime.add(Instant.now());
                    System.out.println("Processing time:" + Helper.getTotalTime(processingTime));
                    totalServerTimeList.add(Helper.getTotalTime(processingTime));
                    objectOutputStreamclient.writeObject(totalsum);


                    try {
                        outputStreamServer3.writeObject(remValues);

                        System.out.println("Sent remainder values to Server 3");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                executor.shutdown();
            }

            if (serverNumber == 2) {
                System.out.println("Loaded " + serverData.length + " rows of data");

                // for server 3
                String server3IP = config.getProperty("server3_ip");
                int server3Port = Integer.parseInt(config.getProperty("server3_port"));
                System.out.println(server3Port);
                Socket socketServer3 = new Socket(server3IP, server3Port);

                ObjectOutputStream outputStreamServer3 = new ObjectOutputStream(socketServer3.getOutputStream());
                System.out.println(server3Port);
                ObjectInputStream inputStreamServer3 = new ObjectInputStream(socketServer3.getInputStream());
                System.out.println(server3Port);
                System.out.println("Server3");
                System.out.println("Local port:"+socketServer3.getLocalPort());
                System.out.println("Remote port:"+socketServer3.getPort());

                ServerSocket ss = new ServerSocket(serverPort);
                Socket socketServer = ss.accept();
                System.out.println("Connected!");
                ObjectInputStream inputStream = new ObjectInputStream(socketServer.getInputStream());
                ObjectOutputStream objectOutputStreamclient = new ObjectOutputStream(socketServer.getOutputStream());
                System.out.println("Client");

                for (int i = 0; i < iter; i++) {
                    // initialization
                    ArrayList<Instant> processingTime = new ArrayList<>();

                    int rowsPerThread = numRows / numThreads;
                    int remainder = numRows % numThreads;
                    Future<?>[] futures = new Future<?>[numThreads];
                    int[] precomputedPrefix = new int[numBits + 1];

                    query = (int[][]) inputStream.readObject();
                    System.out.println("Received from client for iteration i:" + i);

                    processingTime.add(Instant.now());
                    precomputedPrefix[1] = (int) query[0][0];
                    for (int m = 1; m < numBits; m++) {
                        precomputedPrefix[m + 1] = (int) (precomputedPrefix[m] + (query[m][0]));
                    }
                    long[] [] processedData = processQueryWithPrecompute2(
                            executor, futures, rowsPerThread, remainder, numThreads, numBits,
                            serverData, query, results, remValues, totalsum, precomputedPrefix);

                    totalsum = processedData[0];
                    remValues = processedData[1];

                    processingTime.add(Instant.now());
                    System.out.println("Processing time:" + Helper.getTotalTime(processingTime));
                    totalServerTimeList.add(Helper.getTotalTime(processingTime));
                    objectOutputStreamclient.writeObject(totalsum);


                    try {
                        outputStreamServer3.writeObject(remValues);

                        System.out.println("Sent remainder values to Server 3");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                executor.shutdown();
            }




            if (serverNumber == 3) {
                System.out.println("Server 3: Comparing the Remainders");

                ServerSocket ss = new ServerSocket(serverPort);
                Socket server1Socket = ss.accept(); // start accepting server connection
                ObjectInputStream inputStreamServer1 = new ObjectInputStream(server1Socket.getInputStream());
                ObjectOutputStream objectOutputStreamServer1 = new ObjectOutputStream(server1Socket.getOutputStream());
                System.out.println("Connected!");
                System.out.println("Server?");
                System.out.println("Local port:"+server1Socket.getLocalPort());
                System.out.println("Remote port:"+server1Socket.getPort());

                Socket server2Socket = ss.accept(); // start accepting server connection
                ObjectInputStream inputStreamServer2 = new ObjectInputStream(server2Socket.getInputStream());
                ObjectOutputStream objectOutputStreamServer2 = new ObjectOutputStream(server2Socket.getOutputStream());
                System.out.println("Connected!");
                System.out.println("Server?");
                System.out.println("Local port:"+server2Socket.getLocalPort());
                System.out.println("Remote port:"+server2Socket.getPort());

                Socket clientSocket = ss.accept(); // start accepting server connection
                ObjectInputStream inputStreamClient = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream objectOutputStreamClient = new ObjectOutputStream(clientSocket.getOutputStream());
                System.out.println("Connected!");
                System.out.println("Server?");
                System.out.println("Local port:"+clientSocket.getLocalPort());
                System.out.println("Remote port:"+clientSocket.getPort());


                for (int m = 0; m < iter; m++) {
                    long[] rem_Server1 = (long[]) inputStreamServer1.readObject();
                    long[] rem_Server2 = (long[]) inputStreamServer2.readObject();

                    // Compare the remainders
                    int[] comp = new int[numRows];
                    ArrayList<Instant> processingTime = new ArrayList<>();


                    System.out.println(rem_Server2.length);
                    processingTime.add(Instant.now());
                    for (int j = 0; j < numRows; j++) {
                        if (Math.abs(rem_Server1[j]) < Math.abs(rem_Server2[j])) {
                            comp[j] = 1;
                        }

                        else {
                            comp[j] = 0;
                        }
                    }

                    int totalComp = 0;
                    for (int value : comp) {
                        totalComp += value;
                    }
                    System.out.println("COMP:" + totalComp);


                    long[] totalCompBytes = new long[]{totalComp};

                    processingTime.add(Instant.now());
                    System.out.println("Processing time: " + Helper.getTotalTime(processingTime));
                    totalServerTimeList.add(Helper.getTotalTime(processingTime));

                    objectOutputStreamClient.writeObject(totalCompBytes);



                }
            }
        } finally {
        }
        //Calculate Average
        double sumServerTime = 0;
        for (double servertime : totalServerTimeList) {
            sumServerTime += servertime;
        }
        double averageServerTime = sumServerTime / iter;
        System.out.println("Average Server time " + averageServerTime);
    }
}


