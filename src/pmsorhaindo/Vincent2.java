package pmsorhaindo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;

import ant.Ant;
import svn.SVNManager;


// TODO: sort out some checksum comparison of the version file

public class Vincent2 {
	
	/**
	 * @param args
	 */
	private static String command = null;
	private static String recipient = null;
	private static String destinationFilePath = null;
	private static long svnRevision = 0;
	
	private static SVNManager svn = null;
	private static Utils u = null; 
   	
	public static void main(String[] args) {

		try {
			command = args[0];
			recipient = args[1];
			setPath(args[2]);
		}
		catch (ArrayIndexOutOfBoundsException e){
			
			//TODO: remove once launched from command line
			if (command == null) command = "BUILD";
			if (recipient == null) recipient = "INTERNAL";
			if (destinationFilePath == null) destinationFilePath = ""; 
			System.out.println("Not enough parameters supplied arr out of bounds");
		}
		
		init();
		
		editVersionFile(u);
		
		String commitFilePath = commitFile();
		
		moveVersionFile(commitFilePath);
		
		System.out.println("Versioning complete");

		Ant a = new Ant();
		a.build();

		
	}
	
	private static void moveVersionFile(String commitFilePath) {
		String destinationFile = "";
		// Copy new Version file to the build target
		if(destinationFilePath == "")
		{
			destinationFilePath = svn.getMyWorkingCopyPath();
			destinationFile = destinationFilePath + "\\build\\VERSION.txt";
		}
		else
		{
			destinationFile = destinationFilePath + "\\VERSION.txt";
		}
		
		u.copyFile(commitFilePath, destinationFile);
		
	}

	private static String commitFile() {
		
		//Determine the correct File to commit to SVN
		String commitFilePath = svn.getMyWorkingCopyPath();
		commitFilePath = commitFilePath + "\\VERSION.txt";
		File commitFile = new File(commitFilePath);
		
		//Commit new Version file to SVN
		try {
			//System.out.println("New version number = "+a.getNewRevision());
			SVNCommitInfo a = svn.commit(commitFile, false,"Auto update of Build version");
		} catch (SVNException e) {
			System.err.println("Failed to check in " + commitFilePath);
			e.printStackTrace();
		}
		
		return commitFilePath;
	}

	public static void init(){
		
		SVNManager svn = new SVNManager();
		svn.init();
		try {
			svnRevision = svn.checkoutRoot(null);
		} catch (SVNException e1) {
			System.err.println("Unable to grab the project");
			e1.printStackTrace();
		}
		
		u = new Utils(recipient, svn.getMyWorkingCopyPath()); 
	}
	
	public static ArrayList<Integer> incVersionBuild(ArrayList<Integer> parsedVersionArr) {
	
		int newVersion = 0;
		if (parsedVersionArr.size() == 4) {
			int x = Utils.processCommand(command);
			switch (x) {
				case 0:
					newVersion = parsedVersionArr.get(3) + 1;
					parsedVersionArr.set(3, newVersion);
					break;
				case 1:
					newVersion = parsedVersionArr.get(2) + 1;
					parsedVersionArr.set(2, newVersion);
					break;
				case 2:
					newVersion = parsedVersionArr.get(1) + 1;
					parsedVersionArr.set(1, newVersion);
					break;
				case 3:
					newVersion = parsedVersionArr.get(0) + 1;
					parsedVersionArr.set(0, newVersion);
					break;
				default:
					break;
			}
		}
		return parsedVersionArr;
	}

	public static String getPath() {
		return destinationFilePath;
	}

	public static void setPath(String path) {
		Vincent2.destinationFilePath = path;
	}
	
	
	public static void editVersionFile(Utils u) {
		
		String versionDetails = "";
		try {
			versionDetails = u.grabVersionFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Pull apart the contents of Version file assigning each line to the String array.
		String[] procDetails = u.processVersionFile(versionDetails);
		
		// Process the Version number
		ArrayList<Integer> versionNums = u.processVersionLine(procDetails[0]);
		
		// Increment the relevant version number based on the command parameter
		ArrayList<Integer> newVersion = incVersionBuild(versionNums);
		
		//System.out.println("New Version: " + newVersion.toString());
		//System.out.println("New Date: " + u.getDateLine());
		//System.out.println("New Recipient: " + recipient);
		
		// Add new values to be written to the version file to a List
		List<String> vals = new ArrayList<String>();
		vals.add("VERSION: " + u.formatVersion(newVersion));
		vals.add("DATE: " + u.getDateLine());
		// SVN
		Long l = new Long(svnRevision);
		vals.add("AG-SVN: " + l.toString());
		vals.add("RECIPIENT: " + recipient);
		
		//Write File
		u.authorNewVersionFile(vals);
	}
	
}
