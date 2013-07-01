package ant;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class Ant {
	
	public Ant() {
		build();
		
	}
	
	public void build() {

	File buildFile = new File("build.xml");
	Project p = new Project();
	//p.setUserProperty("ant.file", buildFile.getAbsolutePath());
	p.init();
	ProjectHelper helper = ProjectHelper.getProjectHelper();
	p.addReference("ant.projectHelper", helper);
	helper.parse(p, buildFile);
	p.executeTarget("main");
	}
	
}
