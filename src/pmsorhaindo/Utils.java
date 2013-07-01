package pmsorhaindo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class Utils {

	//to be private
	
	private String versionFilePath = "";
	//final static Charset ENCODING = "UTF-8"; //StandardCharsets.UTF_8;
	final String DATE_FORMAT_NOW = "dd/MM/yyyy";
	public String lineInfo[] = null;
	public String versionNo = null;
	public String recip = null;
	final String VERSIONTOKEN = "VERSION: ";
	final String VERSIONFILENAME = "VERSION.txt";
	public int svnRevision =  0;
	
	/**
	 * 
	 */
	public Utils() {
		
		versionNo = "0.0.0.0";
		recip = "";
		versionFilePath = "C:\\Users\\michael.sorhaindo\\Documents\\workspace\\Vincent";
	}
	
	/**
	 * 
	 * @param args
	 */
	public Utils(String recipient, String workingPath) {
		
		versionFilePath = workingPath;
		recip = recipient;
	}
	
	public String grabVersionFile() throws Exception {
		
		String everything = "";
		BufferedReader br = new BufferedReader(new FileReader(versionFilePath+"\\"+VERSIONFILENAME));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        
	        everything = sb.toString();
	    }
	    finally {
	        br.close();
	    }
	    
	    return everything;
	}
	
	public String[] processVersionFile(String info) {
		
		lineInfo = info.split("\\r?\\n");
		return lineInfo;
	}
	
	public ArrayList<Integer> processVersionLine(String unProcessedVersion) {
		
		unProcessedVersion = unProcessedVersion.substring(VERSIONTOKEN.length());
		String strVersionArr[] = unProcessedVersion.split("\\.");
		ArrayList<Integer> versionNums = new ArrayList<Integer>();
		for (int i = 0; i < strVersionArr.length; i++) {
			versionNums.add(Integer.parseInt(strVersionArr[i]));
		}
		
		return versionNums;
	}
	
	public String getDateLine() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
	
	public String getAGSVNLine() {
		
		return null;
	}
	
	public String processRecipientLine() {
		
		return null;
	}
	
	public void authorNewVersionFile(List<String> vals) {
		
		try {
			writeTextFile(VERSIONFILENAME,vals);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void writeTextFile(String aFileName, List<String> aLines) throws IOException {
		
		String eol = System.getProperty("line.separator");
	    //System.out.println("Writing to file named " + aFileName + ". Encoding: " + "encoding disabled"); //ENCODING
	    Writer out = new OutputStreamWriter(new FileOutputStream(aFileName)); //, ENCODING
	    try {
	    
	    	for (String txt : aLines)
	    	{
	    		out.write(txt);
	    		out.write(eol);
	    	}
	    }
	    finally {
	      out.close();
	    }
		
		/*Path path = Paths.get(aFileName);
	    System.out.println("Path Writing to ... " + path.toAbsolutePath().toString());
	    try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)){
	      for(String line : aLines){
	        writer.write(line);
	        writer.newLine();
	      }
	    }*/
	  }

	public String formatVersion(ArrayList<Integer> newVersionArr) {
		String versionStr = "";
		Iterator<Integer> it = newVersionArr.iterator();
		while(it.hasNext())
		{
		    Integer i = it.next();
		    versionStr += i.toString()+".";
		}
		//remove trailing "." in version number produced by unintelligent while loop.
		versionStr = versionStr.substring(0, versionStr.length() - 1);
		return versionStr;
	}
	
	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
	
	public void copyFile(String src, String dest) {

		try {
			Utils.deleteFile(dest);
			File f1 = new File(src);
			File f2 = new File(dest);
			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(f2, true);
			byte[] buf = new byte[1024];
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			
			in.close();
			out.close();
			
		} catch (Exception ex) {
			System.out.println(ex);
		}
		
	}
	
	public static boolean deleteFile(String fileLoc) {
		boolean success = false;
		try{
    		File file = new File(fileLoc);

    		if(file.delete()){
    			//System.out.println(file.getName() + " is deleted!");
    			success = true;
    		}else{
    			//System.out.println("Delete operation is failed.");
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		return success;
	}

	public static int processCommand(String command) {

		if(command == "BUILD")
		{
			return 0;
		}
		if(command == "QA")
		{
			return 1;
		}
		if(command == "CLIENT")
		{
			return 2;
		}
		if(command == "LIVE")
		{
			return 3;	
		}
		return 0;
	}

}
