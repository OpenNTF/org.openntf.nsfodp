package org.openntf.xsp.extlibx.bazaar.odpcompiler.eclipse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.ibm.commons.util.NotImplementedException;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.Global;

import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.local.NotesReferenceQueue;

public class CompileODPHandler extends AbstractHandler {
	public static final MessageConsole console = findConsole(CompileODPHandler.class.getPackage().getName());

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MessageConsoleStream out = console.newMessageStream();
		try {
			out.println("hey hey");
			out.println("event: " + event);
			out.println("params: " + event.getParameters());
			out.println("trigger" + event.getTrigger());
			out.println("context: " + event.getApplicationContext());
			
			initNotes();
			lotus.domino.Session session = NotesFactory.createSession();
			out.println("session: " + session);
		
			Job job = new Job("Compile On-Disk Project") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					// Set total number of work units
					monitor.beginTask("start task", 100);
	 
					for (int i = 0; i < 10; i++) {
						try {
							Thread.sleep(1000);
							monitor.subTask("doing " + i);
							// Report that 10 units are done
							monitor.worked(10);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					return Status.OK_STATUS;
				}
			};
	 
			job.schedule();
		} catch(Throwable t) {
			try(PrintWriter pw = new PrintWriter(out)) {
				t.printStackTrace(pw);
				pw.flush();
			}
		} finally {
			StreamUtil.close(out);
			try {
				termNotes();
			} catch(NException e) {
				// Nothing we can do
			}
		}
		return null;
	}

	// h/t https://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in%3F
	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
	
	private static void initNotes() throws NException {
		// libxmlproc
		// no lsxbe in java.library.path
		
		if(SystemUtils.IS_OS_MAC_OSX) {
			// TODO find way to locate Notes programmatically
			String notesPath = "/Applications/IBM Notes.app";
			Path notes = Paths.get(notesPath);
			Path notesBin = notes.resolve("Contents").resolve("MacOS");
			System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + notesBin.toString());
			
//			Path execDir = Paths.get(System.getProperty("user.dir"));
//			Arrays.stream(new String[] {
//					"gsk8iccs",
//					"notes",
//					"xmlproc",
//					"lsxbe"
//				})
//				.map(lib -> "lib" + lib + ".dylib")
//				.map(notesBin::resolve)
//				.map(path -> {
//					try {
//						// TODO NOT THIS
//						Path dest = execDir.resolve(path.getFileName());
//						if(!Files.exists(dest)) {
//							Files.copy(path, dest);
//						}
//						return dest;
//					} catch (IOException e) {
//						throw new RuntimeException(e);
//					}
//				})
//				.collect(Collectors.toList()).stream()
//				.map(String::valueOf)
//				.forEach(System::load);
			NotesThread.isLoaded = true;
			new NotesReferenceQueue(false);
		} else {
			throw new NotImplementedException("Platform not yet implemented: " + System.getProperty("os.name"));
		}
		
//		Global.NotesInit();
		NotesThread.sinitThread();
	}
	
	private static void termNotes() throws NException {
		// TODO delete files copied above
		NotesThread.stermThread();
//		Global.NotesTerm();
	}
}
