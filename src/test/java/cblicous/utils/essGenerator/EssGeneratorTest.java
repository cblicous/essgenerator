package cblicous.utils.essGenerator;

import static org.junit.Assert.*;

import org.junit.Test;

import cblicous.utils.essGenerator.EssGenerator.Row;
import javaslang.collection.List;

public class EssGeneratorTest {

	@Test
	public void testGenerateCode() {
		EssGenerator generator = new EssGenerator();
		List<Row> list = List.of(generator.new CodeRow("test", 2, "String"),generator.new CodeRow("test", 10, "String"));
		String result = generator.generateCode(list, 0, "");
		// we could do better here
		assertTrue(result.length() == 151);
		
	}

}
