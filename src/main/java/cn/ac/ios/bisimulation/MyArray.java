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
package cn.ac.ios.bisimulation;

public class MyArray
{
	private int max1;
	private int free;
	private int[] elems;
	
	public MyArray()
	{
		this.max1 = 0;
		this.free = 0;
	}
	  
	public final void close()
	{
		del();
	}
	
	public final void del()
	{
		elems = null;
		max1 = 0;
		free = 0;
	}

	public void make_size(int ms)
	{
		max1 = ms + 1;
		elems = new int[max1];
	}

	public boolean active()
	{
		return elems != null;
	}
	
	public int getItem(int idx)
	{
		if(idx>=max1)
			System.out.println("Illeagal index");
		
		return elems[idx];
	}

	public void setItem(int idx, int value)
	{
		if(idx>=max1)
			System.out.println("Illeagal index");
		
		elems[idx] = value;
	}

	public void plusOne(int idx)
	{
		if(idx>=max1)
			System.out.println("Illeagal index");
		
		++elems[idx];
	}
	
	public void minus(int idx, int value)
	{
		if(idx>=max1)
			System.out.println("Illeagal index");
		
		elems[idx] -= value;
	}
	
	public final int stack_size()
	{
		return free;
	}
	
	public final void add(int el)
	{
		if (free >= max1)
		{
			System.out.println("Full stack");
		}
	  
		elems[free] = el;
		++free;
	}
  
  
	public final int remove()
	{
		if (free == 0)
		{
			System.out.println("Empty stack");
		}
		--free;
		return elems[free];
	}
  
	public final void clear()
	{
		free = 0;
	}
}

