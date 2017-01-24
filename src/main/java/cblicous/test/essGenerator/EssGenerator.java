package cblicous.test.essGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EssGenerator {

	class Row {

	}

	/**
	 * The Class CodeRow if it is a code row 
	 */
	class CodeRow extends Row {
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
	class CommentRow extends Row {
		private final String comment;

		public CommentRow(String comment) {
			this.comment = comment;
		}
	}

	private Function<String, Row> mapToRow = (line) -> {
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

			List<Row> rows = br.lines().map(mapToRow).collect(Collectors.toList());

			StringBuffer resultFileStringBuffer = new StringBuffer();
			// write the variables
			rows.forEach(row -> {

				String result;
				if (row instanceof CodeRow) {
					result = "private " + ((CodeRow) row).variableType + " " + ((CodeRow) row).variableName + "; \n";
				} else {
					result = ((CommentRow) row).comment + " \n";
					;
				}
				resultFileStringBuffer.append(result);

			});

			
			// Todo , tryout javaslang
			int i = 0;
			for (Row row : rows) {
				if (row instanceof CodeRow) {
					String result;
					String variableNameUpperCase = ((CodeRow) row).variableName;
					variableNameUpperCase = Character.toUpperCase(variableNameUpperCase.charAt(0))
							+ variableNameUpperCase.substring(1);
					result = " @Field(offset = " + i + ", length = " + ((CodeRow) row).offset + ") \n" + "public "
							+ ((CodeRow) row).variableType + " get" + variableNameUpperCase + "(){\n" + "return "
							+ ((CodeRow) row).variableName + "; \n" + "} \n";

					i = i + ((CodeRow) row).offset;
					resultFileStringBuffer.append(result);

				}
			}

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
