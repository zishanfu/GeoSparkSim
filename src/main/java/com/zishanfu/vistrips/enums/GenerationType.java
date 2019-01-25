package com.zishanfu.vistrips.enums;

public enum GenerationType {
	//data-space oriented approach(DSO)
	DSO,
	//region-based approach(RB)
	RB,
	//network-based approach(NB)
	NB;
	
	public static GenerationType getGenerationType(String str)
    {
        for (GenerationType type : GenerationType.values()) {
            if (type.name().equalsIgnoreCase(str)) { return type; }
        }
        return null;
    }
}
