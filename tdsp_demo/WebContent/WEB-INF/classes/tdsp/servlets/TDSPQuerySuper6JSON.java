package tdsp.servlets;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.URLConnection;
import java.net.URL;
import org.json.*;
import java.net.*;
import java.lang.Object;
import java.nio.charset.Charset;

public class TDSPQuerySuper6JSON extends HttpServlet {


    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        PrintWriter out = response.getWriter();
		
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		
        String startOrds = request.getParameter("start");
        String endOrds = request.getParameter("end");
        String time = request.getParameter("time");
        String update = request.getParameter("update");
        String day = request.getParameter("day");
        		
		
		String urlString = "http://cs-server.usc.edu:24723/cgi-bin/getLatLon.pl?" + 
							"start=" + startOrds +
							"&end=" + endOrds + 
							"&time=" + time +
							"&update=" + update +
							"&day=" + day;
		
		//out.println(urlString);
		URL url = new URL(urlString);
		URLConnection urlConnection = url.openConnection();
		//urlConnection.setDoOutput(true);
		urlConnection.setAllowUserInteraction(false);
		//Charset charset = Charset.forName("UTF-8");
		//BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), charset));
		BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));

		
		String xml="";
		String inputLine;
		//out.println("Hello I am here !!");
		while((inputLine = in.readLine()) != null){
			xml += inputLine;
			//out.println(inputLine);
		}
		in.close();
		
		//String xml_utf = new String(xml.getBytes("UTF-8"),"UTF-8");
		//out.println(xml_utf);
		
		try {
			JSONObject jsonObject = XML.toJSONObject(xml);
			//out.println();
			out.println(jsonObject);
		} catch(JSONException e){
			e.printStackTrace();
		}
		out.flush();
    }
}