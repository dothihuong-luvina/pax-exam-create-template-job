package test.groovy.common;
import java.io.File;
public class TestUTCommon {
	/**
	 * Function to remove file exist for test
	 * @param file_path path to file need to remove
	 */
	public boolean cleanData(String file_path){
		try{
			File file = new File(file_path);
			if(file.delete()){
				System.out.println(file.getName() + " is deleted!");
			}else{
				System.out.println("Delete operation is failed.");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Function to create job for test.
	 * @param inputFile path to get data for test.
	 * @param outputFile path to write job file for test
	 */
	public boolean createJobTest(inputFile, outputFile){
		def inputStr = new File(inputFile).text;
		FileWriter fw = new FileWriter(outputFile)
		fw.write(inputStr);
		fw.close();
	}
	
	/**
	 * Function to run command for create job.
	 * @param strCmd command for create job
	 */
	public String runCmd(strCmd) {
		def proc = strCmd.execute()
		proc.waitFor()
		return proc.text
	}
	
	/**
	 * run command with ProcessBuider
	 * @param listCmd list command to run
	 * @param dir directory of project
	 * @param waitFor
	 * @return message when run command
	 */
	public static String runProcClosure(listCmd,dir,waitFor){
		def output = [:]
		ProcessBuilder builder = new ProcessBuilder(listCmd);
		builder.redirectErrorStream(true);
		builder.directory(dir);
		Process p = builder.start();
		if(waitFor){
			output['exitVal'] = p.waitFor()
		}
		InputStream procOut  = p.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(procOut))
		def line = null
		StringBuffer stdin = new StringBuffer()
		while((line = br.readLine()) != null){
			stdin.append(line + "\n")
		}
		output["message"] = stdin.toString()
		return output["message"]
	}
}