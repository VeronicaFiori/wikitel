package it.cnr.istc.psts.wikitel.controller;
import java.util.logging.Logger;


public class Executor {
	private static final Logger LOG = Logger.getLogger(Executor.class.getName());

	private int tickCounter;

	public Executor() {
		this.tickCounter = 0;
	}

	public void tick() throws ExecutorException {
		try {
			tickCounter++;
//			if (tickCounter % 5 == 0) { 
//				throw new ExecutorException("Simulated execution error.");
//			}
			LOG.info("Tick executed successfully. Tick count: " + tickCounter);
		} catch (Exception e) {
			LOG.severe("Error during tick execution: " + e.getMessage());
			throw new ExecutorException("Execution failed during tick.", e);
		}
	}
	public int getTickCounter() {
        return tickCounter;
    }
}


class ExecutorException extends Exception {
	public ExecutorException(String message) {
		super(message);
	}

	public ExecutorException(String message, Throwable cause) {
		super(message, cause);
	}
}


