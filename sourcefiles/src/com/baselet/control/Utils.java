package com.baselet.control;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.RenderingHints;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.baselet.control.Constants.LineType;


public abstract class Utils {

	private final static Logger log = Logger.getLogger(Utils.getClassName());
	
	private Utils() {} // private constructor to avoid instantiation

	/**
	 * This method checks if the drawing of graphics should start at pixel (1,1) instead of (0,0) or not
	 */
	public static boolean displaceDrawingByOnePixel() {
		if (Constants.SystemInfo.JAVA_IMPL == Constants.JavaImplementation.OPEN) return true;
		else return false;
	}

	// Not used
	public static File createRandomFile(String extension) {
		File randomFile = new File(Path.homeProgram() + "tmp.diagram." + new Date().getTime() + "." + extension);
		randomFile.deleteOnExit();
		return randomFile;
	}

	public static Point normalize(Point p, int pixels) {
		Point ret = new Point();
		double d = Math.sqrt(p.x * p.x + p.y * p.y);
		ret.x = (int) (p.x / d * pixels);
		ret.y = (int) (p.y / d * pixels);
		return ret;
	}

	public static Vector<String> decomposeStringsWithEmptyLines(String s, String delimiter) {
		return Utils.decomposeStringsWFilter(s, delimiter, true, false);
	}

	public static Vector<String> decomposeStringsWithComments(String s, String delimiter) {
		return Utils.decomposeStringsWFilter(s, delimiter, false, true);
	}

	public static Vector<String> decomposeStrings(String s, String delimiter) {
		return Utils.decomposeStringsWFilter(s, delimiter, true, true);
	}

	public static Vector<String> decomposeStrings(String s) {
		return decomposeStrings(s, Constants.NEWLINE);
	}

	static Vector<String> decomposeStringsWFilter(String s, String delimiter, boolean filterComments, boolean filterNewLines) {
		s = s.replaceAll("\r\n", delimiter); // compatibility to windows \r\n
		Vector<String> ret = new Vector<String>();
		for (;;) {
			int index = s.indexOf(delimiter);
			if (index < 0) {
				if (filterComments) {
					s = Utils.filterComment(s);
					if (s.startsWith("bg=") || s.startsWith("fg=") ||
							s.startsWith(Constants.AUTORESIZE)) s = ""; // filter color-setting strings

				}
				if (!s.equals("") || !filterNewLines) {
					ret.add(s);
				}
				return ret;
			}
			String tmp = s.substring(0, index);
			if (filterComments) {
				tmp = Utils.filterComment(tmp);
				if (tmp.startsWith("bg=") || tmp.startsWith("fg=") ||
						s.startsWith(Constants.AUTORESIZE)) tmp = ""; // filter color-setting strings
			}

			if (!tmp.equals("") || !filterNewLines) ret.add(tmp);
			s = s.substring(index + delimiter.length(), s.length());
		}
	}

	public static String filterComment(String s) {

		int pos = s.indexOf("//");
		char c;
		while (pos >= 0) {
			if (pos == 0) return "";
			c = s.charAt(pos - 1);
			if (s.length() > pos + 2) {
				if ((s.charAt(pos + 2) != '/') && (c != '/') && (c != ':')) return s.substring(0, pos);
			}
			else if ((c != '/') && (c != ':')) return s.substring(0, pos);

			pos = s.indexOf("//", pos + 1);
		}
		return s;
	}

	public static Vector<String> decomposeStringsIncludingEmptyStrings(String s, String delimiter) {
		return decomposeStringsWFilter(s, delimiter, false, false);
	}

	public static String composeStrings(Vector<String> v, String delimiter) {
		String ret = null;
		if (v != null) {
			for (int i = 0; i < v.size(); i++) {
				if (ret == null) {
					ret = new String(v.elementAt(i));
				}
				else {
					ret = ret + delimiter + v.elementAt(i);
				}
			}
		}
		if (ret == null) ret = "";
		return ret;
	}

	public static BasicStroke getStroke(LineType lineType, int lineThickness) {
		// If the lineThickness is not supported, the default type is used
		if (lineThickness < 0) lineThickness = Constants.DEFAULT_LINE_THICKNESS;

		BasicStroke stroke = null;
		switch (lineType) {
			case SOLID:
				stroke = new BasicStroke(lineThickness);
				break;
			case DASHED:
				float dash1[] = { 8.0f, 5.0f };
				stroke = new BasicStroke(lineThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash1, 0.0f);
				break;
			case DOTTED:
				float dash2[] = { 1.0f, 2.0f };
				stroke = new BasicStroke(lineThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash2, 0.0f);
				break;
		}
		return stroke;
	}

	public static Map<RenderingHints.Key, Object> getUxRenderingQualityHigh(boolean subpixelRendering) {
		HashMap<RenderingHints.Key, Object> renderingHints = new HashMap<RenderingHints.Key, Object>();
		renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		if (subpixelRendering) renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		else renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		return renderingHints;
	}

	/**
	 * Calculates and returns the angle of the line defined by the coordinates
	 */
	public static double getAngle(double x1, double y1, double x2, double y2) {
		double res;
		double x = x2 - x1;
		double y = y2 - y1;
		res = Math.atan(y / x);
		if ((x >= 0.0) && (y >= 0.0)) res += 0.0;
		else if ((x < 0.0) && (y >= 0.0)) res += Math.PI;
		else if ((x < 0.0) && (y < 0.0)) res += Math.PI;
		else if ((x >= 0.0) && (y < 0.0)) res += 2.0 * Math.PI;
		return res;
	}

	/**
	 * Converts colorString into a Color which is available in the colorMap or if not tries to decode the colorString
	 * 
	 * @param colorString
	 *            String which describes the color
	 * @return Color which is related to the String or null if it is no valid colorString
	 */
	public static Color getColor(String colorString) {
		Color returnColor = null;
		for (String color : Constants.colorMap.keySet()) {
			if (colorString.equals(color)) {
				returnColor = Constants.colorMap.get(color);
				break;
			}
		}
		if (returnColor == null) {
			try {
				returnColor = Color.decode(colorString);
			} catch (NumberFormatException e) {
				log.error("Invalid color:" + colorString);
			}
		}
		return returnColor;
	}

	public static String getClassName() {
		return Thread.currentThread().getStackTrace()[2].getClassName();
		//		return new RuntimeException().getStackTrace()[1].getClassName(); //ALSO POSSIBLE
	}

	/**
	 * eg: createDoubleArrayFromTo(5, 6, 0.1) = [5, 5.1, 5.2, ..., 5.9, 6] <br/>
	 * eg: createDoubleArrayFromTo(10, 20, 3) = [10, 13, 16, 19, 22] <br/>
	 * 
	 * @param min	first value of the result array
	 * @param max	if this value is reached (or passed if it's not dividable through "step") the array is finished
	 * @param step	the stepsize of the array
	 */
	public static Double[] createDoubleArrayFromTo(Double min, Double max, Double step) {
		if (min > max) return null;
		int range = (int) Math.ceil(((max-min)/step)+1);
		Double[] returnArray = new Double[range];
		for (int i = 0; i < range; i++) {
			returnArray[i] = min + i*step;
		}
		return returnArray;
	}
	
	public static Double[] createDoubleArrayFromTo(Double min, Double max) {
		return createDoubleArrayFromTo(min, max, 1D);
	}

	public static Color darkenColor(String inColor, int factor) {
		return darkenColor(getColor(inColor), factor);
	}
	
	public static Color darkenColor(Color inColor, int factor) {
		int r = Math.max(0, inColor.getRed() - factor);
		int g = Math.max(0, inColor.getGreen() - factor);
		int b = Math.max(0, inColor.getBlue() - factor);
		
		return new Color(r,g,b);
	}

//	/**
//	 * Converts a String[] into a int[]. If something goes wrong (eg: a NumberFormatException gets thrown) null is returned
//	 * @param stringArray String[] to convert
//	 * @return int[] or null if error
//	 */
//	public static int[] convertStringToIntArray(String[] stringArray) {
//		if (stringArray == null) return null;
//		else {
//			int[] intArray = new int[stringArray.length];
//			try {
//				for (int i = 0; i < stringArray.length; i++) {
//					intArray[i] = Integer.parseInt(stringArray[i]);
//				}
//			} catch (NumberFormatException e) {
//				return null;
//			}
//			return intArray;
//		}
//	}
}
