package org.openntf.nsfodp.notesapi.darwinonapi;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.darwino.domino.napi.DominoThread;

public class DarwinoNAPIThreadFactory implements ThreadFactory {
	private static AtomicInteger spawnCount = new AtomicInteger(0);

	@Override
	public Thread newThread(Runnable r) {
		int count = spawnCount.incrementAndGet();
		return new DominoThread(r, "DominoThreadFactory Thread " + count); //$NON-NLS-1$
	}

}
