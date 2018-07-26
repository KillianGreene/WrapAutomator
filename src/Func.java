import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.activation.UnsupportedDataTypeException;

public class Func {
	boolean debug;
	// What the function returns
	// Int, String, Double, etc
	public String comment, methodName, duplicateNumber, parameters;
	// type of args, name of arg
	// ex. int[], "buttons"
	public ArrayList<String> argTypes, argNames;
	// Number of params the func takes, useful for getArgsSet()
	public int numArgs;
	boolean hasReturn, isArray;
	ReturnType returnType;

	public Func(String comment, String methodName, String parameters, ReturnType returnType, boolean isArray, String duplicateNumber, boolean debug) {
		this.comment = comment;
		this.methodName = methodName;
		this.parameters = parameters;
		this.returnType = returnType;
		this.isArray = isArray;
		this.duplicateNumber = duplicateNumber;
		this.debug = debug;
		if (!parameters.equals("")) {
			fillArgArrays();
		}
		hasReturn = !(returnType.equals(ReturnType.VOID));
		
		//System.out.println("New func object started with values:\n" + comment + "\n" + methodName + "\n" + returnType.toString() + "\n" + isArray + "\n\n\n");
	}
	
	// fills the arrays of arg names/types, and sets the method name
	private void fillArgArrays() {	
		String text = parameters.replaceAll(",", "");
		String[] allText = text.split(" ");
		
		print("new text: " + text);
		
		argTypes = new ArrayList<String>();
		argNames = new ArrayList<String>();
		
		for (int i=0; i<allText.length; i++) {
			if (i%2==0) {
				if (isArrayType(allText[i])) {
					argTypes.add("[" + allText[i].substring(0, allText[i].length()-2) + "]");
				} else {
					argTypes.add(allText[i]);
				}
				print("\t\t\tArg type:" + allText[i]);
			} else {
				print("\t\t\tArg name:" + allText[i]);
				argNames.add(allText[i]);
			}
		}
		numArgs = argTypes.size();
		
		print("numArgs: " + numArgs + " arg: " + argTypes.get(0));
		
		return;
	}
	
	// Generate the values into text that will be written in swift
	
	
	// ---Used in final:
	// no bracket
	public String getFirstLine() throws UnsupportedDataTypeException {
		String extraReturnCharsStart = "";
		String extraReturnCharsEnd = "";
		if (!returnType.equals(ReturnType.VOID)) {
			extraReturnCharsStart = "-> ";
		}
		if (isArray) {
			extraReturnCharsStart += "[";
			extraReturnCharsEnd = "]?";
		}
		return "public func " + methodName + "(" + getSwiftArgs() + ") " +
			extraReturnCharsStart + returnType.getSwiftReturn() + extraReturnCharsEnd;
	}
	public String getSwiftArgs() throws UnsupportedDataTypeException {
		String returnString = "";
		for (int i=0; i<numArgs; i++) {
			returnString += argNames.get(i) + ": " + convertToSwiftType(argTypes.get(i)); 
			if (i != (numArgs - 1)) {
				returnString += ", ";
			}
		}
		return returnString;
	}
	public String convertToSwiftType(String argType) throws UnsupportedDataTypeException {
		String bracketStart = "";
		String bracketEnd = "";
		if (isConvertedArray(argType)) {
			argType = argType.substring(1, argType.length()-1);
			bracketStart = "[";
			bracketEnd = "]";
		}
		if (argType.equalsIgnoreCase("int")) {
			return bracketStart + "Int" + bracketEnd;
		} else if (argType.equalsIgnoreCase("boolean")) {
			return bracketStart + "Bool" + bracketEnd;
		} else if (argType.equalsIgnoreCase("long")) {
			return bracketStart + "Int64" + bracketEnd;
		} else if (argType.equalsIgnoreCase("double")) {
			return bracketStart + "Double" + bracketEnd;
		} else if (argType.equalsIgnoreCase("byte")) {
			return bracketStart + "Int8" + bracketEnd;
		} else if (argType.equalsIgnoreCase("char")) {
			return bracketStart + "Character" + bracketEnd;
		} else if (argType.equalsIgnoreCase("short")) {
			return bracketStart + "Int16" + bracketEnd;
		} else if (argType.equalsIgnoreCase("float")) {
			return bracketStart + "Float" + bracketEnd;
		} else if (argType.equalsIgnoreCase("String")) {
			return bracketStart + "String" + bracketEnd;
		} else if (Pattern.compile(Pattern.quote(argType), Pattern.CASE_INSENSITIVE).matcher("ONCLICKLISTENERKEYEVENTMOTIONEVENTRECTRUNNABLECANVASVIEWSTRUCTUREDRAWABLEVIEW").find()) {
			return bracketStart + getVarConversion(argType).getSwiftType() + bracketEnd;
		} else {
			throw new UnsupportedDataTypeException(argType);
			
		}
	}
	// Returns if there is "[]" in the var type, meaning the variable is an array 
	public boolean isArrayType(String argType) {
		return argType.substring(argType.length()-2).equals("[]");
	}
	public boolean isConvertedArray(String arg) {
		return arg.contains("[");
	}
	// the first line of "var __args = "
	public String getArgsInit() {
		int numArgsPrint = numArgs;
		if (numArgs == 0) {
			numArgsPrint = 1;
		}
		return "var __args = [jvalue]( repeating: jvalue(), count: " + numArgsPrint + " )";
	}
	// all the lines of "__args[0] = ..."
	//	(will be for loop)
	public String getArgsSet() throws UnsupportedDataTypeException {
		if (numArgs == 0) {
			return "";
		}
		
		print("Num args: " + numArgs);
		
		String returnString = "";
		String argType;
		String argName;
		
		for (int i=0; i<numArgs; i++) {
			if (checkIfObject(argTypes.get(i))) {
				argType = "object";
			} else {
				argType = argTypes.get(i);
			}
			argName = argNames.get(i);
			returnString += "__args[" + i + "] = " + getArgLine(argType, argName);
			if (i != (numArgs - 1)) {
				returnString += "\n\t";
			}
			
		}
		returnString += "\n";
		return returnString + "\n";
	}
	public boolean checkIfObject(String argType) {
		// add more types to understand
		if (argType.equalsIgnoreCase("String")) {
			return true;
		}
		return false;
	}
	public String getArgLine(String argType, String argName) throws UnsupportedDataTypeException {
		if (isConvertedArray(argType)) {
			//argType = argType.substring(1, argType.length()-1);
			return "JNIType.toJava(value: " + argName + ", locals: &__locals)";
		}
		if (argType.equalsIgnoreCase("int")) {
			return "jvalue(i: jint(" + argName + "))";
		} else if (argType.equalsIgnoreCase("boolean")) {
			return "jvalue(z: jboolean(" + argName + " ? JNI_TRUE : JNI_FALSE))";
		} else if (argType.equalsIgnoreCase("long")) {
			return "jvalue(j: jlong(" + argName + "))";
		} else if (argType.equalsIgnoreCase("double")) {
			return "jvalue(d: jdouble(" + argName + "))";
		} else if (argType.equalsIgnoreCase("byte")) {
			return "jvalue(b: jbyte(" + argName + "))";
		} else if (argType.equalsIgnoreCase("char")) {
			return "jvalue(c: jchar(" + argName + "))";
		} else if (argType.equalsIgnoreCase("short")) {
			return "jvalue(s: jshort(" + argName + "))";
		} else if (argType.equalsIgnoreCase("float")) {
			return "jvalue(f: jfloat(" + argName + "))";
		} else if (argType.equalsIgnoreCase("object")) {
			return "JNIType.toJava(value: " + argName + ", locals: &__locals)";
		//} else if (argType.equalsIgnoreCase("View.onclicklistener") || argType.equalsIgnoreCase("View")) {/////////////////// Or others
		} else if (Pattern.compile(Pattern.quote(argType), Pattern.CASE_INSENSITIVE).matcher("ONCLICKLISTENERKEYEVENTMOTIONEVENTRECTRUNNABLECANVASVIEWSTRUCTUREDRAWABLEVIEW").find()) {
			return "JNIType.toJava(value: " + argName + ", locals: &__locals)";
		} else {
			throw new UnsupportedDataTypeException(argType);
		}
	}
	// Returns type signature for passed java type
	public String getTypeSig(String argType) throws UnsupportedDataTypeException {
		String addBracket = "";
		if (isConvertedArray(argType)) {
			addBracket = "[";
			argType = argType.substring(1, argType.length()-1);
		}
		if (argType.equalsIgnoreCase("int")) {
			return addBracket + "I";
		} else if (argType.equalsIgnoreCase("boolean")) {
			return addBracket + "Z";
		} else if (argType.equalsIgnoreCase("long")) {
			return addBracket + "J";
		} else if (argType.equalsIgnoreCase("double")) {
			return addBracket + "D";
		} else if (argType.equalsIgnoreCase("byte")) {
			return addBracket + "B";
		} else if (argType.equalsIgnoreCase("char")) {
			return addBracket + "C";
		} else if (argType.equalsIgnoreCase("short")) {
			return addBracket + "S";
		} else if (argType.equalsIgnoreCase("float")) {
			return addBracket + "F";
		}
		else if (Pattern.compile(Pattern.quote(argType), Pattern.CASE_INSENSITIVE).matcher("ONCLICKLISTENERKEYEVENTMOTIONEVENTRECTRUNNABLECANVASVIEWSTRUCTUREDRAWABLEVIEW").find()) {
			return getVarConversion(argType).getSignature();
		}
		else if (argType.equalsIgnoreCase("String")) {
			return addBracket + "Ljava/lang/String;";
		} else {
			throw new UnsupportedDataTypeException(argType);
		}
	}
	
	public Var getVarConversion(String type) throws UnsupportedDataTypeException {
		if (type.equalsIgnoreCase("View")) {
			return Var.VIEW;
		} else if (type.equalsIgnoreCase("onclicklistener")) {
			return Var.ONCLICKLISTENER;
		} else if (type.equalsIgnoreCase("keyevent")) {
			return Var.KEYEVENT;
		} else if (type.equalsIgnoreCase("motionevent")) {
			return Var.MOTIONEVENT;
		} else if (type.equalsIgnoreCase("rect")) {
			return Var.RECT;
		} else if (type.equalsIgnoreCase("runnable")) {
			return Var.RUNNABLE;
		} else if (type.equalsIgnoreCase("canvas")) {
			return Var.CANVAS;
		} else if (type.equalsIgnoreCase("viewstructure")) {
			return Var.VIEWSTRUCTURE;
		} else if (type.equalsIgnoreCase("drawable")) {
			return Var.DRAWABLE;
		} else {
			throw new UnsupportedDataTypeException(type);
		}
	}
	
	// ex. "callVoidMethod", "callIntMethod", etc.
	public String getCallType() {
		if (isArray) {
			return "CallObjectMethod";
		}
		return returnType.getCallMethod();
	}
	// method name surrounded by quotes
	// 		"isClickable"
	public String getMethodName() {
		return methodName;
	}
	// (params)return type
	// Z, B, I, Ljava/lang/String; etc
	public String getMethodSig() throws UnsupportedDataTypeException {
		String returnBracket = "";
		if (numArgs == 0) {
			return "\"()" + returnType.getSigValue() + "\"";
		}
		String parameterString = "";
		for (String argType : argTypes) {
			parameterString += getTypeSig(argType);
		}
		if (isArray) {
			returnBracket = "[";
		}
		return "\"(" + parameterString + ")" + returnBracket + returnType.getSigValue() + "\"";
	}
	// remember to add numbers if more than one
	public String getMethodCache() {
		return "&JNICache.MethodID." + methodName + duplicateNumber;
	}
	public String getDeferLine() {
		if (returnType.isObject) {
			return "defer { JNI.DeleteLocalRef( __return ) }\n";
		} else {
			return "";
		}
	}
	// Generate "return ..."
	// Only if function doesn't return void
	public String getReturnLine() {
		if (isArray) {
			return getArrayReturnLine();
		}
		return returnType.getReturnStatement();
	}
	public String getArrayReturnLine() {
		return "\n\treturn JNIType.toSwift(type: [" + returnType.getSwiftReturn() + "].self, from: __return)";
	}
	
	public String getFullCallMethod() throws UnsupportedDataTypeException {
		String tabs, returnVarSet;
		if (returnType.equals(ReturnType.VOID)) {
			returnVarSet = "";
			tabs = "\t\t";
		} else {
			returnVarSet = "let __return = ";
			tabs = "\t\t\t\t\t";
		}
		
		return "\t" + returnVarSet + "JNIMethod." + getCallType() + "(" + "\n" +
				tabs + "object: javaObject" + ",\n" +
				tabs + "methodName: \"" + getMethodName() + "\",\n" +
				tabs + "methodSig: " + getMethodSig() + ",\n" +
				tabs + "methodCache: " + getMethodCache() + ",\n" +
				tabs + "args: &__args" + ",\n" +
				tabs + "locals: &__locals" + " )\n";
	}
	
	public String getComment() {
		if (comment.equals("")) {
			return comment;
		}
		return "/**\n * " + comment + "\n */";
	}
	
	
	// ---Final
	// generate the full wrapped method
	public String getFullReturn() throws UnsupportedDataTypeException {
		return getComment()  + "\n" + 
				getFirstLine() + " {\n\n" +
				"\tvar __locals = [jobject]()" + "\n\n" +
				"\t" + getArgsInit() + "\n\n" +
				"\t" + getArgsSet() + 
				getFullCallMethod() +
				"\t" + getDeferLine() + 
				getReturnLine() + 
				"\n}";
	}
	
	public void print(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

}
