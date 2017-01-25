package cblicous.utils.essGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javaslang.Function1;
import javaslang.Function2;
import javaslang.Function3;
import javaslang.Tuple2;

public class EssGenerator {

	public class Row {

	}

	/**
	 * The Class CodeRow if it is a code row
	 */
	 public class CodeRow extends Row {
		public final String variableName;
		public final String variableType;
		public final int offset;

		public CodeRow(String variableName, int offset, String variableType) {
			this.variableName = variableName;
			this.offset = offset;
			this.variableType = variableType;
		}

	}

	/**
	 * The Class Comment Row if it is a comment row (starts with /)
	 */
	public class CommentRow extends Row {
		private final String comment;

		public CommentRow(String comment) {
			this.comment = comment;
		}
	}

	private Function1<String, Row> mapToRow = (line) -> {
		if (!line.substring(0, 1).equals("/")) {

			String[] p = line.split(",");
			String varName = p[0];
			varName = varName.replace("ä", "ae");
			varName = varName.replace("ö", "oe");
			varName = varName.replace("ü", "ue");
			varName = varName.replace("ß", "ss");
			varName = varName.replaceAll("[^a-zA-Z0-9]", "");
			varName = varName.replaceAll(" ", "");
			varName = varName.toLowerCase();
			return new CodeRow(varName, Integer.parseInt(p[1]), p[2]);
		} else {
			return new CommentRow(line);
		}
	};

	private Function1<Row, String> generateVariables = (row) -> {
		String result;
		if (row instanceof CodeRow) {
			result = "private " + ((CodeRow) row).variableType + " " + ((CodeRow) row).variableName + "; \n";
		} else {
			result = ((CommentRow) row).comment + " \n";
		}
		return result;
	};

	protected String generateCode(javaslang.collection.List<Row> rowList, Integer i, String result) {
		if (rowList.size() >= 1) {
			Row row = rowList.head();

			if (row instanceof CodeRow) {
				String renderedCode;
				String variableNameUpperCase = ((CodeRow) row).variableName;
				variableNameUpperCase = Character.toUpperCase(variableNameUpperCase.charAt(0))
						+ variableNameUpperCase.substring(1);
				renderedCode = " @Field(offset = " + i + ", length = " + ((CodeRow) row).offset + ") \n" + "public "
						+ ((CodeRow) row).variableType + " get" + variableNameUpperCase + "(){\n" + "return "
						+ ((CodeRow) row).variableName + "; \n" + "} \n";
				i = i + ((CodeRow) row).offset;
				String concenatedResult = result + renderedCode;
				return generateCode(rowList.pop(),i,concenatedResult);
			} else {
				return generateCode(rowList.pop(),i,result);
			}
		} else {
			return result;
		}
	};

	/**
	 * takes in a sourcefile "name","length","type" and generated a pojo out of
	 * it
	 * 
	 * @param sourceFile
	 * @param destinationFile
	 */
	public void generateFile(String sourceFile, String destinationFile) {
		InputStream is;
		try {
			is = new FileInputStream(new File(sourceFile));
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			ArrayList<Row> rows = new ArrayList<Row>(br.lines().map(mapToRow).collect(Collectors.toList()));

			StringBuffer resultFileStringBuffer = new StringBuffer();
			// write the variables
			resultFileStringBuffer.append(rows.stream().map(generateVariables).collect(Collectors.joining()));
			// first tryout , lets improve
			
			javaslang.collection.List<Row>list = javaslang.collection.List.ofAll(rows);
			resultFileStringBuffer.append(generateCode(list,0,""));
	
			Files.write(Paths.get(destinationFile), resultFileStringBuffer.toString().getBytes());
			System.out.println("Wrote " + rows.size() + " Fields to " + destinationFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
