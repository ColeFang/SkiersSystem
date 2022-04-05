/**
 * @program: client
 * @description:
 * @author: Mr.Fang
 * @create: 2022-02-17 09:10
 **/
import io.swagger.client.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.ResortsApi;
import okhttp3.OkHttpClient;
import okhttp3.Response;


import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
public class skiersUpdate {
    /**
    * @Description:
    * @Param: [args]
    * @return: void
    */
    public static float sendNumber = 0;
    public static float receiveNumber = 0;
    public static void main(String[] args) throws InterruptedException, IOException, ArgumentsException {
        // get configures from command line
        int num_threads = 0;
        int num_skiers=0;
        int num_lift = 0;
        int num_run = 0;
        String basePath = null;
        Option numThreads = new Option(null, "num_threads", true, "maximum number of threads to run");
        Option numSkiers = new Option(null, "num_skiers", true, "number of skier to generate lift rides for");
        Option numLifts = new Option(null, "num_lifts", true, "number of ski lifts ");
        Option numRuns  = new Option(null, "num_runs", true, "number of runs for each user.");
        //Option ip = new Option(null, "ip", true, "ip address for the server.");
        //Option port = new Option(null, "port", true, "port for the connection.");

        Options options = new Options();
        options.addOption(numThreads);
        options.addOption(numSkiers);
        options.addOption(numLifts);
        options.addOption(numRuns);
        //options.addOption(ip);
        //options.addOption(port);
        CommandLine cli = null;
        CommandLineParser cliParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            cli = cliParser.parse(options, args);
            num_threads = Integer.parseInt(cli.getOptionValue(numThreads.getLongOpt()));
            num_skiers = Integer.parseInt(cli.getOptionValue(numSkiers.getLongOpt()));
            num_lift = Integer.parseInt(cli.getOptionValue(numLifts.getLongOpt()));
            num_run = Integer.parseInt(cli.getOptionValue(numRuns.getLongOpt()));
            //String ip_string = cli.getOptionValue(ip.getLongOpt());
            //String port_string = cli.getOptionValue(port.getLongOpt());
            basePath="http://skiers-d8b65650b78ad35c.elb.us-east-1.amazonaws.com:8080/skiers-1";
        } catch (ParseException e) {
            helpFormatter.printHelp(">>>>>> test cli options", options);
            e.printStackTrace();
            throw new ArgumentsException("Error:");
        }

        long startTime = System.currentTimeMillis();
        File writeFile = new File("./write32.csv");
        BufferedWriter writeText = new BufferedWriter(new FileWriter(writeFile));


        //phase 1
        int num_phase1 = num_threads/4;
        int skies1 = num_skiers/num_phase1;
        int times1 = (num_run/5)*skies1;
        ExecutorService es1 = new ThreadPoolExecutor(16, 100, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(num_phase1));
        for (int i = 0; i < num_phase1; i++) {
            es1.execute(new update(basePath, i*skies1,(i+1)*skies1,0,90,0,10,num_lift, times1,i, writeText));
        }
        ThreadPoolExecutor tpe1 = ((ThreadPoolExecutor) es1);
        while (tpe1.getCompletedTaskCount()<=num_phase1/5){
            Thread.sleep(30);
        }
        System.out.println("phase2 start");

        //phase 2
        int num_phase2 = num_threads;
        int skies2 = num_skiers/num_phase2;
        int times2 = (num_run*3/5)*skies2;
        ExecutorService es2 = new ThreadPoolExecutor(16, 100, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(num_phase2));
        for (int i = 0; i < num_phase2; i++) {
            es2.execute(new update(basePath, i*skies2,(i+1)*skies2,91,360,0,10,num_lift, times2,i,writeText));
        }
        ThreadPoolExecutor tpe2 = ((ThreadPoolExecutor) es2);
        while (tpe2.getCompletedTaskCount()<=num_phase2/5){
            Thread.sleep(30);
        }
        System.out.println("phase3 start");

        //phase 3
        int num_phase3 = num_threads/10;
        int skies3 = num_skiers/num_phase3;
        int times3 = (num_run/10)*skies3;
        ExecutorService es3 = new ThreadPoolExecutor(16, 100, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(num_phase3));
        for (int i = 0; i < num_phase3; i++) {
            es3.execute(new update(basePath, i*skies3,(i+1)*skies3,361,420,0,10,num_lift, times3,i,writeText));
        }

        //stop check
        ThreadPoolExecutor tpe3 = ((ThreadPoolExecutor) es3);
        while (tpe1.getCompletedTaskCount()!=num_phase1 || tpe2.getCompletedTaskCount()!=num_phase2 || tpe3.getCompletedTaskCount()!=num_phase3){
            Thread.sleep(30);
        }
        long endTime = System.currentTimeMillis();

        int requests = num_phase1 * times1 + num_phase2 * times2 + num_phase3 * times3;
        int wallTime = (int) ((endTime - startTime)/1000);

        //print results
        System.out.println("completed requests number: "+requests);
        System.out.println("uncompleted requests number: 0");
        System.out.println("wall time：" + wallTime + "s");
        System.out.println("the total throughput in requests per second：" + (float)requests/(float)wallTime);
        writeText.flush();
        writeText.close();
    }
}


