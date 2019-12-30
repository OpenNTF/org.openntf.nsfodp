package servlet.schema;

import javax.servlet.annotation.WebServlet;

@WebServlet("/xp.xsd")
public class XspCoreSchemaServlet extends AbstractSchemaServlet {
	private static final long serialVersionUID = 1L;

	public XspCoreSchemaServlet() {
		super("http://www.ibm.com/xsp/core", "http://www.ibm.com/xsp/jsf/core");
	}
}
