import java.io.File;
import java.io.IOException;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Main {

	public static void main(String[] args) throws BiffException, IOException, RowsExceededException, WriteException{
		String inPath = "data/in";
		String outPath = "data/out";
		String prefix_suffix = "data/prefix_suffix";
		listFiles(inPath);
		new Samurai();
		Samurai.run(inPath, outPath, prefix_suffix);
		System.out.println();
		System.out.println("Done.");
	}
	private static void listFiles(String inPath){
		File folder = new File(inPath);
		File[] listOfFiles = folder.listFiles();
		System.out.println("The projects under analysis are:");
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println(i+1+" - "+listOfFiles[i].getName());
			}
		}
		System.out.println();
	}
}
