
public class Sample {

	public static void main(String[] args) {
		
		Typist prog = null;
		try{
			prog = new Typist("terminal");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		String printme="Hello there, Rob.  How are you this afternoon?";
		
		prog.sendMessage(printme);
		
		/*try{
			prog.sendChar("h");
			prog.sendChar("i");
		}
		catch(Exception e){
			e.printStackTrace();
		}*/
		
		//System.out.print("\""+prog.getMessage()+"\"");
		
		//prog.sendMessage("hello there, what are you up to this fine afternoon?");
		System.exit(0);
	}

}
