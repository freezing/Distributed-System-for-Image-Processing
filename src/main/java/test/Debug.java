package test;

import java.io.PrintStream;
import java.util.Locale;

public final class Debug {
	private static boolean enabled[] = new boolean[10000];
	static {
		enabled[0] = true;
		enabled[100] = true;
		enabled[101] = true;
		enabled[102] = true;
		enabled[103] = true;
		enabled[104] = true;
		enabled[1000] = true;
	}

	public static void flush(int type) {
		if (!enabled[type]) return;
		System.out.flush();
	}

	public static void close(int type) {
		if (!enabled[type]) return;
		System.out.close();
	}

	public static boolean checkError(int type) {
		if (!enabled[type]) return false;
		return System.out.checkError();
	}

	public static void write(int type, int b) {
		if (!enabled[type]) return;
		System.out.write(b);
	}

	public static void write(int type, byte[] buf, int off, int len) {
		if (!enabled[type]) return;
		System.out.write(buf, off, len);
	}

	public static void print(int type, boolean b) {
		if (!enabled[type]) return;
		System.out.print(b);
	}

	public static void print(int type, char c) {
		if (!enabled[type]) return;
		System.out.print(c);
	}

	public static void print(int type, int i) {
		if (!enabled[type]) return;
		System.out.print(i);
	}

	public static void print(int type, long l) {
		if (!enabled[type]) return;
		System.out.print(l);
	}

	public static void print(int type, float f) {
		if (!enabled[type]) return;
		System.out.print(f);
	}

	public static void print(int type, double d) {
		if (!enabled[type]) return;
		System.out.print(d);
	}

	public static void print(int type, char[] s) {
		if (!enabled[type]) return;
		System.out.print(s);
	}

	public static void print(int type, String s) {
		if (!enabled[type]) return;
		System.out.print(s);
	}

	public static void print(int type, Object obj) {
		if (!enabled[type]) return;
		System.out.print(obj);
	}

	public static void println(int type) {
		if (!enabled[type]) return;
		System.out.println();
	}

	public static void println(int type, boolean x) {
		if (!enabled[type]) return;
		System.out.println(x);
	}

	public static void println(int type, char x) {
		if (!enabled[type]) return;
		System.out.println(x);
	}

	public static void println(int type, int x) {
		if (!enabled[type]) return;
		System.out.println(x);
	}

	public static void println(int type, long x) {
		if (!enabled[type]) return;
		System.out.println(x);
	}

	public static void println(int type, float x) {
		if (!enabled[type]) return;
		System.out.println(x);
	}

	public static void println(int type, double x) {
		if (!enabled[type]) return;
		System.out.println(x);
	}

	public static void println(int type, char[] x) {
		if (!enabled[type]) return;
		System.out.println(x);
	}

	public static void println(int type, String x) {
		if (!enabled[type]) return;
		System.out.println(x);
	}

	public static void println(int type, Object x) {
		if (!enabled[type]) return;
		System.out.println(x);
	}

	public static PrintStream printf(int type, String format, Object... args) {
		if (!enabled[type]) return null;
		return System.out.printf(format, args);
	}

	public static PrintStream printf(int type, Locale l, String format, Object... args) {
		if (!enabled[type]) return null;
		return System.out.printf(l, format, args);
	}

	public static PrintStream format(int type, String format, Object... args) {
		if (!enabled[type]) return null;
		return System.out.format(format, args);
	}

	public static PrintStream format(int type, Locale l, String format, Object... args) {
		if (!enabled[type]) return null;
		return System.out.format(l, format, args);
	}

	public static PrintStream append(int type, CharSequence csq) {
		if (!enabled[type]) return null;
		return System.out.append(csq);
	}

	public static PrintStream append(int type, CharSequence csq, int start, int end) {
		if (!enabled[type]) return null;
		return System.out.append(csq, start, end);
	}

	public static PrintStream append(int type, char c) {
		if (!enabled[type]) return null;
		return System.out.append(c);
	}

}
