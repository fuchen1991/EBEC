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

public class BinaryRelation {
	//size of blocks
	int size;
	//size of row of the matrix, usually larger than size (can be equal)
	private int rowSize;
	private boolean[] data;
	
	public BinaryRelation(int size)
	{
		this.size = size;
		rowSize = 2*size;
		
		data = new boolean[rowSize*rowSize];
	}
	
	//the default value is false
	public void resize(int newSize)
	{
		assert(rowSize < newSize);
		
		boolean[] tmp = new boolean[newSize * newSize];
		int src = 0;
		int dst = 0;
		for(int i=0;i<size;++i)
		{
			System.arraycopy(data, src, tmp, dst, size);
			src += rowSize;
			dst += newSize;
		}
		
		data = tmp;
		rowSize = newSize;
	}
	
	public void set(int row, int col, boolean value)
	{
		assert(row<size);
		assert(col<size);
		
		data[row*rowSize+col] = value;
	}
	
	public boolean get(int row, int col)
	{
		return data[row*rowSize+col];
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
			data[r*rowSize + newBlockIndex] = data[r*rowSize + oldBlockIndex];
		}
		
		System.arraycopy(data, oldBlockIndex*rowSize, data, size*rowSize, size);
		
		data[size*rowSize + size] = true;
		
		++this.size;
	}
}
