import com.jfx.*;
import com.jfx.net.JFXServer;
import com.jfx.strategy.*;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PositionListenerExample {
    String eurjpy = "EURJPY";
    Strategy s;
    boolean ordersComplete;

    private void run() throws IOException, InterruptedException, MT4Exception {
        s = new Strategy();
        s.setPositionListener(new PositionListener() {
            @Override
            public void onInit(PositionInfo initialPositionInfo) {
                System.out.println("Time: " + System.currentTimeMillis() + "> PLSNR: init: " + initialPositionInfo.liveOrders());
            }

            @Override
            public void onChange(PositionInfo currentPositionInfo, PositionChangeInfo changes) {
                for (OrderInfo o : changes.getNewOrders()) {
                    System.out.println("Time: " + System.currentTimeMillis() + "> NEW:      " + o);
                }
                for (OrderInfo o : changes.getModifiedOrders()) {
                    System.out.println("Time: " + System.currentTimeMillis() + "> MODIFIED: " + o);
                }
                for (OrderInfo o : changes.getDeletedOrders()) {
                    System.out.println("Time: " + System.currentTimeMillis() + "> DELETED:  " + o);
                }
                for (OrderInfo o : changes.getClosedOrders()) {
                    System.out.println("Time: " + System.currentTimeMillis() + "> CLOSED:   " + o);
                }
            }
        }, 10, 1000).addTickListener(eurjpy, new Strategy.TickListener() {
            @Override
            public void onTick(TickInfo tick, MT4 connection) {
                System.out.println("(1)" + tick);
            }
        });
        s.connect("127.0.0.1", 7788, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD);
        System.out.println("Connected.");
//        s.addTerminal(eurjpy, new Strategy.TickListener() {
//            @Override
//            public void onTick(TickInfo tick, MT4 connection) {
//                System.out.println("(2)" + tick);
//            }
//        }).connect();
        //
        if (s.orderSelect(0, SelectionType.SELECT_BY_POS, SelectionPool.MODE_TRADES)) {
            s.orderTicket();
        }
        //
        OrderInfo orderInfo = s.orderGet(0, SelectionType.SELECT_BY_POS, SelectionPool.MODE_TRADES);
        if (orderInfo != null) {
            int ticket = orderInfo.getTicket();
            Date openTime = orderInfo.getOpenTime();
            double profit = orderInfo.getProfit();
            // ...
        }
        //
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                try {
                    int tickets[] = new int[5];
                    for (int i = 0; i < tickets.length; i++) {
                        try {
                            System.out.println("Time: " + System.currentTimeMillis() + "> Sending order");
                            tickets[i] = s.orderSend(eurjpy, TradeOperation.OP_BUY, 1, s.marketInfo(eurjpy, MarketInfo.MODE_ASK), 100, 0, 0, "asynch orders example", 0, null, 0);
                            System.out.println("Time: " + System.currentTimeMillis() + "> Order sent: ticket=" + tickets[i]);
                            Thread.sleep(5000);
                        } catch (InterruptedException ignore) {
                            break;
                        } catch (Exception e) {
                            System.out.println("Time: " + System.currentTimeMillis() + "> Order sending error: " + e);
                        }
                    }
                    for (int i = 0; i < tickets.length; i++) {
                        if (tickets[i] > 0) {
                            try {
                                System.out.println("Time: " + System.currentTimeMillis() + "> Closing order");
                                boolean b = s.orderClose(tickets[i], 1, s.marketInfo(eurjpy, MarketInfo.MODE_BID), 100, 0);
                                System.out.println("Time: " + System.currentTimeMillis() + "> Order " + (b ? "closed" : "not closed") + ": ticket=" + tickets[i]);
                                Thread.sleep(2000);
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
        executorService = Executors.newCachedThreadPool();
        executorService.submit(task1);
    }

    ExecutorService executorService;

    public static void main(String[] args) throws Exception {
        System.setProperty("jfx_activation_key", System.getProperty("jfx_activation_key", "235961853"));
//        System.setProperty("jfx_server_port", "17342");
        //
        PositionListenerExample example = new PositionListenerExample();
        System.out.println("Press <Enter> to exit.");
        example.run();
        //noinspection ResultOfMethodCallIgnored
        System.in.read();
        example.s.disconnect();
        System.out.println("Account disconnected");
        example.executorService.shutdown();
        JFXServer.stop();
    }

}
