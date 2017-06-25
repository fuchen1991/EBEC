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

public class BitSetBinaryRelation {

	int size;
	private int rowSize;
	private BitSet[] data;
	
	public BitSetBinaryRelation(int size, int maxSize)
	{
		this.size = size;
		
		this.rowSize = size*2;
		data = new BitSet[rowSize];
		for(int i=0;i<size;++i)
		{
			data[i] = new BitSet(maxSize);
		}
	}
	
	public void resize(int newSize)
	{
		assert(rowSize < newSize);
		
		BitSet[] newData = new BitSet[newSize];
		for(int i=0;i<size;++i)
		{
			newData[i] = data[i];
		}
		
		data = newData;
		this.rowSize = newSize;
	}
	
	public void set(int row, int col, boolean value)
	{
		assert(row<size);
		assert(col<size);
		
		if(value)
			data[row].set(col);
		else
			data[row].clear(col);
	}
	
	public boolean get(int row, int col)
	{
		return data[row].get(col);
	}
	
	public void split(int oldBlockIndex, int newBlockIndex)
	{
		assert(oldBlockIndex < newBlockIndex);
		assert(newBlockIndex == this.size);
		
		if(this.size == this.rowSize)
		{
			this.resize(2*rowSize);
		}
		
		
		for(int r=0;r<size;++r)
		{
			if(data[r].get(oldBlockIndex))
				data[r].set(newBlockIndex);
		}
		
		data[size] = (BitSet) data[oldBlockIndex].clone();
		data[size].set(size);
		
		++this.size;
	}
	
	public int first(int row)
	{
		return data[row].nextSetBit(0);
	}
	
	public int next(int row, int col)
	{
		return data[row].nextSetBit(col+1);
	}
}
