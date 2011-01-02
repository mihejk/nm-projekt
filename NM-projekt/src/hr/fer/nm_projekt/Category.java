package hr.fer.nm_projekt;

public class Category {
	
	public static final int EMPTY = 0;
	
	public static final int DASH = 1;
	
	public static final int A = 2;
	
	public static final int B = 3;
	
	public static final int C = 4;
	
	public static final int D = 5;
	
	public static final int E = 6;
	
	public static final int F = 7;
	
	public static final int G = 8;
	
	public static final int H = 9;
	
	public static final int I = 10;
	
	public static final int J = 11;
	
	public static final int CATEGORY_COUNT = 12;
	
	public static int fromString( String categoryString ) {
		if( categoryString.equals("empty") ) return Category.EMPTY;
		if( categoryString.equals("-") ) return Category.DASH;
		if( categoryString.equals("A") ) return Category.A;
		if( categoryString.equals("B") ) return Category.B;
		if( categoryString.equals("C") ) return Category.C;
		if( categoryString.equals("D") ) return Category.D;
		if( categoryString.equals("E") ) return Category.E;
		if( categoryString.equals("F") ) return Category.F;
		if( categoryString.equals("G") ) return Category.G;
		if( categoryString.equals("H") ) return Category.H;
		if( categoryString.equals("I") ) return Category.I;
		if( categoryString.equals("J") ) return Category.J;
		
		throw new IllegalArgumentException("Unknown category string: " + categoryString);		
	}

	public static String toString( int category ) {
		if( category == EMPTY ) return "empty";
		if( category == DASH ) return "-";
		if( category == A ) return "A";
		if( category == B ) return "B";
		if( category == C ) return "C";
		if( category == D ) return "D";
		if( category == E ) return "E";
		if( category == F ) return "F";
		if( category == G ) return "G";
		if( category == H ) return "H";
		if( category == I ) return "I";
		if( category == J ) return "J";
		
		throw new IllegalArgumentException("Unknown category: " + category);
	}
}
