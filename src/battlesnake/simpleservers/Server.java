package battlesnake.simpleservers;

import battlesnake.logging.Log;

/* A simple server class (well technically, a service) */
public abstract class Server {

	private boolean starting = false;
	private boolean stopping = false;
	private boolean running = false;

	protected abstract void Run() throws Exception;

	protected abstract void PreRun() throws Exception;

	protected abstract void PostRun() throws Exception;

	private RunThread thread = null;

	private class RunThread extends Thread {
		@Override
		public void run() {
			int stage = 0;
			try {
				try {
					PreRun();
					stage = 1;
					setRunning(true);
					setStarting(false);
					Log.Add(Server.this, Log.TYPE_INFO, "Started");
					Run();
					Log.Add(Server.this, Log.TYPE_INFO, "Stopping");
				} catch (Exception e) {
					Log.Add(Server.this, Log.TYPE_INFO, "Failing");
					if (stage == 0)
						Log.Add(Server.this,
								Log.TYPE_FATAL,
								"Server failed to start!  "
										+ e.getClass().getName() + ": "
										+ e.getMessage());
					else if (stage == 1)
						Log.Add(Server.this,
								Log.TYPE_FATAL,
								"Server terminated due to untrapped error!  "
										+ e.getClass().getName() + ": "
										+ e.getMessage());
				}
			} finally {
				try {
					stage = 2;
					try {
						PostRun();
					} catch (Exception e) {
						Log.Add(Server.this,
								Log.TYPE_FATAL,
								"Server failed to close cleanly!  "
										+ e.getClass().getName() + ": "
										+ e.getMessage());
					}
				} finally {
					setRunning(false);
					setStarting(false);
					setStopping(false);
				}
				Log.Add(Server.this, Log.TYPE_INFO, "Stopped");
			}
		}
	}

	public void Start(boolean wait) throws Exception {
		if (getRunning()) {
			Log.Add(this, Log.TYPE_ERR,
					"Attempted to start server when it is already running");
			throw new Exception(
					"Cannot start server: server is already running");
		}
		setStarting(true);
		thread = new RunThread();
		Log.Add(this, Log.TYPE_INFO, "Starting");
		thread.start();
		if (wait)
			while (getStarting())
				Thread.sleep(50);

	}

	public synchronized void Stop() {
		if (getRunning())
			setStopping(true);
	}

	public synchronized void setStopping(boolean value) {
		stopping = value;
	}

	public synchronized boolean getStopping() {
		return stopping;
	}

	public synchronized void setStarting(boolean value) {
		starting = value;
	}

	public synchronized boolean getStarting() {
		return starting;
	}

	public synchronized void setRunning(boolean value) {
		running = value;
	}

	public synchronized boolean getRunning() {
		return running;
	}

}
