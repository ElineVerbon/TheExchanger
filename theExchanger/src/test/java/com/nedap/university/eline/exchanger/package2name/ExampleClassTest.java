package com.nedap.university.eline.exchanger.package2name;

import com.nedap.university.eline.exchanger.package2name.ExampleClass;

public class ExampleClassTest {
	
	//@Test
	public void testExample() {
		//can access the (non-public!!) ExampleClass, because in same package
		ExampleClass exampleClass = new ExampleClass();
	}
}
