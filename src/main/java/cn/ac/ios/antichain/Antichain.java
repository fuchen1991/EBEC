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
package cn.ac.ios.antichain;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Antichain {
	
	private List<AntichainPair> r;
	private Map<Integer,List<BitSet>> map_r;
	
	public Antichain()
	{
		r = new ArrayList<AntichainPair>();
		map_r = new HashMap<Integer, List<BitSet>>();
	}
	
	public boolean unify(int x, BitSet y)
	{
		for(int i=0;i<r.size();i++)
		{
			if(r.get(i).getX()==x)
			{
				if(isSubset(r.get(i).getY(), y))
					return true;
				if(isSubset(y,r.get(i).getY()))
				{
					r.set(i,new AntichainPair(x,y));
					return false;
				}
			}
		}
		r.add(new AntichainPair(x,y));
		return false;
	}
	
	boolean unify_by_map(int x, BitSet y)
	{
		//////////////a small optimization 
		if(y.get(x))
			return true;
		
		List<BitSet> list = map_r.get(x);
		if(list==null)
		{
			List<BitSet> l = new ArrayList<BitSet>();
			l.add(y);
			map_r.put(x, l);
			return false;
		}
		else
		{
			for(int i=0;i<list.size();i++)
			{
				if(isSubset(list.get(i),y))
				{
					return true;
				}
				else if(isSubset(y,list.get(i)))
				{
					list.set(i, y);
					++i;
					while(i<list.size())
					{
						if(isSubset(y,list.get(i)))
						{
							list.remove(i);
						}
						else
						{
							++i;
						}
					}
					//actually, we do not need this line
					//map_r.put(x, list);
					return false;
				}
			}
			list.add(y);
			//map_r.put(x, list);
			return false;
		}
	}
	
//	private boolean isSubset(BitSet x_, BitSet y_)
//	{
//		BitSet x = (BitSet) x_.clone();
//		x.or(y_);
//		if(x.equals(y_))
//			return true;
//		return false;
//	}
	
	private boolean isSubset(BitSet x_, BitSet y_)
	{
		for(int i=x_.nextSetBit(0);i>=0;i=x_.nextSetBit(i+1))
		{
			if(!y_.get(i))
				return false;
		}
		return true;
	}
}
