package c42.CompletableFuture;

import Utils.PrintUtils;
import com.sun.org.apache.regexp.internal.RE;
import org.junit.Assert;
import org.junit.Test;

import javax.sound.midi.Soundbank;
import java.util.concurrent.*;
import java.util.function.BiFunction;
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

    @Test
    public void testThenApply(){
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApply(s -> {
            return s.toUpperCase();
        });
        Assert.assertEquals("MESSAGE", cf.getNow(null));
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

    //多个任务进行串联和并联,加入每个任务都有一个返回值，最终的结果就是要把所以的task的结果做字符串拼接操作；
    //并且保证各个task按照指定的顺序执行；
    @Test
    public void testChainMultipleTasks() throws ExecutionException, InterruptedException {
        //task1 需要输出自己的结果，所以是Supplier
        Supplier<String> task1 = new Supplier<String>() {
            @Override
            public String get() {
                PrintUtils.print("task1 is running ");
                return "task1";
            }
        };

        //表示task1的CompletableFuture
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(task1);

        //task2的逻辑就是把task的输出作为输入，然后加上自己的输出
        Function<String, CompletableFuture<String>> task2Fn = (s) ->{
            PrintUtils.print("task2 is running");
            return CompletableFuture.supplyAsync(() -> s + "|task2");
        } ;
        //把task1和task2串联起来
        CompletableFuture task2Result = cf1.thenCompose(task2Fn);

        //task2之后，task3和task4可以并行的执行;
        //task3的输入是task2的输出
        Function<String,CompletableFuture<String>> task3Fn = (s) ->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            PrintUtils.print("task3 is running");
           return CompletableFuture.supplyAsync(() -> s + "|task3");
        };

        //task4的输入是task2的输出
        Function<String,CompletableFuture<String>> task4Fn = (s) ->{
            PrintUtils.print("task4 is running");
            return CompletableFuture.supplyAsync(() -> s + "|task4");
        };

        CompletableFuture<String> task3 = task2Result.thenComposeAsync(task3Fn);
        CompletableFuture<String> task4 = task2Result.thenComposeAsync(task4Fn);

        //task3和task4的结果进行组合
        BiFunction<String,String,String> combineTask34Fn = (t,v) -> {
            PrintUtils.print("组合task3 和 task4");
            return "{" + t + "}{"+ v+"}";
        };
        CompletableFuture<String> task34Result = task3.thenCombineAsync(task4,combineTask34Fn);

        //task5的逻辑就是把task34的结果作为输入
        Function<String, CompletableFuture<String>> task5Fn = (s) ->{
            PrintUtils.print("task5 is running");
            return CompletableFuture.supplyAsync(() -> s + "|task5");
        } ;
        CompletableFuture task5Result = task34Result.thenCompose(task5Fn);
        PrintUtils.print("task5 的输出为:" + task5Result.get());
    }




}
