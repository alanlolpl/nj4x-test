package net.alanlo.fx.poc;

import java.io.IOException;
import java.util.ArrayList;

import com.jfx.AppliedPrice;
import com.jfx.Broker;
import com.jfx.ErrAccountDisabled;
import com.jfx.ErrCommonError;
import com.jfx.ErrCustomIndicatorError;
import com.jfx.ErrIntegerParameterExpected;
import com.jfx.ErrInvalidAccount;
import com.jfx.ErrInvalidFunctionParamvalue;
import com.jfx.ErrInvalidPrice;
import com.jfx.ErrInvalidPriceParam;
import com.jfx.ErrInvalidStops;
import com.jfx.ErrInvalidTradeParameters;
import com.jfx.ErrInvalidTradeVolume;
import com.jfx.ErrLongPositionsOnlyAllowed;
import com.jfx.ErrLongsNotAllowed;
import com.jfx.ErrMarketClosed;
import com.jfx.ErrNoConnection;
import com.jfx.ErrNotEnoughMoney;
import com.jfx.ErrOffQuotes;
import com.jfx.ErrOldVersion;
import com.jfx.ErrOrderLocked;
import com.jfx.ErrPriceChanged;
import com.jfx.ErrRequote;
import com.jfx.ErrServerBusy;
import com.jfx.ErrShortsNotAllowed;
import com.jfx.ErrStringParameterExpected;
import com.jfx.ErrTooFrequentRequests;
import com.jfx.ErrTooManyRequests;
import com.jfx.ErrTradeContextBusy;
import com.jfx.ErrTradeDisabled;
import com.jfx.ErrTradeExpirationDenied;
import com.jfx.ErrTradeModifyDenied;
import com.jfx.ErrTradeNotAllowed;
import com.jfx.ErrTradeTimeout;
import com.jfx.ErrTradeTimeout2;
import com.jfx.ErrTradeTimeout3;
import com.jfx.ErrTradeTimeout4;
import com.jfx.ErrTradeTooManyOrders;
import com.jfx.ErrUnknownSymbol;
import com.jfx.MT4;
import com.jfx.MarketInfo;
import com.jfx.MarketInformation;
import com.jfx.MovingAverageMethod;
import com.jfx.TickInfo;
import com.jfx.Timeframe;
import com.jfx.TradeOperation;
import com.jfx.strategy.OrderInfo;
import com.jfx.strategy.PositionChangeInfo;
import com.jfx.strategy.PositionInfo;
import com.jfx.strategy.PositionListener;
import com.jfx.strategy.Strategy;
import com.jfx.strategy.Strategy.Instrument;
import com.jfx.strategy.Strategy.TickListener;

public class MT4Terminal extends Strategy {

	private String terminalServerIp;
	private int terminalServerPort;

	private Broker mt4Server;
	private String mt4Login;
	private String mt4Password;

	public MT4Terminal(String terminalServerIp, int terminalServerPort, String mt4Server, String mt4Login,
			String mt4Password) {
		this.terminalServerIp = terminalServerIp;
		this.terminalServerPort = terminalServerPort;
		this.mt4Server = new Broker(mt4Server);
		this.mt4Login = mt4Login;
		this.mt4Password = mt4Password;
	}

	public void finalize() {
		System.out.println("MT4 Terminal client disconnect...");
		try {
			this.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect() throws IOException {
		System.out.println("Connecting to MT4");
		this.connect(terminalServerIp, terminalServerPort, mt4Server, mt4Login, mt4Password);
		addShutdownHook();
		System.out.println("Connected to MT4");
	}

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                	MT4Terminal.this.close(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

	public void printtest() {
		System.out.println("printtest(): ");

		System.out.println("Strategy , acc=" + this.getMt4User());
		System.out.println("Strategy , acc=" + this.accountName());
		System.out.println("Strategy , bal=" + this.accountBalance());
		System.out.println("Strategy , sym=" + this.getSymbol());
		this.symbol = "USDJPY";
		System.out.println("Strategy , sym=" + this.getSymbol());
		
		long chartId = this.chartOpen("USDJPY.em", Timeframe.PERIOD_M5);
		System.out.println("Strategy , chartId=" + chartId);

		/*
		 * ArrayList<String> a = this.getSymbols(); for (String symbol : a) {
		 * System.out.println("symbol: " + symbol); }
		 */
		
		ArrayList<Strategy.Terminal> terminals = new ArrayList<Strategy.Terminal>();

		Strategy.Terminal terminal = null;
		
		/*
		try {
			terminal = this.addTerminal("USDJPY.em", new TickListener() {
				@Override
				public void onTick(TickInfo tick, MT4 connection) {
					System.out.println(connection.symbol() + ": " + tick.bid + "/" + tick.ask);
					try {
						long ticketId = connection.orderSend("USDJPY.em", TradeOperation.OP_BUY, 0.1, tick.ask, 10, 0, 0, "EA?", 12345, null);
						System.out.println("New order: " + ticketId);
					} catch (ErrInvalidFunctionParamvalue | ErrCustomIndicatorError | ErrStringParameterExpected
							| ErrIntegerParameterExpected | ErrUnknownSymbol | ErrInvalidPriceParam | ErrTradeNotAllowed
							| ErrLongsNotAllowed | ErrShortsNotAllowed | ErrCommonError | ErrInvalidTradeParameters
							| ErrServerBusy | ErrOldVersion | ErrNoConnection | ErrTooFrequentRequests
							| ErrAccountDisabled | ErrInvalidAccount | ErrTradeTimeout | ErrInvalidPrice
							| ErrInvalidStops | ErrInvalidTradeVolume | ErrMarketClosed | ErrTradeDisabled
							| ErrNotEnoughMoney | ErrPriceChanged | ErrOffQuotes | ErrRequote | ErrOrderLocked
							| ErrLongPositionsOnlyAllowed | ErrTooManyRequests | ErrTradeTimeout2 | ErrTradeTimeout3
							| ErrTradeTimeout4 | ErrTradeModifyDenied | ErrTradeContextBusy | ErrTradeExpirationDenied
							| ErrTradeTooManyOrders e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).connect();
			
			terminals.add(terminal);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		
		
		
//
//		try {
//			terminal = this.addTerminal("EURUSD.em", new TickListener() {
//				@Override
//				public void onTick(TickInfo tick, MT4 connection) {
//					System.out.println(connection.symbol() + ": " + tick.bid + "/" + tick.ask);
//				}
//			}).connect();
//
//			terminals.add(terminal);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		try {
			terminal = this.addTerminal(TerminalType.ORDERS_WORKER).connect();

			terminals.add(terminal);
			
			MarketInformation mi = marketInfo("USDJPY.em");
			terminal.getMt4Connection().orderSend("USDJPY.em", TradeOperation.OP_BUY, 1, mi.ASK, 10, mi.ASK - mi.POINT * 200, mi.ASK + mi.POINT * 200, "EA?", 12345, null);
			
		} catch (IOException | ErrUnknownSymbol | ErrInvalidFunctionParamvalue | ErrCustomIndicatorError | ErrStringParameterExpected | ErrIntegerParameterExpected | ErrInvalidPriceParam | ErrTradeNotAllowed | ErrLongsNotAllowed | ErrShortsNotAllowed | ErrCommonError | ErrInvalidTradeParameters | ErrServerBusy | ErrOldVersion | ErrNoConnection | ErrTooFrequentRequests | ErrAccountDisabled | ErrInvalidAccount | ErrTradeTimeout | ErrInvalidPrice | ErrInvalidStops | ErrInvalidTradeVolume | ErrMarketClosed | ErrTradeDisabled | ErrNotEnoughMoney | ErrPriceChanged | ErrOffQuotes | ErrRequote | ErrOrderLocked | ErrLongPositionsOnlyAllowed | ErrTooManyRequests | ErrTradeTimeout2 | ErrTradeTimeout3 | ErrTradeTimeout4 | ErrTradeModifyDenied | ErrTradeContextBusy | ErrTradeExpirationDenied | ErrTradeTooManyOrders e) {
			e.printStackTrace();
		}

		try {
			terminal = this.addTerminal(TerminalType.ORDERS_WORKER);

			terminals.add(terminal);
			
			terminal.connect();
						
			MarketInformation mi = marketInfo("EURUSD.em");
			terminal.getMt4Connection().orderSend("EURUSD.em", TradeOperation.OP_SELL, 1, mi.BID, 10, mi.BID - mi.POINT * 200, mi.BID + mi.POINT * 200, "EA?", 12345, null);
			
		} catch (IOException | ErrUnknownSymbol | ErrInvalidFunctionParamvalue | ErrCustomIndicatorError | ErrStringParameterExpected | ErrIntegerParameterExpected | ErrInvalidPriceParam | ErrTradeNotAllowed | ErrLongsNotAllowed | ErrShortsNotAllowed | ErrCommonError | ErrInvalidTradeParameters | ErrServerBusy | ErrOldVersion | ErrNoConnection | ErrTooFrequentRequests | ErrAccountDisabled | ErrInvalidAccount | ErrTradeTimeout | ErrInvalidPrice | ErrInvalidStops | ErrInvalidTradeVolume | ErrMarketClosed | ErrTradeDisabled | ErrNotEnoughMoney | ErrPriceChanged | ErrOffQuotes | ErrRequote | ErrOrderLocked | ErrLongPositionsOnlyAllowed | ErrTooManyRequests | ErrTradeTimeout2 | ErrTradeTimeout3 | ErrTradeTimeout4 | ErrTradeModifyDenied | ErrTradeContextBusy | ErrTradeExpirationDenied | ErrTradeTooManyOrders e) {
			e.printStackTrace();
		}

		for(Strategy.Terminal t : terminals) {
			System.out.println("Terminal: " + t.getId() + " " + t.getType());
		}

		System.out.println("printtest() end ");
	}

    boolean areIndicatorsDone = false;

	@Override
	public void coordinate() {
//		System.out.println("--------------- " + coordinationIntervalMillis() + " " + getSymbol());
//        if (!areIndicatorsDone) {
//    		System.out.println("calcIndicators ");
//            calcIndicators(getSymbol(), "**** CALLED FROM COORDINATE *****");
//            areIndicatorsDone = true;
//        }
	}

    @Override
    public void deinit() {
        System.out.println("-- Deinit --");
        super.deinit();
    }

    @Override
	public synchronized void init(String symbol, int period) {
        System.out.println("-- Init --" + symbol + " " + period);
        super.init(symbol, period);
        System.out.println("-- Init --");
    }
    
    public void preConnect() {
        this.setPositionListener(new PositionListener() {
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
        }, 10, 1000);
    }

    private void calcIndicators(String symbol, String msg) {
        try {
//            iCustom(getSymbol(), Timeframe.PERIOD_H1, "ZigZag", 0, 0, 12, 5, 3);
            //
            //String s = "USDJPY.em";
            //boolean contains = getSymbols().contains(new Instrument(s, s, s));
            //if (contains) {
            //    double ask = marketInfo(symbol, MarketInfo.MODE_ASK);
            //}
            System.out.println(msg);
            double[] movingAverages = new double[10];
    		System.out.println("getSymbol() " + getSymbol());
    		System.out.println("getTimeframe() " + getTimeframe().val);
    		System.out.println("getPeriod() " + getPeriod());
            for (int i = 0; i < movingAverages.length; i++) {
        		System.out.println("i " + i);
                movingAverages[i] = iMA(getSymbol(), getTimeframe(), getPeriod(),
                        13, MovingAverageMethod.MODE_SMA, AppliedPrice.PRICE_CLOSE,
                        i);
            }
            //
            System.out.println("----------- MA (13, SMA, CLOSE) --------------");
            for (int i = 0; i < movingAverages.length; i++) {
                System.out.print("" + movingAverages[i] + " ");
                if (i > 0 && (i+1) % 10 == 0) {
                    System.out.println();
                }
            }
            System.out.println("--------- End Of MA (13, SMA, CLOSE) -----------");
        } catch (ErrUnknownSymbol errUnknownSymbol) {
            errUnknownSymbol.printStackTrace();
        }
    }

	@Override
	public long coordinationIntervalMillis() {
		return super.coordinationIntervalMillis();
	}

	public static void main(String[] args) {
        System.setProperty("jfx_activation_key", "235961853");
		//MT4Terminal mt4 = new MT4Terminal("127.0.0.1", 7788, "mdv.empfs.com:55443", "27098158", "abc123");
		MT4Terminal mt4 = new MT4Terminal("127.0.0.1", 7788, "EmperorFinancial-Demo", "800035556", "5uqn894");
		System.out.println("isLimitedFunctionality: " + mt4.isLimitedFunctionality());
		try {
			mt4.preConnect();
			mt4.connect();
			mt4.printtest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mt4.printtest();

		//
		System.out.println("1-minute example ...");
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		System.out.println("Exit.");
		System.exit(0);
	}

}
