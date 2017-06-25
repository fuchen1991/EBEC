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

import java.util.BitSet;

public class Counter {

	int[] count;
	
	//int inLabelSize;
	//int[] labelMap;
	
	int labelSize;
	int stateSize;
	
	public Counter(int labelSize, int stateSize)
	{
		this.labelSize = labelSize;
		this.stateSize = stateSize;
		
		count = new int[labelSize*stateSize];
		
		//this.labelMap = new int[labelSize];
	}
	
	public int get(int label, int state)
	{
		return this.count[label*stateSize + state];
	}
	
	public void set(int label, int state,int value)
	{
		this.count[label*stateSize + state] = value;
	}
	
	public void decrease(int label, int state)
	{
		--this.count[label*stateSize + state];
	}
	
	public void copyCount(Counter counter,BitSet in_labels)
	{
		//this.inLabelSize = in_labels.cardinality();
		//this.labelMap = new int[labelSize];
		
//		int index = 0;
//		for(int i=in_labels.nextSetBit(0);i>=0;i=in_labels.nextSetBit(i+1))
//		{
//			labelMap[i] = index;
//			++index;
//		}
		
		//count = new int[labelSize*stateSize];
		
		for(int lb=in_labels.nextSetBit(0);lb>=0;lb=in_labels.nextSetBit(lb+1))
		{
			System.arraycopy(counter.count, lb*stateSize, this.count, lb*stateSize, stateSize);
			
//			for(int i=0;i<stateSize;++i)
//			{
//				this.count[lb*stateSize+i] = counter.count[lb*stateSize+i];
//			}
		}
	}
}
