package com.omicseq.test;

public class Test5 {

	public static void main(String[] args) {
		Test5 test = new Test5();
		int x = -16, y = 0;
		test.count(x, y);
	}

	private void count(int x, int y) {
		double a = 0.448 + (0.0053*(x+y));
		double b = 0.245 - (0.0039*(x+y));
		double c = 1-a-b;
		
		double A = 0.9/a;
		double B = 0.9/b;
		double C = 0.9/c;
		
		System.out.println(A +"\t" + B + "\t" + C);
	}

}
