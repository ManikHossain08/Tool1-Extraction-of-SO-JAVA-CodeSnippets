package concordia.org.research.stack.overflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class CreateAllJavaFileForSOCode {

	static final String MacOSXPathToWrite = "/Users/manikhossain/EclipseCreatedSOJavaFile/";
	static final String MacOSXPathToReadFile = "/Users/manikhossain/EclipseCreatedSOJavaFile/QueryResults-7.csv";
	static String[] codeTokens = { "class", "package", "public static", "private static", "public class",
			"private boolean", "public void", "import", "public interface", "abstract class" };
	static String[] codeReverseTokens = { "String.class" };

	static String MacOSXFullPathToWrite = "";
	static String CombineAllCode = "";
	static boolean isStartwithSlash = true;
	static String postIDAsFileName = "";
	
//	static ASTParser parser = null;
//	static String[] encodings = { "UTF-8" };
//	static String rtJAR = "";

	

	public static void main(String[] args) throws IOException {

		String filename = MacOSXPathToReadFile;
		File file = new File(filename);
		try {
			Scanner sc = new Scanner(file);
			while (sc.hasNext()) {
				String data = sc.nextLine();
				data = filterData(data);
				if (!data.toLowerCase().contentEquals("body")) {
					if (data.startsWith("\"") && !data.contentEquals("\"")) { // indicate starting of each row value
						data = data.substring(1); // remove first char bcz at the beginning " this char comes
						String[] postID = data.split("<postid>");
						postIDAsFileName = postID[0];
						// if (!isStartwithSlash)
						CombineAllCode = CombineAllCode + postID[1]; // combines all codes line by line until finished
					} else if (data.contentEquals("\"")) {
						// indicate last line of each row value
						extractHtmlCodeTags(postIDAsFileName, CombineAllCode);
						CombineAllCode = "";
					} else {
						CombineAllCode = CombineAllCode + "\n";
						CombineAllCode = CombineAllCode + data;
					}
				}

			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	// replace garbage code from SO database
	private static String filterData(String data) {
		data = data.replace("\"\"", "\"");
		data = data.replace("&lt;", "<");
		data = data.replace("&gt;", ">");
		data = data.replace("...", "");
		data = data.replace("&amp;&amp;", "&&");
		// isStartwithSlash = data.startsWith("//");
		return data;
	}

	// extract only code portion from whole HTML code using split function.
	private static void extractHtmlCodeTags(String fileName, String stringToSearch) {
		int counter = 1;
		String[] splitCOde = stringToSearch.split("<code>");
		for (String partByPartCode : splitCOde) {
			String[] departedCode = partByPartCode.split("</code>");
			if (departedCode.length > 1) {
				String contentOfFile = codeWrangglingFunctionORBlocks(departedCode[0]);
				int noOfLines = countLines(contentOfFile);
				int noOfWords = countWordsUsingStringTokenizer(contentOfFile);
				try {
					if (noOfWords > 3 && noOfLines > 5) {
						createJavaFileUsingFileClass(fileName + "_" + Integer.toString(counter), contentOfFile);
						counter += 1;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	// Adding {} for blocks of code because without brackets parser could not parse
	// the file
	private static String codeWrangglingFunctionORBlocks(String data) {
		boolean iscontain = false;

		data = removeUnbalancedCurlyBrackets(data);

		for (String matchingToken : codeTokens) {
			if (data.toString().contains(matchingToken)) {
				iscontain = true;
			}
		}
		for (String matchingReverseToken : codeReverseTokens) {
			if (data.toString().contains(matchingReverseToken)) {
				if (iscontain)
					iscontain = false;
			}
		}

		if (!iscontain) {
			{
				data = "{" + "\n" + data + "\n" + "}";
				iscontain = false;
			}
		}

		return data;
	}

	private static String removeUnbalancedCurlyBrackets(String fileContent) {
		fileContent = fileContent.trim();
		long count = fileContent.chars().filter(ch -> ch == '{').count();
		long count2 = fileContent.codePoints().filter(ch -> ch == '}').count();
		if (count != count2) {
			if (fileContent != null && fileContent.length() > 0
					&& fileContent.charAt(fileContent.length() - 1) == '}') {
				fileContent = fileContent.substring(0, fileContent.length() - 1);
			}
		}
		return fileContent;
	}

	private static void createJavaFileUsingFileClass(String fileName, String allCode) throws IOException {
		MacOSXFullPathToWrite = MacOSXPathToWrite + fileName + ".java";
		File file = new File(MacOSXFullPathToWrite);

		// Create the file
		if (file.createNewFile()) {
			// System.out.println("File is created!");
		} else {
			// if file already exist then delete first and create the file again.
			file.delete();
			file.createNewFile();
		}
		// Write Content
		FileWriter writer = new FileWriter(file);
		writer.write(allCode);
		writer.close();
	}

	private static int countLines(String str) {
		String[] lines = str.split("\r\n|\r|\n");
		return lines.length;
	}

	private static int countWordsUsingStringTokenizer(String sentence) {
		if (sentence == null || sentence.isEmpty()) {
			return 0;
		}
		StringTokenizer tokens = new StringTokenizer(sentence);
		return tokens.countTokens();
	}
	/*
	 * Check the is parsable by parser or not
	private static boolean istheExtractedCodeParsable(String macOSXFullPathToWrite2) throws IOException {

		File projectDir = new File("/Users/manikhossain/EclipseCreatedSOJavaFile/Test.java"); // macOSXFullPathToWrite2

		parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		String encoding = null;
		parser.setSource(FileUtils.readFileToString(projectDir, encoding).toCharArray());
		String[] sources = { projectDir.getParent() + "/" };
		String[] classpath = { ".", rtJAR };
		parser.setEnvironment(classpath, sources, encodings, true);
		parser.setUnitName(projectDir.getName());

		// the 3 below need to be set each time for a new file otherwise if you had an
		// error it will never recover from it!
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);

		boolean jdtParserOK = false;
		org.eclipse.jdt.core.dom.CompilationUnit cuJDT = null;
		try {
			cuJDT = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);
			if (cuJDT != null) {
				jdtParserOK = true;
				System.out.println("succesfully Parsed the file:" + projectDir.getName());
			}
		} catch (Exception e) {
			System.out.println("JDT parser error on file:" + projectDir.getName());
			jdtParserOK = false;
		}

		return jdtParserOK;
	}*/

}