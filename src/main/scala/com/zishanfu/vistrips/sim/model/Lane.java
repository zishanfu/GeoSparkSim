package com.zishanfu.vistrips.sim.model;

import java.awt.Point;
import java.io.Serializable;

public class Lane implements Serializable{
	private Point head;
	private Point tail;
	
	public Lane(Point head, Point tail) {
		this.head = head;
		this.tail = tail;
	}

	public Point getHead() {
		return head;
	}

	public void setHead(Point head) {
		this.head = head;
	}

	public Point getTail() {
		return tail;
	}

	public void setTail(Point tail) {
		this.tail = tail;
	}
	
	
}
