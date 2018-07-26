
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.activation.UnsupportedDataTypeException;

public class Automate {
	
	final static boolean DEBUG = false;
	final static boolean GENERATE_ERROR_COMMENTS = false;
	final static boolean OPTION_RESTRICT_TO_TYPE = false;
	
	public static HashMap<String, Integer> duplicates;
	private static HashMap<String, Integer> unsupportedReturnTypes;
	private static HashMap<String, Integer> unsupportedVariableTypes;
	
	private static Set<String> allUnsupportedVars;
	private static Set<String> allUnsupportedReturns;
	public static ArrayList<Integer> blacklist;
	public static ArrayList<String> comments, types, vars;
	public static void main(String[] args) {
		Scanner kb = new Scanner(System.in);
		
		duplicates = new HashMap<String, Integer>();
		unsupportedReturnTypes = new HashMap<String, Integer>();
		unsupportedVariableTypes = new HashMap<String, Integer>();
		blacklist = new ArrayList<Integer>();
		vars = new ArrayList<String>();
		comments = new ArrayList<String>();
		types = new ArrayList<String>();
		allUnsupportedReturns = new HashSet<String>();
		allUnsupportedVars = new HashSet<String>();
		
		// Set up file paths for loading and saving code
		System.out.print("Filepath to read Android doc?\n(example: \"/Users/killian.greene/Desktop/data.txt/\")\n>");
		String filepathLoad = kb.nextLine();
		if (filepathLoad.isEmpty()) {
			filepathLoad = "/Users/killian.greene/Desktop/data.txt/";
		}
		System.out.print("Filepath to save generated code?\n(example: \"/Users/killian.greene/Desktop/\")\n>");
		String filepathSave = kb.nextLine();
		if (filepathSave.isEmpty()) {
			filepathSave = "/Users/killian.greene/Desktop/";
		}
		// Constants not working 100% right now, disabled until further fixes
		/*
		System.out.print("'Constants' or 'methods'?\n>");
		String choice = kb.nextLine();
		String constantAccess = "";
		if (choice.equalsIgnoreCase("Constants")) {
			System.out.print("'Fileprivate' or 'internal'?\n>");
			constantAccess = kb.nextLine();
		}
		*/
		String choice = "methods";
		String constantAccess = "";
		System.out.print("Generate comments? (y/n)\n>");
		String generateComments = kb.nextLine();
		String restrictType = "";
		if (OPTION_RESTRICT_TO_TYPE) {
			System.out.print("Restrict to type?\n(Input 'none' for none)\n>");
			restrictType = kb.nextLine();
			if (restrictType.equalsIgnoreCase("none")) {
				restrictType = "";
			}
		}
		kb.close();
		// Set up scanner to read android doc file
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File(filepathLoad), "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// set up writer to make file of generated swift code
		FileWriter mainFileWriter, idFileWriter, unsupportedTypesFileWriter;
		BufferedWriter mainBufferedWriter, idBufferedWriter, unsupportedTypesBufferedWriter;
		PrintWriter mainOut = null, idOut = null, unsupportedTypesPrintWriter = null;
		try
			{
				mainFileWriter = new FileWriter(filepathSave + "generated code.txt", false);
		    	mainBufferedWriter = new BufferedWriter(mainFileWriter);
		    	mainOut = new PrintWriter(mainBufferedWriter);
		    	idFileWriter = new FileWriter(filepathSave + "ids.txt", false);
		    	idBufferedWriter = new BufferedWriter(idFileWriter);
		    	idOut = new PrintWriter(idBufferedWriter);
		    	unsupportedTypesFileWriter = new FileWriter(filepathSave + "unsupported types.txt", false);
		    	unsupportedTypesBufferedWriter = new BufferedWriter(unsupportedTypesFileWriter);
		    	unsupportedTypesPrintWriter = new PrintWriter(unsupportedTypesBufferedWriter);
			} catch (IOException e) {
			    e.printStackTrace();
			    System.exit(0);
			}
		unsupportedTypesPrintWriter.println("// Can't wrap:");
		// Add to arrays
		String currentLine = "";
		boolean lastLineWasVar = false;
		while (fileScanner.hasNext()) {
			currentLine = fileScanner.nextLine();
			if (currentLine.equals("")) {
				continue;
			}
			boolean isVarLine = false;
			for (char c : currentLine.toCharArray()) {
				if ((int)c == 9) {
					isVarLine = true;
					break;
				}
			}
			if (isVarLine) {
				replaceAndAdd(currentLine);
				if (lastLineWasVar) {
					comments.add("");
				}
				lastLineWasVar = true;
			} else {
				if (generateComments.equalsIgnoreCase("Y")) {
					comments.add(currentLine);
					lastLineWasVar = false;
				}
			}
		}
		
		System.out.println("\nWrapped the above classes.\nGenerated code saved to " + filepathSave);
		
		// Generate code and comments
		boolean isArray;
		for (int j = 0; j < types.size(); j++) {
			if (choice.equalsIgnoreCase("Constants")) {
				if (types.get(j).equals("String")) {
					System.out.println(getWrappedString(vars.get(j), comments.get(j)) + "\n");
				} else if (types.get(j).equals("int")) {
					System.out.println(getWrappedInt(vars.get(j), comments.get(j)) + "\n");
				} else {
					// Notify user that we can't wrap, and skip
					System.out.println("\n\n######\tUNKNOWN VARIABLE TYPE\t#####\n\n");
				} 
			}
			else {
				isArray = false;
				// check if static or final, if so delete words "static" and "final"
				if (types.get(j).contains(" ")) {
					//System.out.println("Fixing: " + types.get(j));
					String[] split = types.get(j).split(" ");
					for (int i=0; i<split.length; i++) {
					}
					types.set(j, split[split.length-1]);
				}
				
				// Check if it returns an array of a type, and set the notify var
				if (types.get(j).substring(types.get(j).length()-2).equals("[]")) {
					types.set(j, types.get(j).substring(0, types.get(j).length()-2));
					isArray = true;
				}
				ReturnType returnType = getReturnTypeValue(types.get(j));
				// Check if we don't support that type of return value in the enum yet
				if (returnType == null) {
					if (GENERATE_ERROR_COMMENTS) {
						mainOut.println(getCantWrapMessage(
								vars.get(j) + " \"" + types.get(j) + "\" is currently unsupported"));
					}
					unsupportedTypesPrintWriter.println(getCantWrapMessage(
							vars.get(j) + " \"" + types.get(j) + "\" is currently unsupported"));
					addOne(unsupportedReturnTypes, types.get(j));
					allUnsupportedReturns.add(types.get(j));
					blacklist.add(j);
					continue;
				}
				// check if the method was deprecated
				if (generateComments.equalsIgnoreCase("Y") && comments.get(j).contains("deprecated")) {
					blacklist.add(j);
					continue;
				}
				String name = getName(vars.get(j));
				print("Name is: " + name);
				String params = getParams(vars.get(j));
				String dupNumber = checkDuplicate(name);
				String commentString = "";
				
				if (generateComments.equalsIgnoreCase("Y")) {
					commentString = comments.get(j);
				}
				
				Func func = new Func(commentString, name, params, returnType, isArray, dupNumber, DEBUG);
				
				String output = "";
				// UnsupportedDataTypeException is thrown if we encounter a parameter that isn't supported
				try {
					output = func.getFullReturn() + "\n";
					// If the user opted to restrict output to only one return type
					if (OPTION_RESTRICT_TO_TYPE && !restrictType.equals("") && restrictType.contains(types.get(j))) {
						mainOut.println(output);
					}
					mainOut.println(output);
					System.out.println(name);
				} catch (UnsupportedDataTypeException e) {
					blacklist.add(j);
					if (GENERATE_ERROR_COMMENTS) {
						mainOut.println(getCantWrapMessage(
								vars.get(j) + " \"" + e.getMessage() + "\" is currently unsupported"));
					}
					unsupportedTypesPrintWriter.println(getCantWrapMessage(
							vars.get(j) + " \"" + e.getMessage() + "\" is currently unsupported"));
					addOne(unsupportedVariableTypes, e.getMessage());
					allUnsupportedVars.add(e.getMessage());
				}
				
				vars.set(j, vars.get(j).substring(0, vars.get(j).indexOf("(")) + dupNumber);
			}
		}
		mainOut.close();
		
		idOut.println("// jfield/jmethod IDs:\n\n");
		// Generate fieldIDs
		for (int i=0; i<vars.size(); i++) {
			if (choice.equalsIgnoreCase("constants")) {
				if (!blacklist.contains(i)) {
					//idOut.println("static var " + vars.get(i) + ": jfieldID?");
					if (OPTION_RESTRICT_TO_TYPE && !restrictType.equals("") && restrictType.contains(types.get(i))) {
						idOut.println("static var " + vars.get(i) + ": jfieldID?");
					}
					idOut.println("static var " + vars.get(i) + ": jfieldID?");
				} else {
					unsupportedTypesPrintWriter.println("// Couldn't wrap " + vars.get(i));
				}
			} else {
				if (!blacklist.contains(i)) {
					//idOut.println("static var " + vars.get(i) + ": jmethodID?");
					if (OPTION_RESTRICT_TO_TYPE && !restrictType.equals("") && restrictType.contains(types.get(i))) {
						idOut.println("static var " + vars.get(i) + ": jmethodID?");
					}
					idOut.println("static var " + vars.get(i) + ": jmethodID?");
				} else {
					unsupportedTypesPrintWriter.println("// Couldn't wrap " + vars.get(i));
				}
			}
		}
		// Only if private vars 
		
		if (constantAccess.equalsIgnoreCase("fileprivate")) {
			System.out.println("// Struct for fileprivate vars:\n\n");
			// Generate struct identities
			StringBuilder newLowercase = new StringBuilder("");
			int index, rep;
			for (String t : vars) {
				newLowercase = new StringBuilder(t.replace("_", "").toLowerCase());
				index = -1;
				rep = 0;
				do {
					index = t.indexOf("_", index + 1);
					if (index >= 0) {
						newLowercase.setCharAt(index + rep, Character.toUpperCase(newLowercase.charAt(index + rep)));
					}
					rep--;
				} while (index >= 0);
				idOut.println("public static let " + newLowercase + " = Android.ManifestPermission(rawValue:"
						+ "\n\tAndroidManifestPermissionConstants." + t + ")\n");
			} 
		}
		
		// Log all types of returns and variables that currently aren't supported by wrapping
		unsupportedTypesPrintWriter.println("\nUnsupported returns:\n");
		for (String ret : allUnsupportedReturns) {
			unsupportedTypesPrintWriter.println(unsupportedReturnTypes.get(ret) + "\t" + ret);
		}
		unsupportedTypesPrintWriter.println("\nUnsupported variables:\n");
		for (String var : allUnsupportedVars) {
			unsupportedTypesPrintWriter.println(unsupportedVariableTypes.get(var) + "\t" + var);
		}
		unsupportedTypesPrintWriter.close();
		idOut.close();
	}

	private static void replaceAndAdd(String currentLine) {
		currentLine = currentLine.replaceAll("final ", "");
		currentLine = currentLine.replaceAll("static ", "");
		for (int i = 0; i < currentLine.length(); i++) {
			if (Character.isWhitespace(currentLine.charAt(i))) {
				currentLine = currentLine.substring(0, i) + "~" + currentLine.substring(i+1);
				break;
			}
		}
		types.add(currentLine.substring(0, currentLine.indexOf("~")));
		vars.add(currentLine.substring(currentLine.indexOf("~")+1));
	}
	
	/*private static void replaceFix(String name, String type) {
		System.out.println("Adding: " + name + ", " + type);
	}*/

	// Check if method is a duplicate name, and increment it if so, then returns what
	// should be appended to the name ("" for 1, actual number for all else)
	private static String checkDuplicate(String name) {
		if (duplicates.containsKey(name)) {
			duplicates.put(name, duplicates.get(name)+1);
		} else {
			duplicates.put(name, 1);
		}
		if (duplicates.get(name) == 1) {
			return "";
		} else {
			return ""+duplicates.get(name);
		}
		
	}

	// Returns just method name 
	// 		methodOne(int n, String s) -> methodOne
	private static String getName(String fullName) {
		print("Got name " + fullName.substring(0, fullName.indexOf("(")));
		return fullName.substring(0, fullName.indexOf("("));
	}
	
	// Returns String of just parameters
	// 		methodOne(int n, String s) -> int n, String s
	private static String getParams(String fullName) {
		print("Got params \"" + fullName.substring(fullName.indexOf("(")+1, fullName.length()-1) + "\"");
		return fullName.substring(fullName.indexOf("(")+1, fullName.length()-1);
		
	}

	private static String getCantWrapMessage(String methodName) {
		//return  "/**\n * " + methodName + "\n */\n";
		return methodName;
	}

	public static String getWrappedString(String variableName, String commentText) {
		String comment = "/**\n * " + commentText + "\n */";
		/*return comment + "\nfileprivate static var " + variableName
				+ ": String {\n\n\tget {\n\n\t\tlet __value = JNIField.GetStaticObjectField(\n\t\t\tfieldName: \""
				+ variableName + "\",\n\t\t\tfieldType: \"Ljava/lang/String;\",\n\t\t\tfieldCache: &JNICache.FieldID."
				+ variableName
				+ ",\n\t\t\tclassName: JNICache.className,\n\t\t\tclassCache: &JNICache.jniClass )\n\t\tdefer { JNI.DeleteLocalRef(__value) }\n\t\treturn String(javaObject: __value)\n\t}\n}";
		*/
		String wrapVarType = "internal static var ";
		String initialType = ": String";
		String valueFunction = "JNIField.GetStaticObjectField";
		String fieldType = "Ljava/lang/String;";
		String returnValue = "String(javaObject: __value)";
		
		return comment + "\n" + wrapVarType + variableName + initialType + " {\n\n\tget {\n\n\t\tlet __value = " +
				valueFunction + "(\n\t\t\tfieldName: \"" + variableName + "\",\n\t\t\tfieldType: \"" + fieldType +
				"\",\n\t\t\tfieldCache: &JNICache.FieldID." + variableName +
				",\n\t\t\tclassName: JNICache.className,\n\t\t\tclassCache: &JNICache.jniClass )\n\n\t\treturn " +
				returnValue + "\n\t}\n}";
	}
	
	public static String getWrappedInt(String variableName, String commentText) {
		String comment = "/**\n * " + commentText + "\n */";
		String wrapVarType = "internal static var ";
		String initialType = ": Int";
		String valueFunction = "JNIField.GetStaticIntField";
		String fieldType = "I";
		String returnValue = "Int(__value)";
		
		return comment + "\n" + wrapVarType + variableName + initialType + " {\n\n\tget {\n\n\t\tlet __value = " +
				valueFunction + "(\n\t\t\tfieldName: \"" + variableName + "\",\n\t\t\tfieldType: \"" + fieldType +
				"\",\n\t\t\tfieldCache: &JNICache.FieldID." + variableName +
				",\n\t\t\tclassName: JNICache.className,\n\t\t\tclassCache: &JNICache.jniClass )\n\n\t\treturn " +
				returnValue + "\n\t}\n}";
				
	}
	
	public static ReturnType getReturnTypeValue(String javaType) {
		if (javaType.equalsIgnoreCase("boolean")) {
			return ReturnType.BOOLEAN;
		} else if (javaType.equalsIgnoreCase("int")) {
			return ReturnType.INT;
		} else if (javaType.equalsIgnoreCase("String")) {
			return ReturnType.STRING;
		} else if (javaType.equalsIgnoreCase("void")) {
			return ReturnType.VOID;
		} else if (javaType.equalsIgnoreCase("intent")) {
			return ReturnType.INTENT;
		} else if (javaType.equalsIgnoreCase("float")) {
			return ReturnType.FLOAT;
		} else if (javaType.equalsIgnoreCase("view")) {
			return ReturnType.VIEW;
		}
		else {
			return null;
		}
	}
	
	public static void print(String s) {
		if (DEBUG) {
			System.out.println(s);
		}
	}
	
	private static void addOne(HashMap<String, Integer> hashMap, String key) {
		if (hashMap.containsKey(key)) {
			hashMap.put(key, hashMap.get(key)+1);
		} else {
			hashMap.put(key, 1);
		}
	}

}



