package org.openntf.com.ibm.xsp.extlib.interpreter;

import javax.faces.context.FacesContext;

import org.openntf.com.ibm.xsp.extlib.javacompiler.JavaSourceClassLoader;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.library.FacesClassLoader;

public class DynamicFacesClassLoader implements FacesClassLoader {
	private final DynamicXPageBean dynamicXPageBean;
	private final JavaSourceClassLoader classLoader;

	public DynamicFacesClassLoader(DynamicXPageBean dynamicXPageBean, JavaSourceClassLoader classLoader) {
		this.dynamicXPageBean = dynamicXPageBean;
		this.classLoader=classLoader;
	}
	
	public JavaSourceClassLoader getClassLoader() {
		return classLoader;
	}

	@SuppressWarnings("rawtypes")
	public Class loadClass(String name) throws ClassNotFoundException {
		JavaSourceClassLoader cl = getClassLoader();
		if(cl!=null) {
			if(this.dynamicXPageBean.isDynamicPage(name)) {
				if(cl.isCompiledFile(name)) {
					return cl.loadClass(name);
				}
				String content=this.dynamicXPageBean.getDynamicPage(name);
				if(StringUtil.isNotEmpty(content)) {
					try {
						return this.dynamicXPageBean.compile(name, content);
					} catch (Exception ex) {
						throw new ClassNotFoundException(StringUtil.format("Error while dynamically compiling the XPage class {0}", name), ex);
					}
				}
			}
		}
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			return facesContext.getContextClassLoader().loadClass(name);
		} else {
			return classLoader.loadClass(name);
		}
	}
}