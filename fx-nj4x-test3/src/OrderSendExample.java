import com.jfx.*;
import com.jfx.strategy.Strategy;

import java.io.IOException;

public class OrderSendExample extends Strategy {

    public OrderSendExample init() throws MT4Exception {
        try {
            connect("127.0.0.1", 7788, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD);
            setReconnect(true);
            System.out.println("Connected to " + accountCompany() + " As " + accountName());
            //
            return this;
        } catch (IOException e) {
            throw new MT4Exception(2, "No connection to TS", e);
        }
    }

    public void newPosition(String symbol, String command, double lots) throws MT4Exception {
        if (command.equals("B"))
        {
            int ticketNo = orderSend(
                    symbol,
                    TradeOperation.OP_BUY,
                    lots,
                    marketInfo(symbol, MarketInfo.MODE_ASK),
                    10,
                    0, 0, "comment", 0, null, 0
            );
        }
        else if (command.equals("S"))
        {

        }
    }

    public static void main(String[] args) throws Exception  {
        System.setProperty("jfx_activation_key", "235961853");
        System.setProperty("jfx_activation_key", "2034239897");
        OrderSendExample orderSendExample = new OrderSendExample();
        orderSendExample.init();
        orderSendExample.newPosition("EURUSD", "B", 0.01);
        Thread.sleep(10000000);
    }

}
