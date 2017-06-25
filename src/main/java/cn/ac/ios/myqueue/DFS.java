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
import java.util.Stack;

public class DFS<T> implements MyQueue<T>{

	private Stack<T> stack;
	
	public DFS()
	{
		stack = new Stack<T>();
	}
	
	public boolean isEmpty() {
		
		return stack.isEmpty();
	}

	public boolean contains(T t) {
		
		return stack.contains(t);
	}

	public void push(T t) {
		
		stack.push(t);
	}

	public T pop() {
		
		return stack.pop();
	}
	
	public Collection<T> getCollection()
	{
		return stack;
	}

	public void clear() {
		stack.clear();
		
	}

	public int size() {
		
		return stack.size();
	}

	public void remove(int i) {
		stack.remove(i);
		
	}

	public T get(int i) {
		
		return stack.get(i);
	}

	public boolean direction() {
		
		return false;
	}

}
