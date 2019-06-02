package c36.CyclicBarrier;

import Utils.PrintUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierTest {
    int N; //标识选手的个数
    int[][] data; //记录每个选手每个阶段的用时
    final CyclicBarrier barrier;
    volatile boolean done = false;

    public static void main(String[] args) throws InterruptedException {
        new CyclicBarrierTest(10);
    }


    public CyclicBarrierTest(int n) throws InterruptedException {
        data = new int[n][3]; //n * 3 的二维数组
        N = n;
        Runnable barrierAction = new SumComputer();
        //
        barrier = new CyclicBarrier(N, barrierAction);

        List<Thread> threads = new ArrayList<Thread>(N);
        for (int i = 0; i < N; i++) {
            Thread thread = new Thread(new Player(i,0));
            threads.add(thread);
            thread.start();
        }
        // wait until done
        for (Thread thread : threads) {
            thread.join();
        }
    }

    class SumComputer implements Runnable{
        private int round = 0 ;
        public void run() {
            int sum = 0;
            for (int i = 0; i<10; ++i){
                sum += data[i][round];
            }
            PrintUtils.print("当前round : " + round + ", 平均用时: " + sum/10);
            round ++;
        }
    }

    class Player implements Runnable {
        int no;  //表示是第几名选手;
        int round;  //表示当前比赛是第几轮;一共3轮比赛;
        Player(int no, int round) {
            this.no = no;
            this.round = round;
        }

        public void run() {
            while (round < 3) {
                Random r = new Random();
                int timeSpend = r.nextInt(10);
                PrintUtils.print("当前选手:" + this.no + ", 当前round:" + this.round + ", timeSpend: " + timeSpend);
                //把话费的事件保存到数据中去
                data[no][round] = timeSpend;

                //使用 barrier 保证线程同步,一直等待到所以其他线程到达同样的状态;
                try {
                    barrier.await();
                } catch (InterruptedException ex) {
                    return;
                } catch (BrokenBarrierException ex) {
                    return;
                }

                round ++;
            }
        }
    }
}