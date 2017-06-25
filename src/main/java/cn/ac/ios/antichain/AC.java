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

import java.util.BitSet;

import cn.ac.ios.common.ReturnValue;
import cn.ac.ios.myqueue.MyQueue;
import cn.ac.ios.nfa.NFAWithTwoSets;

public class AC {

	private NFAWithTwoSets nfaWithTwoSets;
	private MyQueue<AntichainPair> todo;
	
	public AC(NFAWithTwoSets nfaUnion, MyQueue<AntichainPair> todo)
	{
		this.nfaWithTwoSets = nfaUnion;
		this.todo = todo;
	}
	
	public ReturnValue checkIncl()
	{
		todo.clear();
		Antichain antichain = new Antichain();

		int ep = 0;
		int tp = 0;
		int rp = 0;
		int actionNum = nfaWithTwoSets.getActionCount();
		
		BitSet initial_1 = nfaWithTwoSets.getStateSet_1();
		BitSet initial_2 = nfaWithTwoSets.getStateSet_2();
		
		for(int i=initial_1.nextSetBit(0);i>=0;i=initial_1.nextSetBit(i+1))
		{
			todo.push(new AntichainPair(i,initial_2));
			ep++;
			tp++;
		}
		while(!todo.isEmpty())
		{
			AntichainPair p = todo.pop();
			int x = p.getX();
			BitSet y = p.getY();
			
			if(!isGoodState(x,y))
			{
				return new ReturnValue(false,ep,tp,rp);
			}
			
			if(antichain.unify_by_map(x, y))
			{
				continue;
			}
			else
			{
				rp++;
				for(int i=0;i<actionNum;i++)
				{
					BitSet set = nfaWithTwoSets.getDstStateSetOfAState(x, i);
					if(!set.isEmpty())
					{
						BitSet t = nfaWithTwoSets.getDstStateSetOfASet(y, i);
						for(int j=set.nextSetBit(0);j>=0;j=set.nextSetBit(j+1))
						{
							ep++;
							AntichainPair a = new AntichainPair(j,t);
							tp++;
							todo.push(a);
						}
						
					}
				}
			}
		}

		return new ReturnValue(true,ep,tp,rp);
	}
	
	public ReturnValue checkInclMem()
	{
		todo.clear();
		Antichain antichain = new Antichain();

		int ep = 0;
		int tp = 0;
		int rp = 0;
		int actionNum = nfaWithTwoSets.getActionCount();
		
		BitSet initial_1 = nfaWithTwoSets.getStateSet_1();
		BitSet initial_2 = nfaWithTwoSets.getStateSet_2();
		for(int i=initial_1.nextSetBit(0);i>=0;i=initial_1.nextSetBit(i+1))
		{
			todo.push(new AntichainPair(i,initial_2));
			ep++;
			tp++;
		}
		while(!todo.isEmpty())
		{
			AntichainPair p = todo.pop();
			int x = p.getX();
			BitSet y = p.getY();
			
			if(!isGoodState(x,y))
			{
				return new ReturnValue(false,ep,tp,rp);
			}
			
			if(antichain.unify_by_map(x, y))
			{
				continue;
			}
			else
			{
				rp++;
				for(int i=0;i<actionNum;i++)
				{
					BitSet set = nfaWithTwoSets.getDstStateSetOfAState(x, i);
					if(!set.isEmpty())
					{
						BitSet t = nfaWithTwoSets.getDstStateSetOfASet(y, i);
						for(int j=set.nextSetBit(0);j>=0;j=set.nextSetBit(j+1))
						{
							ep++;
							AntichainPair a = new AntichainPair(j,t);
							if(!todo.contains(a))
							{
								tp++;
								todo.push(a);
							}
						}
						
					}
				}
			}
		}

		return new ReturnValue(true,ep,tp,rp);
	}
	
	public ReturnValue checkInclMemWithRelation(BitSet[] simulate, BitSet[] beSimulated)
	{
		todo.clear();
		AntichainWithRelation antichain = new AntichainWithRelation(simulate, beSimulated);

		int ep = 0;
		int tp = 0;
		int rp = 0;
		int actionNum = nfaWithTwoSets.getActionCount();
		
		BitSet initial_1 = nfaWithTwoSets.getStateSet_1();
		BitSet initial_2 = nfaWithTwoSets.getStateSet_2();
		//Optimization 2
		BitSet initial_2_min = minimize(initial_2,simulate);
		for(int i=initial_1.nextSetBit(0);i>=0;i=initial_1.nextSetBit(i+1))
		{
			todo.push(new AntichainPair(i,initial_2_min));
			ep++;
			tp++;
		}
		while(!todo.isEmpty())
		{
			AntichainPair p = todo.pop();
			int x = p.getX();
			BitSet y = p.getY();
			
			if(!isGoodState(x,y))
			{
				return new ReturnValue(false,ep,tp,rp);
			}
			
			if(antichain.unify_by_map_with_relation(x, y))
			{
				continue;
			}
			else
			{
				rp++;
				for(int i=0;i<actionNum;i++)
				{
					BitSet set = nfaWithTwoSets.getDstStateSetOfAState(x, i);
					if(!set.isEmpty())
					{
						//Optimization 2
						BitSet t = minimize(nfaWithTwoSets.getDstStateSetOfASet(y, i),simulate);
						for(int j=set.nextSetBit(0);j>=0;j=set.nextSetBit(j+1))
						{
							ep++;
							AntichainPair a = new AntichainPair(j,t);
							if(!todo.contains(a))
							{
								tp++;
								todo.push(a);
							}
						}
						
					}
				}
			}
		}

		return new ReturnValue(true,ep,tp,rp);
	}
	
	private boolean isGoodState(int x, BitSet y)
	{
		BitSet acc = nfaWithTwoSets.getAcceptStateSet();
		if(!acc.get(x))
			return true;
		
		for(int i=y.nextSetBit(0);i>=0;i=y.nextSetBit(i+1))
		{
			if(acc.get(i))
				return true;
		}
		
		return false;
		
//		BitSet y_ = (BitSet) y.clone();
//		y_.and(nfaWithTwoSets.getAcceptStateSet());
//		return !y_.isEmpty();
	}
	
	private BitSet minimize(BitSet x, BitSet[] sim)
	{
		BitSet m = (BitSet) x.clone();
		
		for(int i=m.nextSetBit(0);i>=0;i=m.nextSetBit(i+1))
		{
			for(int j=m.nextSetBit(i+1);j>=0;j=m.nextSetBit(j+1))
			{
				if(sim[i].get(j))//i simulate j
				{
					m.clear(j);
					continue;
				}
				else if(sim[j].get(i))
				{
					m.clear(i);
					break;
				}
					
			}
		}
		
		return m;
	}
	
}
