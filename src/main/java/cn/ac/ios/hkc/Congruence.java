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
package cn.ac.ios.hkc;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import cn.ac.ios.common.StateSetPair;
import cn.ac.ios.myqueue.MyQueue;

public class Congruence {
	List<StateSetPair<BitSet>> rules;
	
	public Congruence()
	{
		rules = new ArrayList<StateSetPair<BitSet>>();
	}
	
	public boolean unify(BitSet x, BitSet y, MyQueue<StateSetPair<BitSet>> todo)
	{
		List<StateSetPair<BitSet>> r1 = new ArrayList<StateSetPair<BitSet>>(rules);
		List<StateSetPair<BitSet>> r2 = new ArrayList<StateSetPair<BitSet>>(rules);
		List<StateSetPair<BitSet>> t = new ArrayList<StateSetPair<BitSet>>(todo.getCollection());
		
		BitSet yNorm = getNormalForm(x,y,t,r1);
		BitSet xNorm = getNormalForm(y,x,t,r2);
		if(yNorm == null)
		{
			if(xNorm == null)
			{
				return true;
			}
			else
			{
				xNorm.or(y);
				xNorm = getNorm(r2,xNorm);
				this.rules.add(0,new StateSetPair<BitSet>(x,xNorm));
				return false;
			}
		}
		else
		{
			if(xNorm == null)
			{
				yNorm.or(x);
				yNorm = getNorm(r1,yNorm);
				this.rules.add(0,new StateSetPair<BitSet>(y,yNorm));
				return false;
			}
			else
			{
				xNorm.or(yNorm);
				xNorm = getNorm(r2,xNorm);
				this.rules.add(0,new StateSetPair<BitSet>(x,xNorm));
				this.rules.add(0,new StateSetPair<BitSet>(y,xNorm));
				return false;
			}
		}
	}

	private BitSet passRules(List<StateSetPair<BitSet>> rules_, BitSet x_)
	{
		BitSet x = (BitSet) x_.clone();
		int i=0;
		while(i<rules_.size())
		{
			if(isSubset(rules_.get(i).getX(),x))
			{
				x.or(rules_.get(i).getY());
				rules_.remove(i);
			}
			else
			{
				i++;
			}
		}
		
		return x;
	}
	
	private BitSet passTodo(List<StateSetPair<BitSet>> todo_, BitSet x_)
	{
		BitSet x = (BitSet) x_.clone();
		int i=0;
		while(i<todo_.size())
		{
			if(isSubset(todo_.get(i).getX(),x))
			{
				x.or(todo_.get(i).getY());
				todo_.remove(i);
			}
			else if(isSubset(todo_.get(i).getY(),x))
			{
				x.or(todo_.get(i).getX());
				todo_.remove(i);
			}
			else
			{
				i++;
			}
		}
		return x;
	}
	
	private BitSet getNorm(List<StateSetPair<BitSet>> rules_, BitSet x_)
	{
		BitSet x = passRules(rules_, x_);
		if(x.equals(x_))
			return x;
		else
			return getNorm(rules_,x);
	}
	
	private BitSet getNormalForm_with_r(List<StateSetPair<BitSet>> rules_, BitSet x_, BitSet y_)
	{
		if(isSubset(x_,y_))
			return null;
		else
		{
			BitSet y = passRules(rules_, y_);
			if(y_.equals(y))
				return y;
			else
				return getNormalForm_with_r(rules_,x_,y);
		}
	}
	
	private boolean checkSubset_with_todo(BitSet x_, BitSet y_,List<StateSetPair<BitSet>> todo_, List<StateSetPair<BitSet>> rules_)
	{
		if(isSubset(x_,y_))
		{
			return true;
		}
		BitSet y = passTodo(todo_,y_);
		y = passRules(rules_,y);
		if(y.equals(y_))
			return false;
		else
			return checkSubset_with_todo(x_,y,todo_,rules_);
	}

	private BitSet getNormalForm(BitSet x_, BitSet y_, List<StateSetPair<BitSet>> todo_, List<StateSetPair<BitSet>> rules_)
	{
		BitSet norm = getNormalForm_with_r(rules_, x_, y_);
		if(norm == null)
		{
			return null;
		}
		else
		{
			List<StateSetPair<BitSet>> todo = new ArrayList<StateSetPair<BitSet>>(todo_);
			if(checkSubset_with_todo(x_,y_,todo,rules_))
			{
				return null;
			}
			else
			{
				return norm;
			}
		}
	}
	
	private boolean isSubset(BitSet x_, BitSet y_)
	{
		for(int i=x_.nextSetBit(0);i>=0;i=x_.nextSetBit(i+1))
		{
			if(!y_.get(i))
				return false;
		}
		return true;
	}
	
//	private boolean isSubset(BitSet x_, BitSet y_)
//	{
//		BitSet x = (BitSet) x_.clone();
//		x.or(y_);
//		if(x.equals(y_))
//			return true;
//		return false;
//	}
}
