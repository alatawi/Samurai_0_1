import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class Samurai {
	public static FrequencyTable freqGlobal = new FrequencyTable();
	public static void run(String inPath, String outPath, String prefix_suffix) throws BiffException, IOException, RowsExceededException, WriteException{
		buildGlobalFrequency(inPath);
		cleanDirectory(outPath); 
		File folder = new File(inPath);
		File[] listOfFiles = folder.listFiles();
		System.out.println("## Building local frequencies...");
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				//Start loop for each file.
				
				
				//1: Building local frequency.
				FrequencyTable freqLocal = new FrequencyTable();
				//System.out.print(i+1+" - "+listOfFiles[i].getName());
				Sheet sheet = readFromEXL(inPath + "/" +listOfFiles[i].getName());
				Cell E;
				for(int j = 0; j < sheet.getRows(); j++) {
					E = sheet.getCell(4, j);
					String[] strArray = conservativeSplitting(E.getContents());
					for (int k = 1; k < strArray.length; k++) {// Start from 1 to avoid
																// recording space
						freqLocal.click(strArray[k].toLowerCase());
					}
				}
				//2: Creating output file to store spited tokens.
				String outputFile = outPath+"/Out-"+listOfFiles[i].getName();
				WritableWorkbook workbook = Workbook.createWorkbook(new File(outputFile));
				WritableSheet newSheet = workbook.createSheet("Splited Tokens", 0);
				Label label1 = new Label(0, 0, "Before"); 
				newSheet.addCell(label1); 
				Label label2 = new Label(1, 0, "After"); 
				newSheet.addCell(label2);

				
				//2: Calling mixedCaseSplit algorithm.
				for(int j = 0; j < sheet.getRows(); j++) {
					E = sheet.getCell(4, j);
					String actualToken = E.getContents();
					String splitedToken = mixedCaseSplit(actualToken, freqLocal, prefix_suffix);
					Label label3 = new Label(0, j+1, actualToken); 
					newSheet.addCell(label3);
					Label label4 = new Label(1, j+1, splitedToken); 
					newSheet.addCell(label4);
				}
				workbook.write();
				workbook.close();
				
				
				//End loop for each file.
			}
		}
			
	}
	private static Sheet readFromEXL(String filename) throws BiffException, IOException{
		Workbook workbook = Workbook.getWorkbook(new File(filename));
		Sheet sheet = workbook.getSheet(0);
		//System.out.println(", # of tokens: "+sheet.getRows());
		return sheet;
	}
	private static void buildGlobalFrequency(String inPath) throws BiffException, IOException{
		File folder = new File(inPath);
		File[] listOfFiles = folder.listFiles();
		System.out.println("## Building the Global Frequency...");
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				//System.out.print(i+1+" - "+listOfFiles[i].getName());
				Sheet sheet = readFromEXL(inPath + "/" +listOfFiles[i].getName());
				Cell E;
				for(int j = 0; j < sheet.getRows(); j++) {
					E = sheet.getCell(4, j);
					String[] strArray = conservativeSplitting(E.getContents());
					for (int k = 1; k < strArray.length; k++) {// Start from 1 to avoid
																// recording space
						freqGlobal.click(strArray[k].toLowerCase());
					}
				}
			}
		}
	}
	private static String[] conservativeSplitting(String str) {
		str = splitOnSpecialCharsAndDigits(str);
		str = splitOnLowercaseToUppercase(str);
		return str.split("\\s+");// remove space
	}

	private static String splitOnSpecialCharsAndDigits(String token) {
		String splittedToken = "";
		String[] identifierList = token.split("\\s*[^a-zA-Z]+\\s*");
		for (int i = 0; i < identifierList.length; i++)
			splittedToken += identifierList[i] + " ";
		return splittedToken;
	}

	private static String splitOnLowercaseToUppercase(String token) {
		String splittedToken = "";
		String[] identifierList = token.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		for (int i = 0; i < identifierList.length; i++)
			splittedToken += identifierList[i] + " ";
		return splittedToken;
	}
	

	private static String mixedCaseSplit(String token, FrequencyTable freqLocal, String prefix_suffix) throws FileNotFoundException {
		token = splitOnSpecialCharsAndDigits(token);
		token = splitOnLowercaseToUppercase(token);
		String sToken = "";
		String[] s = token.split("\\s+");
		double altScore=0, camelScore=0;
		int n=0;
		//for all space-delimited substrings s in token do
		for (int i = 0; i < s.length; i++){
			for(int j = 0; j < s[i].length(); j++){
				//if any: isUpper(s[i]) ^ isLower(s[i + 1]) then
				if(Character.isUpperCase(s[i].charAt(j)) && Character.isUpperCase(s[j+1].charAt(0))){
					n = s[i].length()-1;
				
				// compute score for camelcase split
				if(j>0)
					camelScore = score(s[i].substring(j, n), freqLocal);
				else 
					camelScore = score(s[i].substring(0, n), freqLocal);
				
				// compute score for alternate split
				//altScore   score(s[i + 1; n])
				altScore = score(s[i].substring((j+1), n), freqLocal);
				
				// select split based on score
				if(camelScore > java.lang.Math.sqrt(altScore)){
					if(j>0)
						s[i] = s[i].substring(0, j-1)+" "+ s[i].substring(j, n);
				}else
					s[i] = s[i].substring(0, j)+" "+ s[i].substring(j+1, n);
				}
				
			}//end if
			sToken += " " + s[i];
			
		}//end for
	
		token = sToken;
		s = token.split("\\s+");
		sToken = "";
		// for all space-delimited substrings s in token do
		for (int i = 0; i < s.length; i++){
			sToken += "" + sameCaseSplit(s[i], freqLocal, score(s[i], freqLocal), prefix_suffix);
		}
		return sToken;
	}
	
	private static String sameCaseSplit(String s, FrequencyTable freqLocal, double score_ns, String prefix_suffix) throws FileNotFoundException {
		String splitS = s;
		int n = s.length(), i=0;
		double maxScore = -1, score_l, score_r;
		boolean prefix, toSplit_l, toSplit_r;
		while(i<n){
			score_l = score(s.substring(0,i), freqLocal);
			score_r = score(s.substring(i+1,n), freqLocal);
			prefix = isPrefix(s.substring(0,i), prefix_suffix) || isSuffix(s.substring(i+1,n), prefix_suffix);
			toSplit_l = java.lang.Math.sqrt(score_l) > java.lang.Math.max(score(s, freqLocal),score_ns);
			toSplit_r = java.lang.Math.sqrt(score_r) > java.lang.Math.max(score(s, freqLocal),score_ns);
			if(!prefix && toSplit_l && toSplit_r){
				if((score_l+score_r) > maxScore){
					maxScore = (score_l+score_r);
					splitS = s.substring(0,i) + " " + s.substring(i+1,n);
				}
			} else if(!prefix && toSplit_l){
				String temp = sameCaseSplit(s.substring(i+1,n), freqLocal, score(s.substring(i+1,n), freqLocal), prefix_suffix);
				if(s.substring(i+1,n) != temp)
					splitS = s.substring(0,i) + " " + temp;
			}
			i++;
		}
		return splitS;
	}
	private static double score(String s, FrequencyTable freqLocal){
		//Freq(s; p) + (globalFreq(s)/ log10(AllStrsFreq(p))
		return freqLocal.count(s) + (freqGlobal.count(s)/java.lang.Math.log10((double)freqLocal.AllStrsFreq));
	}

	@SuppressWarnings("resource")
	private static boolean isPrefix(String s, String prefix_suffix) throws FileNotFoundException{
		String content = new Scanner(new File(prefix_suffix+"/prefixes.txt")).useDelimiter(" ").next();
		String[] contentArray = content.split("\\s+");
		for (int i = 0; i < contentArray.length; i++)
			if(contentArray[i].equals(s))
				return true;
		return false;
	}
	@SuppressWarnings("resource")
	private static boolean isSuffix(String s, String prefix_suffix) throws FileNotFoundException{
		String content = new Scanner(new File(prefix_suffix+"/suffixes.txt")).useDelimiter(" ").next();
		String[] contentArray = content.split("\\s+");
		for (int i = 0; i < contentArray.length; i++)
			if(contentArray[i].equals(s))
				return true;
		return false;
	}

	private static void cleanDirectory(String outPath) {
		File dir = new File(outPath);
	    for (File file: dir.listFiles()) {
	        file.delete();
	    }
	}
}
