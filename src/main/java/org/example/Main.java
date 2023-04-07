package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(5);
//        CountDownLatch countDownLatch = new CountDownLatch(10);
        ExecutorService executorService = Executors.newCachedThreadPool();
//        for (int i = 0; i < 10; i++) {
//            executorService.execute(new SemaphoreRunnable(semaphore));
//        }
//        List<Future<?>> result = new ArrayList<>();
//        for (int i = 0; i < 15; i++) {
//            Future<?> future = executorService.submit(new CountDownLatchTask(countDownLatch));
//            result.add(future);
//        }
//        try {
//            countDownLatch.await();
//
//            System.out.println("Done");
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        Random random = new Random();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100000000; i++) {
            list.add(1);
        }

        long startTime = System.currentTimeMillis();
        long sumFromStream = list.stream().mapToInt(i -> i)
                .sum();
        System.out.println(sumFromStream);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        startTime = System.currentTimeMillis();
        long sumFromParallelStream = list.parallelStream().mapToInt(i -> i)
                .sum();
        System.out.println(sumFromParallelStream);
        endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);

        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        AtomicReference<Integer> totalSum = new AtomicReference<>();

//        completableFuture.
//        completableFuture.whenComplete((result, error) -> {
//            if (error != null) {
//                error.getMessage();
//            }
//            totalSum.set(totalSum.get() + result);
//        });
        final int N = 8;
        CountDownLatch count = new CountDownLatch(1);
        long totalSum = 0L;
        int start = 0;

//        int average = (int) Math.ceil((list.size() * 1.0) / N);
        int average = (list.size() + N - 1) / N;
        List<Future<Long>> futureList = new ArrayList<>();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < N; i++) {
//            int iterator = start;
//            while (iterator < start + average && iterator < list.size()) {
//                splittedArray.add(list.get(iterator));
//                iterator++;
//            }
            Future<Long> partialSum = executorService.submit(new SumCallable(count, list, start, start + average));
            futureList.add(partialSum);
            start += average;
        }
        try {
            count.await();
            for (Future<Long> partialSumFuture : futureList) {
                totalSum += partialSumFuture.get();
            }
            System.out.println(totalSum);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);

//        executorService.shutdown();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        ExecutorService executorService1 = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService1.execute(new CyclicBarrierRunnable(cyclicBarrier));
        }
    }
}

class CountDownLatchTask implements Runnable {
    CountDownLatch countDownLatch;

    public CountDownLatchTask(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        System.out.println("man ishim qilib bo'ldim " + Thread.currentThread().getName());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        countDownLatch.countDown();
    }
}

class SemaphoreRunnable implements Runnable {
    private Semaphore semaphore;

    public SemaphoreRunnable(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            System.out.println("man resource oldim " + Thread.currentThread().getName());
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }
}

class SumCallable implements Callable<Long> {
    CountDownLatch countDownLatch;
    List<Integer> listToAdd;
    int start, finish;

    public SumCallable(CountDownLatch countDownLatch, List<Integer> listToAdd, int start, int finish) {
        this.countDownLatch = countDownLatch;
        this.listToAdd = listToAdd;
        this.start = start;
        this.finish = finish;
    }

    @Override
    public Long call() throws Exception {
        Long sum = 0L;
        for (int i = start; i < finish && i < listToAdd.size(); i++) {
            sum += listToAdd.get(i);
        }
        countDownLatch.countDown();
        return sum;
    }
}
class CyclicBarrierRunnable implements Runnable {
    CyclicBarrier barrier;

    public CyclicBarrierRunnable(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+": Etap 1");
//        CommonTask.doTask();
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+": Etap 2");
//        barrier.reset();
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+": Etap 3");

    }
}