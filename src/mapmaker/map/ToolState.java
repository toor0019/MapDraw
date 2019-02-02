package mapmaker.map;
public class ToolState {

		
	private static ToolState toolstate;
	private Tools tool;
	private int option;
	
private ToolState() {
		setTool(Tools.Select);
	}

public static ToolState getToolState() {
	if(toolstate==null) {
		toolstate = new ToolState();
		return toolstate;
	}else {
		return toolstate;
	}
}

public Tools getTool() {
	return tool;
}

public void setTool(Tools t) {
	this.tool=t;
}

public int getOption() {
	return option;
}

public void setOption(int option) {
	this.option = option;
}
}
