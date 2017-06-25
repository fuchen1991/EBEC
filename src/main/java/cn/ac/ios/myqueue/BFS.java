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
import java.util.LinkedList;

public class BFS<T> implements MyQueue<T> {

	private LinkedList<T> list;
	
	public BFS()
	{
		list = new LinkedList<T>();
	}
	
	public boolean isEmpty() {
		
		return list.isEmpty();
	}

	public boolean contains(T t) {
		
		return list.contains(t);
	}

	public void push(T t) {
		
		list.offer(t);
	}

	public T pop() {
		
		return list.poll();
	}
	
	public Collection<T> getCollection()
	{
		return list;
	}

	public void clear() {
		list.clear();
		
	}

	public int size() {
		
		return list.size();
	}

	public void remove(int i) {
		list.remove(i);
		
	}

	public T get(int i) {
		
		return list.get(i);
	}

	public boolean direction() {
		
		return true;
	}

}
