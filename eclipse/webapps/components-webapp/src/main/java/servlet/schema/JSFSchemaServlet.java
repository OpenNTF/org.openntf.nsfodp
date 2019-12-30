package servlet.schema;

import javax.servlet.annotation.WebServlet;

@WebServlet("/f.xsd")
public class JSFSchemaServlet extends AbstractSchemaServlet {
	private static final long serialVersionUID = 1L;

	public JSFSchemaServlet() {
		super("http://www.ibm.com/xsp/jsf/core");
	}
}
