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

public class AntichainWithRelation {
	private Map<Integer,List<BitSet>> r;
	private BitSet[] simulate;
	private BitSet[] beSimulated;
	
	public AntichainWithRelation(BitSet[] simulate, BitSet[] beSimulated)
	{
		r = new HashMap<Integer, List<BitSet>>();
		
		this.simulate = simulate;
		this.beSimulated = beSimulated;
	}
	
	boolean unify_by_map_with_relation(int x, BitSet y)
	{
		//optimization 1(b)
		for(int st=y.nextSetBit(0);st>=0;st=y.nextSetBit(st+1))
		{
			if(beSimulated[x].get(st))
				return true;
		}

		//optimization 1(a)
		for(int st=this.beSimulated[x].nextSetBit(0);st>=0;st=this.beSimulated[x].nextSetBit(st+1))
		{
			//st simulates x
			List<BitSet> list = r.get(st);
			if(list == null)
				continue;
			
			for(BitSet S : list)
			{
				if(isSimulated(S, y))
				{
					return true;
				}
			}
		}
		
		for(int st=this.simulate[x].nextSetBit(0);st>=0;st=this.simulate[x].nextSetBit(st+1))
		{
			//x simulates st
			List<BitSet> list = r.get(st);
			if(list == null)
				continue;
			
			for(int i=0;i<list.size();++i)
			{
				BitSet S = list.get(i);
				
				if(isSimulated(y, S))
				{
					list.remove(i);
					--i;
				}
			}
			
			//r.put(st, list);
		}
		List<BitSet> list = r.get(x);
		if(list == null)
		{
			list = new ArrayList<BitSet>();
			list.add(y);
			r.put(x, list);
		}
		
		list.add(y);
		//r.put(x, list);
		
		return false;
	}
	
	private boolean isSimulated(BitSet S, BitSet P)
	{
		for(int st=S.nextSetBit(0);st>=0;st=S.nextSetBit(st+1))
		{
			boolean done = false;
			for(int p=P.nextSetBit(0);p>=0;p=P.nextSetBit(p+1))
			{
				if(beSimulated[st].get(p))
				{
					done = true;
					break;
				}
			}
			if(!done)
			{
				return false;
			}
		}
		
		return true;
	}
}
