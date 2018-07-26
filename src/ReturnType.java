public enum ReturnType {
	/*
	 * *** Add new return types here ***
	 * 
	 * Format is:
	 * RETURNTYPE("SwiftVariableType", "JNI_signature", "JNI method call type", isObject)
	 * 
	 * - "isObject" is used for wrapping the methods, primitive types aren't objects
	 *   but View, String, Drawable etc. are objects
	 *   
	 * Follow the existing types below for reference.
	 * 
	 */
	VOID("", "V", "CallVoidMethod", "", false),
	BOOLEAN("Bool", "Z", "CallBooleanMethod", "\n\treturn __return != jboolean(JNI_FALSE)", false),
	INT("Int", "I", "CallIntMethod", "\n\treturn Int(__return)", false),
	FLOAT("Float", "F", "CallFloatMethod", "\nreturn Float(__return)", false),
	STRING("String", "Ljava/lang/String;", "CallObjectMethod", "\n\treturn String(javaObject: __return)", true),
	INTENT("Android.Content.Intent", "Landroid/content/Intent;", "CallObjectMethod", "\n\treturn Android.Content.Intent(javaObject: __return)", true),
	VIEW("Android.View.View", "Landroid/view/View;", "CallObjectMethod", "\n\treturn Android.View.View(javaObject: __return)", true);
	
	private String swiftReturn, sigValue, callMethod, returnStatement;
	boolean isObject;
	ReturnType(String swiftReturn, String sigValue, String callMethod, String returnStatement, boolean isObject) {
		this.swiftReturn = swiftReturn;
		this.sigValue = sigValue;
		this.callMethod = callMethod;
		this.returnStatement = returnStatement;
		this.isObject = isObject;
	}
	
	public String getSwiftReturn() {
		return swiftReturn;
	}
	public String getSigValue() {
		return sigValue;
	}
	public String getCallMethod() {
		return callMethod;
	}
	public String getReturnStatement() {
		return returnStatement;
	}
	
	public String toString() {
		return "Values:\n\tSwift return: " + getSwiftReturn() + "\n\tSig value: " +
				getSigValue() + "\n\tCall method: " + getCallMethod() + "\n\tReturn Statement: " + getReturnStatement();
	}
}
