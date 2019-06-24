package c42.CompletableFuture;

import Utils.PrintUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.sound.midi.Soundbank;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFutureTest {


    @Test
    public void testCreateAndGetCompletableFuture() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cf = new CompletableFuture<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cf.complete("hello world");
            }
        }).start();

        //由于cf没有complete，此处会block到其他的线程调用了complete之后才会
        System.out.println(cf.get());
    }


    //适用于task不需要返回任何结果的情况；
    @Test
    public void testRunAsync() throws ExecutionException, InterruptedException {
        CompletableFuture cf =  CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                PrintUtils.print(" i am running");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println(cf.get());
    }


    //将2个异步任务进行串联; 先执行task1， 然后执行task2, 注意：task2的输入参数为task1的输出结果；
    @Test
    public void testChainTwoAsyncTasks() throws ExecutionException, InterruptedException {
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            PrintUtils.print("task1 is running ...");
            return "Hello";
        });

        Function<String, CompletableFuture<String>> fn = (s) -> {
            return CompletableFuture.supplyAsync(() -> s + "World");
        };

        CompletableFuture<String> task = task1.thenCompose(fn);
        Assert.assertEquals("HelloWorld", task.get());
    }




}
