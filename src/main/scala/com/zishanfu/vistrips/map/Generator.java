package com.zishanfu.vistrips.map;

import com.zishanfu.vistrips.model.Pair;

public interface Generator {
	Pair computeAPair(String type);
	Pair[] computePairs(int num, String type);
	int getLongestTrip();
}
