package org.openntf.xsp.extlibx.bazaar.odpcompiler.eclipse;

import java.io.PrintWriter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class CompileODPHandler extends AbstractHandler {
	public static final MessageConsole console = findConsole(CompileODPHandler.class.getPackage().getName());

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MessageConsoleStream out = console.newMessageStream();
		try {
			
		} catch(Throwable t) {
			try(PrintWriter pw = new PrintWriter(out)) {
				t.printStackTrace(pw);
				pw.flush();
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
}
