/*
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Logger;

import ut.Helper;

public class Server {

    public static int div = 32;
    public static int q = 136;

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    */
/**
     * To process the client query using pre-computation approach
     *
     * @param executor          the thread executors
     * @param futures           the thread handler
     * @param rowsPerThread     the number of rows handled by each thread
     * @param remainder         helper variable
     * @param numThreads        the number of threads
     * @param numBits           the number of bits
     * @param serverData        the data stored at the server
     * @param query             the client query
     * @param results           the result formed after server computation
     * @param precomputedPrefix the precomputed prefix values from client share
     * @return the valued computed for each row of server data
     * @throws Exception
     *//*

    public static short[][] processQueryWithPrecompute(ExecutorService executor, Future<?>[] futures,
                                                      int rowsPerThread, int remainder, int numThreads,
                                                      int numBits, long[] serverData, byte[][] query,
                                                     short[] results, short[] remvalues, short[] totalsum, short[] precomputedPrefix) throws Exception {

        // running threads
        for (int t = 0; t < numThreads; t++) {
            final int start = t * rowsPerThread + Math.min(t, remainder);
            final int end = start + rowsPerThread + (t < remainder ? 1 : 0);

            int finalT = t;
            futures[t] = executor.submit(() -> {
                short result;
                long data, data_org;
                short partialSum = 0;

                int temp = numBits - 1;
                byte mod = 127;
                int j, leadingZeros, temp1 = 64 - numBits;
                byte bitValue;
                for (int i = start; i < end; i++) {
                    data = serverData[i];
                    data_org = data;
                    leadingZeros = Long.numberOfLeadingZeros(data) - temp1;
                    result = (precomputedPrefix[leadingZeros]);
                    short quotient = 0;
                    short remn = 0;
                    for (j = temp; j >= leadingZeros; j--) {
                        bitValue = (byte) (data & 1);
                        result += (query[j][bitValue]);
                        data >>= 1;
                    }
                    quotient = (short) (result/div);
                    remn = (short) (result % 8);
                    // Start Here:
                   */
/* System.out.println(result);
                    System.out.println("q:" + quotient);
                    System.out.println("r:" + remn);*//*


                    results[i] = (short) Math.abs(quotient);
                    remvalues[i] = remn;
                    partialSum += (short) results[i];

                    //For mod Count:
                    */
/*byte[] sum;
                    sum[i]=(byte) ((result%mod);
                    results[i] = (byte) ((result+q)/div);*//*

                    serverData[i] = data_org;

                 */
/*   System.out.println("result :" + results[i]);
                    System.out.println("rem :" + remvalues[i]);
                    System.out.println("serverdata :" + serverData[i]);*//*

                }

                totalsum[finalT] = partialSum;

                //System.out.println("total:" + totalsum[finalT]);


            });

        }
            for (Future<?> future : futures) {
                future.get();
        }

        return (short[][]) new short[][]{totalsum, remvalues};
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
        long[] serverData = Helper.loadDataValues(dataPath, numBits, numRows);
        ArrayList<Double> totalServerTimeList = new ArrayList<>();
        try {
            System.out.println("Server" + serverNumber + " Listening........");

            byte[][] query;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            short[] results = new short[numRows];
            short[] totalsum = new short[numThreads];
            short[] remValues = new short[numRows];

            if (serverNumber == 1 || serverNumber == 2) {
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
                Socket socketServer = ss.accept(); // server started to listen
                System.out.println("Connected!");
                ObjectInputStream inputStream = new ObjectInputStream(socketServer.getInputStream());
                ObjectOutputStream objectOutputStreamclient = new ObjectOutputStream(socketServer.getOutputStream());
                System.out.println("Client");
                //  System.out.println("Local port:"+socketServer.getLocalPort());
                // System.out.println("Remote port:"+socketServer.getPort());

                for (int i = 0; i < 50; i++) {
                    // initialization
                    ArrayList<Instant> processingTime = new ArrayList<>();
                  
                    int rowsPerThread = numRows / numThreads;
                    int remainder = numRows % numThreads;
                    Future<?>[] futures = new Future<?>[numThreads];
                    short[] precomputedPrefix = new short[numBits + 1];

                   query = (byte[][]) inputStream.readObject();
                    System.out.println("Received from client for iteration i:" + i);

                    processingTime.add(Instant.now());
                    // with pre-computation
                    precomputedPrefix[1] = query[0][0];
                    for (int m = 1; m < numBits; m++) {
                        precomputedPrefix[m + 1] = (short) (precomputedPrefix[m] + (query[m][0]));
                    }

                    */
/*results = processQueryWithPrecompute(executor, futures, rowsPerThread, remainder, numThreads, numBits,
                            serverData, query, results, remValues, precomputedPrefix);*//*


                    // Calculate totalSum using processQueryWithPrecompute

                    // Process the query and retrieve results and remValues
                   short[][] processedData = processQueryWithPrecompute(executor, futures, rowsPerThread, remainder, numThreads, numBits,
                            serverData, query, results, remValues,totalsum, precomputedPrefix);

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


                    //System.out.println("Iteration " + (m) + ": Waiting for data from Server 1 and Server 2");

                    // Attempt to read data from both servers
                    //Thread
                    short[] rem_Server1 = (short[]) inputStreamServer1.readObject();
                    short[] rem_Server2 = (short[]) inputStreamServer2.readObject();


                    //System.out.println("rems1"+ rem_Server1);
                    //System.out.println("rems2"+ rem_Server2.length);

                    // Compare the remainders
                    byte[] comp = new byte[numRows];
                    ArrayList<Instant> processingTime = new ArrayList<>();
                    processingTime.add(Instant.now());

                    System.out.println(rem_Server2.length);

                    Instant start=Instant.now();
                    for (int j = 0; j < rem_Server1.length; j++) {
                        //System.out.println("rems1:"+ rem_Server1[j]);
                        //System.out.println("rems2:"+ rem_Server2[j]);

                        if (Math.abs(rem_Server1[j]) < Math.abs(rem_Server2[j])) {
                            comp[j] = 1;  // Server 1 remainder is greater
                        } else {
                            comp[j] = 0;  // Server 2 remainder is greater or equal
                        }
                    }

                    byte totalComp = 0;
                    for (byte value : comp) {
                        totalComp += value;
                    }
                  //  System.out.println(totalComp);


                    byte[] totalCompBytes = new byte[]{totalComp};
                    Instant end=Instant.now();
                    System.out.println("");*/
/*//*

                    processingTime.add(Instant.now());

                    objectOutputStreamClient.writeObject(totalCompBytes);

                    // End measuring processing time

                    System.out.println("Processing time: " + Helper.getTotalTime(processingTime));
                    System.out.println(processingTime.size());

                    totalServerTimeList.add(Helper.getTotalTime(processingTime));

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

*/
