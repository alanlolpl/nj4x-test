import com.jfx.Broker;
import com.jfx.MT4Exception;
import com.jfx.MarketInfo;
import com.jfx.TradeOperation;
import com.jfx.strategy.Strategy;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsynchOrdersOperationsExample {
    String eurjpy = "EURJPY";
    Strategy s;
    boolean ordersComplete;

    private void run() throws IOException, InterruptedException, MT4Exception {
        s = new Strategy();
        s.connect("127.0.0.1", 7788, Account.MT_4_SERVER, Account.MT_4_USER, Account.MT_4_PASSWORD, true);
        System.out.println("Connected.");
        //
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                try {
                    int tickets[] = new int[20];
                    for (int i = 0; i < 20; i++) {
                        try {
                            System.out.println("Time: " + System.currentTimeMillis() + "> Sending order");
                            tickets[i] = s.orderSend(eurjpy, TradeOperation.OP_BUY, 1, s.marketInfo(eurjpy, MarketInfo.MODE_ASK), 100, 0, 0, "asynch orders example", 0, null, 0);
                            System.out.println("Time: " + System.currentTimeMillis() + "> Order sent: ticket=" + tickets[i]);
                            Thread.sleep(500);
                        } catch (InterruptedException ignore) {
                            break;
                        } catch (Exception e) {
                            System.out.println("Time: " + System.currentTimeMillis() + "> Order sending error: " + e);
                        }
                    }
                    for (int i = 0; i < 20; i++) {
                        if (tickets[i] > 0) {
                            try {
                                System.out.println("Time: " + System.currentTimeMillis() + "> Closing order");
                                boolean b = s.orderClose(tickets[i], 1, s.marketInfo(eurjpy, MarketInfo.MODE_BID), 100, 0);
                                System.out.println("Time: " + System.currentTimeMillis() + "> Order " + (b ? "closed" : "not closed") + ": ticket=" + tickets[i]);
                                Thread.sleep(200);
                            } catch (InterruptedException ignore) {
                                break;
                            } catch (Exception e) {
                                System.out.println("Time: " + System.currentTimeMillis() + "> Order closing error: " + e);
                            }
                        }
                    }
                } finally {
                    ordersComplete = true;
                }
            }
        };
        Runnable task2 = new Runnable() {
            @Override
            public void run() {
                try {
                    while (!ordersComplete) {
                        System.out.println("Time: " + System.currentTimeMillis() + "> BID = " + s.marketInfo(eurjpy, MarketInfo.MODE_BID));
                        System.out.println("Time: " + System.currentTimeMillis() + "> OrdersTotal = " + s.ordersTotal());
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ignore) {
                        }
                    }
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        ExecutorService executorService = Executors.newCachedThreadPool();
        // asynchOrderOperations == true: we can do orderSend/Close/Delete/Modify in parallel to other MT4 methods
        System.out.println("--------------------------- asynchOrderOperations: submitting jobs -----------------------------");
        executorService.submit(task1);
        executorService.submit(task2);
        System.out.println("--------------------------- asynchOrderOperations: jobs submitted ------------------------------");
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("jfx_activation_key", "235961853");
        System.setProperty("jfx_server_port", "17342");
        AsynchOrdersOperationsExample example = new AsynchOrdersOperationsExample();
        System.out.println("Press <Enter> to exit.");
        example.run();
        //noinspection ResultOfMethodCallIgnored
        System.in.read();
        example.s.disconnect();
        System.out.println("Account disconnected");
        System.exit(0);
    }

    private static class Account {
        public static final Broker MT_4_SERVER = new Broker("demo1-amsterdam.fxpro.com");
        public static final String MT_4_USER = "6796186";
        public static final String MT_4_PASSWORD = "dpkk4hg";
    }
}
