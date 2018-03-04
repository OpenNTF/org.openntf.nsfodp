/*
 * � Copyright IBM Corp. 2013
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package com.ibm.xsp.extlib.interpreter;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.ibm.commons.util.AbstractException;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.ReaderInputStream;
import com.ibm.jscript.std.ErrorObject;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.ViewHandlerEx;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.extlib.javacompiler.JavaSourceClassLoader;
import com.ibm.xsp.page.FacesPageDriver;
import com.ibm.xsp.page.compiled.CompiledPageDriver;
import com.ibm.xsp.page.compiled.PageToClassNameUtil;
import com.ibm.xsp.page.parse.ComponentElement;
import com.ibm.xsp.page.parse.FacesDeserializer;
import com.ibm.xsp.page.parse.FacesReader;
import com.ibm.xsp.page.translator.LogicalPage;
import com.ibm.xsp.page.translator.PhysicalPage;
import com.ibm.xsp.page.translator.Translator;
import com.ibm.xsp.registry.FacesSharableRegistry;
import com.ibm.xsp.util.TypedUtil;

/**
 * This is a bean that compiles some XPages source code into a class and
 * manages a custom classloader.
 * 
 * @author priand
 */
public class DynamicXPageBean {
	
	private static final String DYNAMIC_CLASS_LOADER_ENTRY = "Dynamic_JavaSourceClassLoader";
	
	private class DynamicCompiledPageDriver extends CompiledPageDriver {
		public DynamicCompiledPageDriver(DynamicFacesClassLoader classLoader) {
			super(classLoader);
		}
	}

	private List<String> options;
	private String[] classPath;
	private ClassLoader parent;
	
	private JavaSourceClassLoader classLoader;

	public DynamicXPageBean() {
		install();
	}
	
	protected void install() {
		// Install the source class loader
		this.options=getCompilerOptions();
		this.classPath=getCompilerClassPath();
		this.parent=getThreadClassLoader();

		// And then the page driver for it
		ApplicationEx application = ApplicationEx.getInstance();
		if(application != null) {
			ViewHandlerEx viewHandler=(ViewHandlerEx) application.getViewHandler();
			FacesPageDriver driver=viewHandler.getPageDriver();
			if(!(driver instanceof DynamicCompiledPageDriver)) {
				viewHandler.setPageDriver(new DynamicCompiledPageDriver(new DynamicFacesClassLoader(this, classLoader) {
					@Override
					public JavaSourceClassLoader getClassLoader() {
						return getJavaSourceClassLoader();
					}
				}));
			}
		}
	}

	protected ClassLoader getThreadClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
        		ClassLoader cl=Thread.currentThread().getContextClassLoader();
        		return cl;
            }
        });
	}

	public JavaSourceClassLoader getJavaSourceClassLoader() {
		if(isRequestBasedClassLoader()) {
			// Look for a class loader set at the request level
			Map<String, Object> scope = TypedUtil.getRequestMap(FacesContext.getCurrentInstance().getExternalContext());
			JavaSourceClassLoader l = (JavaSourceClassLoader)scope.get(DYNAMIC_CLASS_LOADER_ENTRY);
			if(l==null) {
				l=createJavaSourceClassLoader();
				scope.put(DYNAMIC_CLASS_LOADER_ENTRY, l);
			}
			return l;
		}
		if(classLoader==null) {
			classLoader=createJavaSourceClassLoader();
		}
		return classLoader;
	}
	protected boolean isRequestBasedClassLoader() {
		return false;
	}

	protected JavaSourceClassLoader createJavaSourceClassLoader() {
		return new JavaSourceClassLoader(parent, options, classPath);
	}

	public boolean isDynamicPage(String className) {
		return true;
	}

	public String getDynamicPage(String className) {
		return null;
	}

	protected List<String> getCompilerOptions() {
		return null;
	}

	protected String[] getCompilerClassPath() {
		String[] bundles=new String[] { 
				"com.ibm.commons", 
				"com.ibm.commons.xml", 
				"com.ibm.commons.runtime", 
				"com.ibm.pvc.servlet", 
				"com.ibm.designer.lib.jsf", 
				"com.ibm.xsp.core",
				"com.ibm.xsp.extsn", 
				"com.ibm.xsp.domino", 
				"com.ibm.xsp.designer", 
				"com.ibm.xsp.extlib", 
				"com.ibm.xsp.extlib.controls", 
				"com.ibm.xsp.extlib.core", 
				"com.ibm.xsp.extlib.domino", 
				"com.ibm.xsp.extlib.mobile", 
				"com.ibm.xsp.extlib.oneui", 
				// Should this be externalized elsewhere?
				"com.ibm.sbt.core", 
				"com.ibm.xsp.sbtsdk", };
		return bundles;
	}

	public void discard() {
		install();
	}

	public boolean isCompiled(String pageName) throws Exception {
		// Make sure it ends with .xsp
		if(!pageName.endsWith(".xsp")) {
			pageName+=".xsp";
		}
		String className=PageToClassNameUtil.getClassNameForPage(pageName);

		// We make sure that the class does not yet exists
		JavaSourceClassLoader loader=getJavaSourceClassLoader();
		return loader.isCompiledFile(className);
	}
	public Class<?> compile(String pageName, String pageContent, FacesSharableRegistry registry) throws Exception {
		// Make sure it ends with .xsp
		if(!pageName.endsWith(".xsp")) {
			pageName+=".xsp";
		}
		String className=PageToClassNameUtil.getClassNameForPage(pageName);

		// We make sure that the class does not yet exist
		JavaSourceClassLoader loader=getJavaSourceClassLoader();
		if(loader.isCompiledFile(className)) {
			return loader.loadClass(className);
		}

		// Translate and compile it
		String javaPage=null;
		try {
			if(registry == null) {
				javaPage=translate(className, pageName, pageContent);
			} else {
				javaPage=translate(className, pageName, pageContent, registry);
			}
		} catch (Exception e) {
			throw new DynamicXPagesException(e, pageContent, null, "Error while compiling the XPages source");
		}
		// Invoke the Java compiler
		try {
			return loader.addClass(className, javaPage);
		} catch (Exception e) {
			throw new DynamicXPagesException(e, pageContent, javaPage, "Error while compiling the XPages generated Java source");
		}
	}
	public Class<?> compile(String pageName, String pageContent) throws Exception {
		return compile(pageName, pageContent, null);
	}
	
	public String translate(String className, String pageName, String pageContent, FacesSharableRegistry registry) throws Exception {
		FacesDeserializer deserial;
		{
			Map<String, Object> options=new HashMap<String, Object>();
			// allowNamespacedMarkupTags defaults to true in FacesDeserializer
			// but defaults to false in the design-time code.
			options.put(FacesDeserializer.OPTION_ALLOW_NAMESPACED_MARKUP_TAGS, true); // /???
			deserial=new FacesDeserializer(registry, options);
		}

		InputStream in=new ReaderInputStream(new StringReader(pageContent));
		ComponentElement root;
		try {
			FacesReader reader=new FacesReader(in);
			root=deserial.readRoot(reader);
		} finally {
			in.close();
		}

		// Ok, we generate the XPages source code
		Map<String, Object> options=new HashMap<String, Object>();
		// options.put(Translator.OPTION_APPLICATION_VERSION,
		// applicationVersion);
		// options.put(Translator.OPTION_ERROR_HANDLER, errHandler);
		Translator compiler=new Translator(registry, options);

		// Compile the page
		boolean isCustomControl=false;
		LogicalPage logical=new LogicalPage(className, pageName, isCustomControl);
		PhysicalPage physical=new PhysicalPage("", root, "", 0);
		logical.addMainPage(physical);

		String result=compiler.translate(logical);
		return result;
	}

	/**
	 * Translates XSP source into an intermediate Java source representation.
	 * 
	 * @param className the final qualified name of the Java source, e.g. "xsp.SomeXPage"
	 * @param pageName the XPage file name, e.g. "SomeXPage.xsp"
	 * @param pageContent
	 * @return
	 * @throws Exception
	 */
	public String translate(String className, String pageName, String pageContent) throws Exception {
		FacesContextEx ctx=FacesContextEx.getCurrentInstance();

		FacesSharableRegistry registry=ctx.getApplicationEx().getRegistry();

		return translate(className, pageName, pageContent, registry);
	}
	
	public String compilationExceptionString(Object e, boolean xpagesSource, boolean javaSource) {
		StringWriter sw = new StringWriter(1024);;
		PrintWriter pw = new PrintWriter(sw);
		
		if(e instanceof ErrorObject) {
			ErrorObject t = (ErrorObject)e;
			pw.println(StringUtil.toString(t.getMessage()));
		}
		if(e instanceof Throwable) {
			Throwable t = (Throwable)e;
			pw.println(t.getMessage());
			AbstractException.printExtraInformation(pw,t);
			if(e instanceof DynamicXPagesException) {
				DynamicXPagesException de = (DynamicXPagesException)e;
				if(xpagesSource && de.getXPagesSource()!=null) {
					pw.println(de.getXPagesSource());
				}
				if(javaSource && de.getTranslatedJava()!=null) {
					pw.println(de.getTranslatedJava());
				}
			}
		}
		//e.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}
}
