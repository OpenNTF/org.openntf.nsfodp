package servlet.schema;

import javax.servlet.annotation.WebServlet;

@WebServlet("/xe.xsd")
public class ExtLibSchemaServlet extends AbstractSchemaServlet {
	private static final long serialVersionUID = 1L;

	public ExtLibSchemaServlet() {
		super("http://www.ibm.com/xsp/coreex", "http://www.ibm.com/xsp/core");
	}
}
