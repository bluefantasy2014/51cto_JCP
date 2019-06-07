package c36.CyclicBarrier;

import Utils.PrintUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierTest {
    int playerCount; //标识选手的个数
    int round ; //比赛一共有几轮
    int[][] data; //记录每个选手每个阶段的用时
    final CyclicBarrier barrier; //

    public static void main(String[] args) throws InterruptedException {
        CyclicBarrierTest test = new CyclicBarrierTest(10,3);
        test.startMatch();
    }

    public CyclicBarrierTest(int playerCount, int round) {
       this.playerCount = playerCount;
       this.round = round;
       this.data = new int[playerCount][round];
       this.barrier = new CyclicBarrier(this.playerCount, new ScoreComputer(playerCount));
    }

    public void startMatch() throws InterruptedException {
        //为每个Player对象创建一个单独的线程并启动
        List<Thread> threads = new ArrayList<Thread>(playerCount);
        for (int i = 0; i < playerCount; i++) {
            Thread thread = new Thread(new Player(i,this.round));
            threads.add(thread);
            thread.start();
        }
    }

    //计算每个Player比赛耗时的类 (barrierAction)
    class ScoreComputer implements Runnable{
        private int round = 0 ;  //表示当前是比赛的第几轮
        private int [] scores ;  //记录每个Player的总耗时

        public ScoreComputer(int playerCount){
            this.scores = new int[playerCount];
        }

       //会计算当前轮比赛后每个Player的总的耗时
        public void run() {
            for (int i = 0; i<data.length; ++i){
                if (round == 0){ //如果是第1轮，不用累加
                   this.scores[i] = data[i][round];
                }else { //如果是大于第一轮，需要累加
                   this.scores[i]  += data[i][round];
                }
                PrintUtils.print(" ScoreComputer 当前round : " + round + "，当前Player：" + i + ", 总用时: " + this.scores[i] );
            }
            round ++;
        }
    }

    class Player implements Runnable {
        int no;  //表示是第几名选手;
        int round = 0;  //表示当前比赛是第几轮;
        int totalRound;  //表示总的轮数;
        Player(int no,int totalRound) {
            this.no = no;
            this.totalRound = totalRound;
        }

        public void run() {
            while (round < totalRound) {
                Random r = new Random();
                int timeSpend = r.nextInt(10);
                PrintUtils.print(" Player 当前选手:" + this.no + ", 当前round:" + this.round + ", timeSpend: " + timeSpend);
                //把花费的事件保存到数据中去
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