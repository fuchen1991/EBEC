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

public class RefinablePartition {
	public int nr_sets;
	public MyArray elems = new MyArray();
	public MyArray locs = new MyArray();
	public MyArray sets = new MyArray();
	public MyArray begins = new MyArray();
	public MyArray ends = new MyArray();
	public MyArray mids = new MyArray();
	
	
	public RefinablePartition()
	{
		this.nr_sets = 0;
	}
	
	public int mark(int el)
	{
		int ss = sets.getItem(el);
		int lc = locs.getItem(el);
		int m_lc = mids.getItem(ss);
		mids.setItem(ss, m_lc + 1);
		elems.setItem(lc, elems.getItem(m_lc));
		locs.setItem(elems.getItem(lc), lc);
		elems.setItem(m_lc, el);
		locs.setItem(el, m_lc);
		if(m_lc == begins.getItem(ss))
			return ss;
		else
			return 0;
	}
	
	public int split(int ss)
	{
		if(mids.getItem(ss) == ends.getItem(ss))
		{
			mids.setItem(ss, begins.getItem(ss));
		}
		if(mids.getItem(ss) == begins.getItem(ss))
		{
			return 0;
		}
		
		++ nr_sets;
		
		if(mids.getItem(ss) - begins.getItem(ss) <= ends.getItem(ss) - mids.getItem(ss))
		{
			begins.setItem(nr_sets, begins.getItem(ss));
			ends.setItem(nr_sets, mids.getItem(ss));
			begins.setItem(ss, mids.getItem(ss));
		}
		else
		{
			ends.setItem(nr_sets, ends.getItem(ss));
			begins.setItem(nr_sets, mids.getItem(ss));
			ends.setItem(ss, mids.getItem(ss));
			mids.setItem(ss, begins.getItem(ss));
			ss = nr_sets;
		}
		mids.setItem(nr_sets, begins.getItem(nr_sets));
		for(int lc = begins.getItem(nr_sets);lc < ends.getItem(nr_sets);++lc)
		{
			sets.setItem(elems.getItem(lc), nr_sets);
		}
		
		return ss;
	}

}
