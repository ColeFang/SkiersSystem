import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.io.*;
import java.util.Random;

/**
 * @program: client
 * @description:
 * @author: Mr.Fang
 * @create: 2022-02-17 11:38
 **/

public class update implements Runnable{
    private String basePath;
    private int skiers_start;
    private int skiers_end;
    private int time_start;
    private int time_end;
    private int wait_start;
    private int wait_end;
    private int lift_end;
    private int times;
    private int id;
    private Random rand =new Random(25);
    private BufferedWriter writer;
    /**
    * @Description:
    * @Param: [basePath, skiers_start, skiers_end, time_start, time_end, wait_start, wait_end, lift_end, times, id, writer]
    * @return:
    */
    public update(String basePath, int skiers_start, int skiers_end, int time_start, int time_end, int wait_start, int wait_end, int lift_end, int times, int id, BufferedWriter writer) {
        this.basePath=basePath;
        this.skiers_end=skiers_end;
        this.skiers_start=skiers_start;
        this.time_start=time_start;
        this.time_end=time_end;
        this.wait_end=wait_end;
        this.wait_start=wait_start;
        this.lift_end=lift_end;
        this.times=times;
        this.id=id;
        this.writer=writer;
    }

    /**
    * @Description:
    * @Param: []
    * @return: void
    */

    @Override
    public void run() {

        for (int i = 0; i < times; i++) {
            // post data to the server
            long startTime = System.currentTimeMillis();
            SkiersApi apiInstance = new SkiersApi();
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(basePath);
            apiInstance.setApiClient(apiClient);
            LiftRide liftRide = new LiftRide();
            liftRide.setLiftID(rand.nextInt(lift_end));
            liftRide.setTime(rand.nextInt(time_end-time_start)+time_start);
            liftRide.setWaitTime(rand.nextInt(wait_end));
            int skiersID = rand.nextInt(skiers_end-skiers_start)+skiers_start;
            skiersUpdate.sendNumber += 1.0;
            for (int j = 0; j < 5; j++) {
                try {
                    apiInstance.writeNewLiftRide(liftRide,12,"12","12",skiersID);
                    skiersUpdate.receiveNumber+=1.0;
                    System.out.println("send/receive:   " + skiersUpdate.sendNumber/skiersUpdate.receiveNumber);
                    break;
                } catch (ApiException e) {
                    System.err.println("Exception when calling ResortsApi#getResorts");
                    e.printStackTrace();
                }
            }
            long endTime = System.currentTimeMillis();
            try {
                writer.write(startTime+",POST,"+(endTime-startTime)+",200\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
