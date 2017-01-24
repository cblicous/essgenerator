package cblicous.test.essGenerator;



/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	String sourceFile ;
		String destinationFile;
		
		if (args.length == 2) {
			sourceFile = args[0];
			destinationFile = args[1];
		} else {
			sourceFile = "/software/ess_satzart.csv";
			destinationFile = "/software/ess_satzart.java";
		}
		EssGenerator essGenerator = new EssGenerator();
		essGenerator.generateFile(sourceFile,destinationFile);
    }
}
