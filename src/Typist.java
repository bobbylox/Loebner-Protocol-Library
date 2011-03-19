import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Arrays;
import javax.swing.*;

public class Typist {
	
	//ASSUMPTIONS
	//More commonly used words will be typed faster SOURCE: BNC
	//Letters which commonly occur together will be typed faster SOURCE: Complete Shakespeare
	//Commonly misspelled words have a high probability of being misspelled SOURCE: Wikipedia
	//There is also a possibility that misspelled words will be corrected
	
	HashMap<String, String> lchars = new HashMap<String, String>(100);
	HashMap<String, String> charsl = new HashMap<String, String>(100);
	
	HashMap<String, Integer> wordFreqMap = new HashMap<String, Integer>();
	HashMap<String, Integer> biFreqMap = new HashMap<String, Integer>();
	HashMap<String,String[]> misspellMap = new HashMap<String,String[]>();
	
	public final String TERMINAL = "terminal";
	public final String DIRECTORY = "directory";
	
	public double FACTOR = 70.0;
	public int FACTOR2 = 100;
	
	File dir;
	File freq;
	File mspell;
	File bigram;
	
	File target;
	
	String io;
	int charNum=1;
	
	//Acceptable outputtypes are terminal or directory
	
	/* ARRAYS FOR SPECIAL CHARACTERS */
	
	String[] real = {"{", "}", "[", "]", "(", ")", " ", ",", ".", ">", "<", "/", "\\",
			"\"", "'", "\t", "=", "_", "+", "-", "!", "@", "#", "$", "%", "*", 
			"^", "~", "`", "&", "\n", ":", ";","?", "\b", "1", "2", "3", "4",
			"5", "6", "7", "8", "9", "0", "q", "w", "e", "r", "t", "y", "u", "i",
			"o", "p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x", "c",
			"v", "b", "n", "m", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
			"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
			"Y", "Z"};
	
	String[] lpp = {"braceleft", "braceright", "bracketleft", "bracketright",
			"parenleft", "parenright", "space", "comma", "period", "greater",
			"less", "slash", "backslash", "quotedble", "quoteright", "Tab",
			"equal", "underscore", "plus", "minus", "exclam", "at", "numbersign",
			"dollar", "percent", "asterisk", "asciicircum", "asciitilde",
			"quoteleft", "ampersand", "Return", "colon", "semicolon","question",
			"BackSpace", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "q",
			"w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d", "f", "g",
			"h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m", "A", "B", "C",
			"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
			"R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
	
	public Typist(String outputtype) throws Exception{
		
		//INSERT SPECIAL CHARACTERS INTO HASHMAPS IN BOTH DIRECTIONS
		
		for(int i = 0;i<real.length;i++){
			lchars.put(real[i], lpp[i]); //lchars: real -> lpp codes
			charsl.put(lpp[i], real[i]); //charsl: lpp codes -> real chars
		}
		
		//TERMINAL IS MOSTLY FOR DEBUGGING
		//DIRECTORY IS THE OFFICIAL LPP
		
		if(outputtype.equals(TERMINAL)||outputtype.equals(DIRECTORY)){
			io = outputtype;
		}
		
		else{
			throw new Exception("allowed output types are \"terminal\" and \"directory\"");
		}
		
		//INITIALIZE FILE OBJECTS FOR THIS DIRECTORY, 
		// WORD FREQUENCY FILE, MISSPELLINGS FILE, BIGRAM FREQUENCY FILE
		
		try{
			dir = new File(System.getProperty("user.dir"));
			freq = new File(dir,"frequencies.txt");
			mspell = new File(dir,"misspellings.txt");
			bigram = new File(dir,"bigrams.txt");
		}
		catch(Exception e){
			System.exit(1);
		}
		
		//Put word frequencies in a HashMap (wordFreqMap)
		Scanner scanFile = null;

		String res="";
		try {
			scanFile = new Scanner(freq);
			res = scanFile.nextLine();
			while(!res.equals("Null")){
				Scanner scanline = new Scanner(res);
				wordFreqMap.put(scanline.next(), scanline.nextInt());
				res = scanFile.nextLine();
			}
		} 
		catch(FileNotFoundException e){
			System.out.println("File Not Found");
		}
		//Put bigram frequencies in a HashMap (biFreqMap)
		
		String bg = "";
		int bgf = 1;
		
		try {
			scanFile = new Scanner(bigram);
			res = scanFile.nextLine();
			while(!res.equals("Null")){
				Scanner scanline = new Scanner(res);
				if(scanline.hasNext("[^\t]{2}")){
					bg = scanline.next("[^ \t]{2}");
				}
				if(scanline.hasNextInt()){
					bgf = scanline.nextInt();
				}
				else{
					bgf = 1;
				}
				biFreqMap.put(bg, bgf);
				res = scanFile.nextLine();
			}
		} 
		catch(FileNotFoundException e){
			System.out.println("File Not Found");
		}
		//put misspellings in a HashMap (misspellMap)
		try {
			scanFile = new Scanner(mspell);
			res = scanFile.nextLine();
			while(!res.equals("Null")){
				Scanner scanline = new Scanner(res);
				misspellMap.put(scanline.next(), res.split("\t"));
				res = scanFile.nextLine();
			}
		} 
		catch(FileNotFoundException e){
			System.out.println("File Not Found");
		}
		
		//USER SELECTS DIRECTORY FOR COMMUNICATIONS-DIRECTORY (target)
		
		if(io.equals(DIRECTORY)){
			
			final JFileChooser fc = new JFileChooser(dir); 
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	
			int returnVal = fc.showOpenDialog(null);
			
			if(returnVal == JFileChooser.APPROVE_OPTION){
				target = fc.getSelectedFile();
			}
		}

	}
	
	public void sendMessage(String message){
		
		//DEFAULT ACCURACY IS .7
		
		sendMessage(message,0.7);
		
	}
	
	public void sendMessage(String message, double proficiency){
		
		FACTOR = proficiency*80;
		FACTOR2 = (int)(proficiency*100);

		String[] words = message.split("[^a-zA-Z\\']+");//all the words in the message
		String[] interwords = message.split("[a-zA-Z\\']+");//stuff between the words
		
		/*for(int i= 0; i<words.length;i++){
			System.out.println(words[i]);
		}*/
		//set the word frequencies
		double[] wordFreqs = new double[words.length];
		
		for(int i = 0;i<words.length;i++){
			wordFreqs[i] = wordFreq(words[i]);
		}
		
		//get possible misspellings
		String[] mspell = {};
		
		for(int i = 0; i<words.length;i++){
			//all possible misspellings
			mspell = misspell(words[i]);
			//randomly select whether to misspell
			double rand = Math.random();
			//if it is to be misspelled...
			if(!(rand<proficiency)&&mspell.length>1){
				
				//choose which way to misspell it
				double cutoff = (1.0-proficiency)/(mspell.length-1);
				String werd = mspell[(int)Math.ceil((rand-proficiency)/cutoff)];
				
				//if this second random number is above the proficiency threshold
				//the typist corrects itself
				double secondrand = Math.random();
				
				if(secondrand>proficiency){
					int commonchars = commonPrefix(words[i],werd);
					
					int n = (werd.length()-commonchars);
					
					for(int m = 0;m<n;m++){

						werd = werd+"\b";

					}
					werd = werd + words[i].substring(commonchars);
				}
				words[i]=werd;
			}

		}

		LinkedList<String> finalchars = new LinkedList<String>();
		LinkedList<Integer> finalpauses = new LinkedList<Integer>();
		
		if(words.length>=interwords.length){
			
			for(int i = 0; i<words.length;i++){
				
				for(int j = 0; j<words[i].length();j++){
					
					finalchars.add(words[i].substring(j,j+1));
					
					if(j==0){
						finalpauses.add((int)Math.ceil(wordFreqs[i]*FACTOR+FACTOR2));
					}
					else{
						finalpauses.add((int)(Math.ceil(FACTOR*(wordFreqs[i]+bigramFreq(words[i].substring(j-1, j+1)))/2))+FACTOR2);
					}
				}
				if(i<interwords.length){
					
					for(int j = 0;j<interwords[i].length();j++){
					
						finalchars.add(interwords[i].substring(j,j+1));
						
						if(j==0){
							finalpauses.add((int)FACTOR+FACTOR2);
						}
						else{
							finalpauses.add((int)Math.ceil(FACTOR*(bigramFreq(interwords[i].substring(j-1, j+1))))+FACTOR2);
						}
					}
				}
			}
		}
		//This is in case the message begins with punctuation - essentially it's same as above
		else{
			
			for(int i = 0; i<interwords.length;i++){
				
				for(int j = 0;j<interwords[i].length();j++){
					
						finalchars.add(interwords[i].substring(j,j+1));
						
						if(j==0){
							finalpauses.add((int)FACTOR+FACTOR2);
						}
						else{
							finalpauses.add((int)Math.ceil(FACTOR*(bigramFreq(interwords[i].substring(j-1, j+1))))+FACTOR2);
						}
					
				}
				if(i<words.length){				
					
					for(int j = 0; j<words[i].length();j++){
					
						finalchars.add(words[i].substring(j,j+1));
						
						if(j==0){
							finalpauses.add((int)Math.ceil(wordFreqs[i]*FACTOR)+FACTOR2);
						}
						else{
							finalpauses.add((int)(Math.ceil(FACTOR*(wordFreqs[i]+bigramFreq(words[i].substring(j-1, j+1)))/2))+FACTOR2);
						}
					}
				}
				
			}
		}
		
		while(!(finalchars.isEmpty())){
			try{
				waitandsend(finalpauses.removeFirst(),finalchars.removeFirst());
			}
			catch(Exception e){
				System.out.println(e);
			}
		}
	}
	
	private int commonPrefix(String a, String b){
		int i = 0;
		
		while(a.charAt(i)==b.charAt(i)){
			i++;
		}
		return i;
	}
	
	private String[] misspell(String word){
		
		String[] mspl = {};
		
		String tword = word.replaceAll("[^a-zA-Z\\']","");
		
		if(misspellMap.containsKey(tword)){
			mspl = misspellMap.get(tword);
		}
		
		return mspl;
	}
	
	private double wordFreq(String word){
		
		String tword = word.replaceAll("[^a-zA-Z\\']","").toLowerCase();
		int fqc = 1;
		
		if(wordFreqMap.containsKey(tword)){
				fqc = wordFreqMap.get(tword);
		}
		
		double f = 1/(double)fqc;
		return f;
	}
	
	private double bigramFreq(String biGram){
		
		int bfqc = 1;
		double f = 1.0;
		
		if(biGram.length()!=2){
		}
		else{
			
			if(biFreqMap.containsKey(biGram)){
				bfqc = biFreqMap.get(biGram);
				f = 1/(double)bfqc;
			}
		}
		
		return f;
	}
	
/*	private String row(File file, String key){
		Scanner scanFile = null;
		String res="";
		try {
			scanFile = new Scanner(file);
			while(!(res.startsWith(key+" ")||res.startsWith(key+"\t"))&&scanFile.hasNextLine()){
				res = scanFile.nextLine();
			}
		} 
		catch(FileNotFoundException e){
			System.out.println("File Not Found");
		}
		
		if(res.equals("Null")){
			res = "";
		}
		return res;
		
	}*///DEPRECATED
	
	public void sendChar(String character) throws Exception{
		
		if(!lchars.containsKey(character)){
			throw new Exception("unsupported character");
		}
		
		if(io.equals(TERMINAL)){
			//print using the command line
			System.out.print(character/*.replace("\b", "\b \b")*/);
		}
		else if(io.equals(DIRECTORY)){
			//create a directory with the name specified by LPP
			//SOURCE: http://www.loebner.net/Prizef/2010_Contest/Loebner_Prize_Rules_2010.html
			String filename = "";
			Integer num = charNum;
			
			for(int i = 1; i<=10-num.toString().length(); i++){
				filename = filename + "0";
			}
			
			filename = filename + num.toString() + "." + lchars.get(character) + ".other";
			
			(new File(target,filename)).mkdir();
			
			charNum++;
		}
		
	}
	
	public String getMessage(){
		
		String judge="";
		
		if(io.equals(TERMINAL)){
			Scanner judgeInput= new Scanner(System.in);
			while(judgeInput.hasNextLine()){
				judge = judge+"\n"+judgeInput.nextLine();
			}
			return judge;
		}
		else if(io.equals(DIRECTORY)){
			//get all filenames ending in .judge
			String[] allnames = target.list(new JudgeFilter());
			
			String tempchr = "";
			
			//then delete all these directories
			for(int i = 0;i<allnames.length;i++){
				new File(target,allnames[i]).delete();
			}
			//hopefully these will already be in sorted order, but if not...
			Arrays.sort(allnames);
			
			for(int i = 0;i<allnames.length;i++){
				tempchr = allnames[i].substring(allnames[i].indexOf(".")+1,allnames[i].lastIndexOf("."));
				
				if(charsl.containsKey(tempchr)){
					judge = judge + charsl.get(tempchr);
				}
				
			}
			
			return judge;
		}
		else{
			return "";
		}
		
	}
	
	public static void wait(int n){
        
        long t0, t1;

        t0 =  System.currentTimeMillis();

        do{
            t1 = System.currentTimeMillis();
        }
        while ((t1 - t0) < (n));
    }
	
	private void waitandsend(int millis, String character) throws Exception{
		
		wait(millis);
		sendChar(character);
	
	}
	
	public void finish(){
		System.exit(0);
	}

}
