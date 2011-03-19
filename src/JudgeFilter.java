import java.io.File;
import java.io.FilenameFilter;

public class JudgeFilter implements FilenameFilter{

	public boolean accept(File f, String fn) {
        if (f.isDirectory()&&fn.matches("[0-9]+\\.[0-9a-zA-Z]+\\.judge")) {
            return true;
        }
        else{
        	return false;
        }
	}

	public String getDescription() {

		return "Just JudgeDirectories";
	}
}
