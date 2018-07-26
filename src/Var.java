
public enum Var {
	// Fields:
	// 		isObject
	//		signature
	//		swiftType
	//		returnStatement
	
	ONCLICKLISTENER("JavaObject", "Landroid/view/View/OnClickListener;", true),
	KEYEVENT("JavaObject", "Landroid/view/KeyEvent;", true),
	MOTIONEVENT("JavaObject", "Landroid/view/MotionEvent;", true),
	RECT("JavaObject", "Landroid/graphics/Rect;", true),
	RUNNABLE("JavaObject", "Ljava/lang/Runnable;", true),
	CANVAS("JavaObject", "Landroid/graphics/Canvas;", true),
	VIEWSTRUCTURE("JavaObject", "Landroid/view/ViewStructure;", true),
	DRAWABLE("JavaObject", "Landroid/graphics/drawable/Drawable;", true),
	VIEW("Android.View.View", "Landroid/view/View;", true);
	
	private String swiftType, signature;
	private boolean isObject;
	
	Var(String swiftType, String signature, boolean isObject) {
		this.swiftType = swiftType;
		this.signature = signature;
		this.isObject = isObject;
	}

	public String getSwiftType() {
		return swiftType;
	}

	public String getSignature() {
		return signature;
	}

	public boolean isObject() {
		return isObject;
	}
	
}
