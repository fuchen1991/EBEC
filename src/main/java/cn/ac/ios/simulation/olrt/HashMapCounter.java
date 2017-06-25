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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HashMapCounter {

	Map<Integer,Integer> count;
	int stateSize;
	
	public HashMapCounter(int stateSize)
	{
		this.stateSize = stateSize;
		
		this.count = new HashMap<Integer,Integer>();
	}
	
	public int get(int label, int state)
	{
		return count.get(label*stateSize + state);
	}
	
	public void set(int label, int state,int value)
	{
		this.count.put(label*stateSize + state, value);
	}
	
	public boolean decrease(int label, int state)
	{
		int value = count.get(label*stateSize + state);
		if(value == 1)
		{
			count.remove(label*stateSize + state);
			return true;
		}
		
		this.count.put(label*stateSize + state, value - 1);
		return false;
	}
	
	public void copyCount(HashMapCounter counter)
	{
//		Iterator<Entry<Integer, Integer>> iter = counter.count.entrySet().iterator();
//		while (iter.hasNext()) 
//		{
//			Map.Entry<Integer,Integer> entry = (Map.Entry<Integer,Integer>) iter.next();
//			int key = entry.getKey();
//			int val = entry.getValue();
//			
//			this.count.put(key, val);
//		}
		
		this.count = new HashMap<Integer,Integer>(counter.count);
	}
}
