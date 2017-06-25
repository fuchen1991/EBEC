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

import cn.ac.ios.common.ReturnValue;
import cn.ac.ios.common.StateSetPair;
import cn.ac.ios.myqueue.MyQueue;
import cn.ac.ios.nfa.NFAWithTwoSets;

public class HK {

	private NFAWithTwoSets nfaWithTwoSets;
	private MyQueue<StateSetPair<BitSet>> todo;
	
	public HK(NFAWithTwoSets nfaUnion, MyQueue<StateSetPair<BitSet>> todo)
	{
		this.nfaWithTwoSets = nfaUnion;
		this.todo = todo;
	}
	
	public ReturnValue checkEquiv()
	{
		todo.clear();
		Dsf r = new Dsf();
		todo.push(new StateSetPair<BitSet>(nfaWithTwoSets.getStateSet_1(),nfaWithTwoSets.getStateSet_2()));
		
		int tp = 1;
		int rp = 0;
		int ep = 1;
		int actionNum = nfaWithTwoSets.getActionCount();
		
		while(!todo.isEmpty())
		{
			StateSetPair<BitSet> p = todo.pop();
			BitSet x = p.getX();
			BitSet y = p.getY();
			
			//check output
			if(nfaWithTwoSets.isAccept(x) != nfaWithTwoSets.isAccept(y))
			{
				return new ReturnValue(false,ep,tp,rp);
			}
			
			//check the disjoint sets forests
			if(r.unify(x, y))
				continue;
			else
			{
				rp++;
				for(int i=0;i<actionNum;i++)
				{
					ep++;
					StateSetPair<BitSet> pair = new StateSetPair<BitSet>(nfaWithTwoSets.getDstStateSetOfASet(x, i),nfaWithTwoSets.getDstStateSetOfASet(y, i));
					todo.push(pair);
					tp++;
				}
			}
		}
		
		return new ReturnValue(true,ep,tp,rp);
	}
	
	public ReturnValue checkEquivMem()
	{
		todo.clear();
		Dsf r = new Dsf();
		todo.push(new StateSetPair<BitSet>(nfaWithTwoSets.getStateSet_1(),nfaWithTwoSets.getStateSet_2()));
		
		int tp = 1;
		int rp = 0;
		int ep = 1;
		int actionNum = nfaWithTwoSets.getActionCount();
		
		while(!todo.isEmpty())
		{
			StateSetPair<BitSet> p = todo.pop();
			BitSet x = p.getX();
			BitSet y = p.getY();
			
			//check output
			if(nfaWithTwoSets.isAccept(x) != nfaWithTwoSets.isAccept(y))
			{
				return new ReturnValue(false,ep,tp,rp);
			}
			
			//check the disjoint sets forests
			if(r.unify(x, y))
				continue;
			else
			{
				rp++;
				for(int i=0;i<actionNum;i++)
				{
					ep++;
					StateSetPair<BitSet> pair = new StateSetPair<BitSet>(nfaWithTwoSets.getDstStateSetOfASet(x, i),nfaWithTwoSets.getDstStateSetOfASet(y, i));
					if(!todo.contains(pair))
					{
						todo.push(pair);
						tp++;
					}
				}
			}
		}
		
		return new ReturnValue(true,ep,tp,rp);
	}
	

}
