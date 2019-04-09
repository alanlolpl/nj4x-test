package net.alanlo.fx.poc;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TickManager {

	private static final Logger logger = LogManager.getLogger(TickManager.class);

	private long rawCount;
	private long quoteCount;

	private Timer timer;
	private HeartbeatTask heartbeatTask;
	private MonitorTask monitorTask;

	private ReceiveMessageProcessor receiveMessageProcessor;
	private ConcurrentLinkedQueue<String> receiveQueue;

	private HashSet<String> subscribeQuotes;

	private AtomicInteger connectionStatus;

	public TickManager() {
		timer = new Timer();
		heartbeatTask = new HeartbeatTask();
		monitorTask = new MonitorTask();

		receiveQueue = new ConcurrentLinkedQueue<String>();
		receiveMessageProcessor = new ReceiveMessageProcessor(receiveQueue);
		subscribeQuotes = new HashSet<String>();

		receiveMessageProcessor.start();
	}

	public void subscribeQuote(String curr) {
		synchronized (subscribeQuotes) {
			subscribeQuotes.add(curr);
			logger.info("[{}] Subscribe quote: {}", feedName, curr);
		}
	}

	public synchronized void connect() {
		if (connectionStatus.get() == CONN_STATUS_CONNECTED) {
			return;
		}

		logger.info("[{}] Initializing Online Feed connection...", feedName);

		if (feedConn != null) {
			try {
				feedConn.close();
			} catch (IOException e) {
				logger.error("[" + feedName + "] " + "Error: " + e.getMessage(), e);
			}
			feedConn = null;
		}

		rawCount = 0;
		quoteCount = 0;
		timer.schedule(monitorTask = new MonitorTask(), 0, 60000);

		connectionStatus.set(CONN_STATUS_CONNECTING);
		while (connectionStatus.get() != CONN_STATUS_CONNECTED) {
			logger.info("[{}] Start connecting...", feedName);

			try {
				feedConn = new NonBlockingConnection(this.hostname, this.port, new OnlineFeedConnectionHandler(this));
				feedConn.setIdleTimeoutMillis(idleTimeout);
				feedConn.setAutoflush(false);
				feedConn.setFlushmode(FlushMode.SYNC);

				connectionStatus.set(CONN_STATUS_CONNECTED);

				timer.schedule(heartbeatTask = new HeartbeatTask(), heartbeatPeriod / 2, heartbeatPeriod);

			} catch (Exception e) {
				logger.error("[" + feedName + "] " + "Error when connecting to feed: {}:{}. Reason: {}", this.hostname, this.port, e.getMessage());
			}

			try {
				Thread.sleep(RandomUtils.nextInt(1, 6) * 1000);
			} catch (InterruptedException e) {
			}
		}
	}

	public synchronized void disconnect() {
		logger.info("[{}] Try to close Online Feed connection...", feedName);
		if (feedConn != null && feedConn.isOpen()) {
			try {
				feedConn.setHandler(null);
				feedConn.close();
			} catch (Exception e) {
				logger.error("[" + feedName + "] " + "Error: " + e.getMessage(), e);
			}
			synchronized (receiveQueue) {
				receiveQueue.notifyAll();
			}
			clear();
		}

		if (heartbeatTask != null) {
			heartbeatTask.cancel();
		}
		if (monitorTask != null) {
			monitorTask.cancel();
		}

		connectionStatus.set(CONN_STATUS_DISCONNECT);
		logger.info("[{}] Close Online Feed connection finish...", feedName);

		try {
			Thread.sleep(RandomUtils.nextInt(1, 11) * 1000);
		} catch (InterruptedException e) {
		}

		logger.info("[{}] Try to reconnect to Online Feed...", feedName);
		connect();
	}

	public void clear() {
		receiveQueue.clear();
	}

	public void receiveMessage(String receiveMsg) {
		try {
			receiveQueue.offer(receiveMsg);
			synchronized (receiveQueue) {
				receiveQueue.notifyAll();
			}
			rawCount++;
		} catch (Exception ex) {
			logger.error("[" + feedName + "] " + "Error: " + ex.getMessage(), ex);
		}
	}

	public void sendMessage(String sendMsg) {
		logger.debug("[{}] Send: {}", feedName, sendMsg);
		try {
			synchronized (feedConn) {
				feedConn.markWritePosition();
				feedConn.write((int) 0);
				int written = feedConn.write(sendMsg);
				feedConn.resetToWriteMark();
				feedConn.write(written);
				feedConn.flush();
			}
		} catch (Exception ex) {
			logger.error("[" + feedName + "] " + "Error: " + ex.getMessage(), ex);
		}
	}

	protected class ReceiveMessageProcessor extends Thread {
		private ConcurrentLinkedQueue<String> receiveQueue;

		public ReceiveMessageProcessor(ConcurrentLinkedQueue<String> receiveQueue) {
			this.receiveQueue = receiveQueue;
			this.setPriority(Thread.MAX_PRIORITY);
		}

		public void run() {
			logger.info("[{}] ReceiveMessageProcessor start", feedName);
			while (true) {
				if (receiveQueue == null) {
					logger.error("[{}] ReceiveMessageProcessor's receiveQueue is null", feedName);
					break;
				}
				if (receiveQueue.isEmpty()) {
					try {
						synchronized (receiveQueue) {
							receiveQueue.wait();
						}

					} catch (InterruptedException ex) {
						logger.error("[" + feedName + "] " + "Error: " + ex.getMessage(), ex);
					}
				}

				String curr = "";
				String bid = "";
				String ask = "";
				Date dateTime = null;
				boolean ignoreQuote = false;

				while (!receiveQueue.isEmpty()) {
					try {
						String receiveMsg = receiveQueue.poll();

						if (receiveMsg != null) {
							int index = 0;
							StringTokenizer st = new StringTokenizer(receiveMsg, "#");
							while (st.hasMoreTokens()) {
								switch (index) {
								case 0:
									curr = st.nextToken();
									break;
								case 1:
									bid = st.nextToken();
									break;
								case 2:
									ask = st.nextToken();
									break;
								case 3:
									try {
										long time = Long.parseLong(st.nextToken());
										dateTime = new Date(time);
									} catch (Exception e) {
										logger.error("[" + feedName + "] " + "Error: " + e.getMessage(), e);
									}
									break;
								default:
									break;
								}
								if (subscribeQuotes.size() > 0 && !subscribeQuotes.contains(curr)) {
									ignoreQuote = true;
									break;
								}
								index++;
							}
							if (!ignoreQuote) {
								PriceQuote quote = new PriceQuote(curr, bid, ask, dateTime);
								server.addToPriceQueue(quote);
								quoteCount++;
							} else {
								ignoreQuote = false;
							}
						}
					} catch (Exception ex) {
						logger.error("[" + feedName + "] " + "Error: " + ex.getMessage(), ex);
					}

				}

			}
			logger.info("[{}] ReceiveMessageProcessor end", feedName);
		}
	}

	protected class HeartbeatTask extends TimerTask {
		public HeartbeatTask() {
		}

		public void run() {
			sendMessage(MSG_HEARTBEAT);
		}
	}

	protected class MonitorTask extends TimerTask {
		public MonitorTask() {
		}

		public void run() {
			logger.info("[{}] Feed Monitor - Raw: {} - Quote: {}", feedName, rawCount, quoteCount);
		}
	}
}
