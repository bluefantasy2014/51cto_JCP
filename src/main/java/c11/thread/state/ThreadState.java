package c11.thread.state;

import Utils.PrintUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/*
*   演示java thread的各个不同的状态以及状态的转化；
*
* */
public class ThreadState {

    //NEW
    @Test
    public void testThreadState_NEW(){
        Thread thread = new Thread();
        PrintUtils.print(thread.getState().toString());
        Assert.assertTrue(thread.getState() == Thread.State.NEW);
    }

    //RUNNABLE,TERMINATED
    @Test
    public void testThreadState_RUNNABLE_TERMINATED() throws InterruptedException {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                //状态为RUNNABLE
                PrintUtils.print("I am running ! My state is :" + Thread.currentThread().getState());
                Assert.assertTrue(Thread.currentThread().getState() == Thread.State.RUNNABLE);
            }
        });
        thread.start();

        TimeUnit.SECONDS.sleep(1);
        //此时线程执行已经结束了，状态为TERMINATED
        PrintUtils.print(thread.getState().toString());
        Assert.assertTrue(thread.getState() == Thread.State.TERMINATED);
    }

    //TIMED_WAITING 通过sleep的方式
    @Test
    public void testThreadState_TIMED_WAITING() throws InterruptedException {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                //此处sleep之后线程会进入TIMED_WAITING 状态
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        TimeUnit.SECONDS.sleep(1);
        //此时thread还在睡眠中
        PrintUtils.print(thread.getState().toString());
        Assert.assertTrue(thread.getState() == Thread.State.TIMED_WAITING);
    }

    //TIMED_WAITING 通过join的方式
    @Test
    public void testThreadState_1_TIMED_WAITING() throws InterruptedException {
        final Thread callingThread = Thread.currentThread();

        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    //当前线程（thread）会等待主线程（callingThread） 2秒钟的时间；
                    callingThread.join(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        TimeUnit.SECONDS.sleep(1);
        //此时thread 正在等待当前线程(callingThread),所以thread 的状态应该是TIMED_WAITING
        PrintUtils.print(thread.getState().toString());
        Assert.assertTrue(thread.getState() == Thread.State.TIMED_WAITING);
    }

    //WAITING 通过join的方式
    @Test
    public void testThreadState_WAITING() throws InterruptedException {
        final Thread callingThread = Thread.currentThread();

        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    //当前线程（thread）会一致等待主线程（callingThread）,因为我们没有设置等待时间；
                    callingThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        TimeUnit.SECONDS.sleep(1);
        //此时thread 正在等待当前线程(callingThread),所以thread 的状态应该是WAITING
        PrintUtils.print(thread.getState().toString());
        Assert.assertTrue(thread.getState() == Thread.State.WAITING);
    }

    //WAITING:  通过Object.wait()的方式
    @Test
    public void testThreadState_1_WAITING() throws InterruptedException {
        final Object lock = new Object();

        final Thread thread = new Thread(new Runnable() {
            public void run() {
                 synchronized (lock){
                     try {
                         //注意： 调用wait后当前线程会被挂起，进入WAITING状态，无限期的等待，一直到被lock.notify()唤醒;
                         lock.wait();
                         PrintUtils.print("after wait .. ");
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
            }
        });
        thread.start();

        TimeUnit.SECONDS.sleep(1);
        //此时thread的状态应该是WAITING
        PrintUtils.print(thread.getState().toString());
        Assert.assertTrue(thread.getState() == Thread.State.WAITING);

        //此处notifyAll使得thread 重新获取lock的锁， 并继续执行；
        synchronized (lock){
            lock.notifyAll();
        }
        TimeUnit.SECONDS.sleep(1);
    }

    //WAITING 通过ReentrantLock;
    //注意： ReentrantLock 是基于AQS实现的； AQS底层是使用LockSupport实现线程的操作的；所以这个测试中线程的状态是WAITING， 而不是BLOCKED
    @Test
    public void testThreadState_2_WAITING() throws InterruptedException {
        final ReentrantLock lock = new ReentrantLock();

        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                try {
                    lock.lock();
                    try {
                        //注意：执行完这句话后，thread1 会进入TIMED_WAITING状态；
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }finally {
                    lock.unlock();
                }
            }
        });
        thread1.start();

        //等待1秒钟，此时thread1肯定正在拿着 lock对象的锁；
        TimeUnit.SECONDS.sleep(1);
        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                try{
                    lock.lock();
                    //do nothing
                }finally {
                    lock.unlock();
                }
            }
        });
        thread2.start();

        TimeUnit.SECONDS.sleep(1);
        //此时thread2肯定正在等待 lock对象的锁，因为锁被thread1 占有着；
        PrintUtils.print(thread2.getState().toString());
        Assert.assertTrue(thread2.getState() == Thread.State.WAITING);
    }

    //BLOCKED
    @Test
    public void testThreadState_BLOCKED() throws InterruptedException {
        final Object lock = new Object();

        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                synchronized (lock){
                    try {
                        //注意：执行完这句话后，thread1 会进入TIMED_WAITING状态；但是它并不会释放锁 lock ；
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread1.start();

        //等待1秒钟，此时thread1肯定正在拿着 lock对象的锁；
        TimeUnit.SECONDS.sleep(1);
        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                synchronized (lock){
                    // do nothing
                }
            }
        });
        thread2.start();

        TimeUnit.SECONDS.sleep(1);
        //此时thread2肯定正在等待 lock对象的锁，因为锁被thread1 占有着；
        PrintUtils.print(thread2.getState().toString());
        Assert.assertTrue(thread2.getState() == Thread.State.BLOCKED);
    }


}
