package org.openntf.nsfodp.commons.odp.notesapi;

public interface NNote extends AutoCloseable {
	void save();
	void sign();
	
	void compileLotusScript() throws NLotusScriptCompilationException;
	
	boolean hasItem(String itemName);
	
	<T> T get(String itemName, Class<T> valueClass);
	String getAsString(String itemName, char delimiter);
	NCompositeData getCompositeData(String itemName);
	void set(String itemName, Object value);
	
	Iterable<String> getItemNames();
	
	short getNoteClassValue();
	
	int getNoteID();
	
	boolean isRefValid();
	
	@Override void close();
}
