/*******************************************************************************
 * Copyright (C) 2016-2017 Chen Fu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package cn.ac.ios.myqueue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

public class HashSetDFS<T> implements MyQueue<T> {
	private Stack<T> stack;
	private HashSet<T> set;
	
	public HashSetDFS()
	{
		stack = new Stack<T>();
		set = new HashSet<T>();
	}
	
	
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	public boolean contains(T t) {
		return set.contains(t);
	}

	public void push(T t) {
		set.add(t);
		stack.push(t);
	}

	public T pop() {
		// TODO Auto-generated method stub
		return stack.pop();
	}

	public Collection<T> getCollection() {
		// TODO Auto-generated method stub
		return stack;
	}

	public void clear() {
		// TODO Auto-generated method stub
		set.clear();
		stack.clear();
	}

	public int size() {
		// TODO Auto-generated method stub
		return stack.size();
	}


}
