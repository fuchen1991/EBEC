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
package cn.ac.ios.simulation.olrt;

import java.util.ArrayList;

public class MyArrayList {

	private ArrayList<Integer> list;
	
	public MyArrayList()
	{
		list = new ArrayList<Integer>();
	}
	
	public void add(int value)
	{
		list.add(value);
	}
	
	public int size()
	{
		return list.size();
	}
	
	public int get(int index)
	{
		return list.get(index);
	}
	
	public ArrayList<Integer> getList()
	{
		return this.list;
	}
	
	public boolean isEmpty()
	{
		return list.isEmpty();
	}
}
