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
package cn.ac.ios.hk;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class Dsf {
	private Map<BitSet,BitSet> map;
	
	public Dsf()
	{
		map = new HashMap<BitSet,BitSet>();
	}
	
	public boolean unify(BitSet x, BitSet y)
	{
		BitSet x_ = getParent(x);
		BitSet y_ = getParent(y);
		if(x_.equals(y_))
			return true;
		else
		{
			map.put(x_, y_);
			return false;
		}
	}
	private BitSet getParent(BitSet x)
	{
		BitSet tmp = map.get(x);
		if(tmp ==null)
			return x;
		else
		{
			BitSet tmp2 = map.get(tmp);
			if(tmp2==null)
				return tmp;
			else
			{
				map.put(x, tmp2);
				return getParent(tmp2);
			}
		}
	}
}
