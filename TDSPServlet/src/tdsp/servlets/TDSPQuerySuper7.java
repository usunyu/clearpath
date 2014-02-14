package tdsp.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

public class TDSPQuerySuper7 extends HttpServlet {
	@Override
    public void init(ServletConfig config){
    	try {
            super.init(config);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
}
