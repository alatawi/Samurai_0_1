import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;

public class FrequencyTable {
	public int AllStrsFreq = 0;
	private ST<String, Integer> st = new ST<String, Integer>();

	public void click(String key) {
		int count = count(key);
		st.put(key, count + 1);
		//System.out.println("Click Key: "+key);
		AllStrsFreq += count + 1;
	}

	// return the number of times the key appears
	public int count(String key) {
		if (!st.contains(key))
			return 0;
		else
			return st.get(key);
	}

	// print table to standard output
	public void show() {
		String format = "%-30s %-20s\n";
		System.out.format(format, "Key", "Count");
		System.out.println("--------------------------------------");
		for (String key : st) {
			System.out.format(format, key, count(key));
		}
	}

	public void createOutput(String LocalORGlobal, String filename, String path)
			throws IOException, RowsExceededException, WriteException, BiffException {
		WritableWorkbook workbook = Workbook
				.createWorkbook(new File(path+"/"+LocalORGlobal+"_" + filename));
		WritableSheet freqSheet = workbook.createSheet("freq_data", 0);
		Label label;
		Number number;
		int i = 0;
		for (String key : st) {
			//System.out.println("Key: "+key);
			label = new Label(0, i, key);
			number = new Number(1, i, count(key));
			freqSheet.addCell(label);
			freqSheet.addCell(number);
			i++;
		}
		workbook.write();
		workbook.close();
	}
	
	public void updateGlobal(String filename, String path, FrequencyTable freqLocal)
			throws IOException, RowsExceededException, WriteException, BiffException {
		WritableWorkbook workbook = Workbook
				.createWorkbook(new File(path+"/freqLocal_" + filename));
		WritableSheet freqSheet = workbook.createSheet("freq_data", 0);
		Label label;
		Number number;
		int i = 0;
		for (String key : st) {
			label = new Label(0, i, key);
			number = new Number(1, i, count(key));
			freqSheet.addCell(label);
			freqSheet.addCell(number);
			i++;
		}
		workbook.write();
		workbook.close();
	}
	
	
}
