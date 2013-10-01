package tdsp.repos;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import Objects.LinkInfo;
import Objects.PairInfo;

public class LinkToNodesInCSV {

	static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
    static String userName = "clearp";
    static String password = "clearp";
    static LinkInfo info[];
    static PairInfo [] nodes;
    static int node_count = 0;

    public static void main(String[] args) {
        try{
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            System.out.print("connecting home database.....");
            Connection connHome = DriverManager.getConnection(url_home, userName, password);
         
            Date start = new Date();
            String sql = "select s.link_id, s.func_class, s.geom from los_angeles_3 s";
            PreparedStatement fetch = connHome.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            
            fetch.setFetchSize(100000);
            ResultSet result = fetch.executeQuery();
            Date end = new Date();
            System.out.println("finished, gettint to last row ..");
            
            result.last();
            System.out.println("getting rownums ..");
            int rownum = result.getRow();
            info = new LinkInfo[rownum];
            
            System.out.println("finished,"+rownum+" records returned .... getting to first row...");
            result.beforeFirst();
            System.out.println("after resetting cursor ..");
            FileWriter writer = new FileWriter("C:\\Users\\Shireesh\\Desktop\\Links_all_2.csv");
            FileWriter writer_txt = new FileWriter("C:\\Users\\Shireesh\\Desktop\\Links_all_2.txt");
            FileWriter writer2 = new FileWriter("C:\\Users\\Shireesh\\Desktop\\Nodes_2.csv");
            
            int i = 0;
            
            nodes = new PairInfo[rownum*2];
            for(int i1=0;i1<rownum*2;i1++)
            	nodes[i1]= new PairInfo(new Double(180.0),new Double(180.0));
            
            for(i=0;i<rownum;i++){
            	
            	result.next();
                int linkid = result.getInt(1);
                int func_class = result.getInt(2);
                //System.out.println(linkid);
                STRUCT st = (STRUCT) result.getObject(3);
                JGeometry geom = JGeometry.load(st);
                double [] ords = geom.getOrdinatesArray();
                PairInfo [] pairs = new PairInfo[100];
                int pair_count = 0;
                
                for(int j=0;j<ords.length;j=j+2)
                {   
                	pairs[pair_count] = new PairInfo(ords[j+1],ords[j]);
                	pair_count++;
                }
                  
                info[i] = new LinkInfo(linkid, func_class, pairs,pair_count);
                
                if(!(Check(nodes,pairs[0])))
            		nodes[node_count++]=(pairs[0]);
                if(!(Check(nodes,pairs[pair_count-1])))
                	nodes[node_count++]=(pairs[pair_count-1]);
         
                PairInfo start_node = info[i].getNodes()[0];
        		PairInfo end_node = info[i].getNodes()[pair_count-1];
        		
         		int start_index = GetIndex(start_node);
        		int end_index = GetIndex(end_node);
                writer.write(linkid+","+func_class+","+"n"+start_index+","+"n"+end_index+",");
                writer_txt.write(linkid+","+func_class+","+"n"+start_index+","+"n"+end_index+",");
        		for(int j=0;j<pair_count;j++)
        		{
        			writer.write(info[i].getNodes()[j].getLati()+","+info[i].getNodes()[j].getLongi()+",");
        			writer_txt.write(info[i].getNodes()[j].getLati()+","+info[i].getNodes()[j].getLongi()+",");
        		}
        		writer.write("\n");
        		writer_txt.write("\n");
        		
                //writer1.write("n"+start_index+","+"n"+end_index+","+info[i].getNodes()[pair_count/2].getLati()+","+info[i].getNodes()[pair_count/2].getLongi()+"\n");
             
                
            }
            	System.out.println(node_count);

            	
            	
            	// Nodes_Info
            	/*for(int e=0;e<node_count;e++)
            	{	for(int f=e+1;f<node_count;f++)
            		{
            			if(nodes[f].getLati()<nodes[e].getLati())
            			{
            				PairInfo tmp = nodes[e];
            				nodes[e] = nodes[f];
            				nodes[f] = tmp;
            				
            			}
            			
            		}
            	}*/
            	
            	for(int e=0;e<node_count;e++)
            		writer2.write("n"+e+","+nodes[e].getLati()+","+nodes[e].getLongi()+"\n");
            	
            end = new Date();
            System.out.println("stored in files "+(end.getTime()-start.getTime()));
            connHome.close();
            writer.close();
            //writer1.close();
            writer2.close();

        }catch(Exception ee){
            ee.printStackTrace();
        }
    }

	private static int GetIndex(PairInfo node) {
		for(int i=0;i<node_count;i++)
		{	if(nodes[i].equal(node))
				return (i);
		}
		return -1;
	}

	private static boolean Check(PairInfo[] nodes, PairInfo pairInfo) {
		int i;
		
		for(i=0;i<nodes.length;i++)
		{
			if(nodes[i].equal(pairInfo))
				return true;
		}
		return false;
	}

}
