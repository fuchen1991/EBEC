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
package cn.ac.ios.nfa;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class NFA extends NFABase{
	private BitSet initialStateSet;

	public NFA(){}
	
	public NFA(int stateCount, int actionCount) {
		super(stateCount,actionCount);
	}
	
	public NFA removeUselessState()
	{
		boolean[] access_from_initial = this.accessFromInital();
		boolean[] access_from_accept = this.reverse().accessFromInital();
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		int newCount =0;
		int currMinus = 0;
		for(int i=0;i<this.stateCount;i++)
		{
			if(access_from_initial[i] && access_from_accept[i])
			{
				map.put(i, i-currMinus);
				newCount++;
			}
			else
			{
				currMinus++;
			}
		}
		NFA nfa = new NFA(newCount, this.actionCount);
		
		for(int i=0;i<stateCount;i++)
		{
			if(access_from_initial[i] && access_from_accept[i])
			{
				for(int j=0;j<actionCount;j++)
				{
					for(int k = transitionArray[i][j].nextSetBit(0);k>=0;k=transitionArray[i][j].nextSetBit(k+1))
					{
						if(access_from_initial[k] && access_from_accept[k])
						{
							nfa.transitionArray[map.get(i)][j].set(map.get(k));
						}
					}
				}
			}
		}
		BitSet initial = new BitSet();
		for(int i=this.initialStateSet.nextSetBit(0);i>=0;i=this.initialStateSet.nextSetBit(i+1))
		{
			if(access_from_initial[i] && access_from_accept[i])
			{
				initial.set(map.get(i));
			}
		}
		nfa.setInitialStateSet(initial);
		
		BitSet accept = new BitSet();
		for(int i=this.acceptStateSet.nextSetBit(0);i>=0;i=this.acceptStateSet.nextSetBit(i+1))
		{
			if(access_from_initial[i] && access_from_accept[i])
			{
				accept.set(map.get(i));
			}
		}
		nfa.setAcceptStateSet(accept);
		
		return nfa;
	}
	
	public NFA removeUselessAction()
	{
		int newActionCount = 0;
		boolean[] isUseful = new boolean[actionCount];
		for(int i=0;i<this.actionCount;i++)
		{
			for(int j=0;j<this.stateCount;j++)
			{
				if(!this.transitionArray[j][i].isEmpty())
				{
					isUseful[i] = true;
					newActionCount++;
					break;
				}
			}
		}
		
		NFA nfa = new NFA(stateCount,newActionCount);
		nfa.acceptStateSet = this.acceptStateSet;
		nfa.initialStateSet = this.initialStateSet;
		
		int k=0;
		for(int i=0;i<this.actionCount;i++)
		{
			if(isUseful[i])
			{
				for(int j=0;j<this.stateCount;j++)
				{
					nfa.transitionArray[j][k] = this.transitionArray[j][i];
				}
				k++;
			}
		}
			
		return nfa;
	}
	
	public NFA reverse()
	{
		NFA nfa = new NFA(stateCount, actionCount);
		nfa.setAcceptStateSet(initialStateSet);
		nfa.setInitialStateSet(acceptStateSet);
		for(int i=0;i<stateCount;i++)
		{
			for(int j=0;j<actionCount;j++)
			{
				for(int k=transitionArray[i][j].nextSetBit(0);k>=0;k=transitionArray[i][j].nextSetBit(k+1))
				{
					nfa.transitionArray[k][j].set(i);
				}
			}
		}
		
		return nfa;
	}
	
	public boolean[] accessFromInital()
	{
		boolean[] access_from_initial = new boolean[stateCount];
		Stack<Integer> stack = new Stack<Integer>();
		for(int i=initialStateSet.nextSetBit(0);i>=0;i=initialStateSet.nextSetBit(i+1))
		{
			stack.push(i);
			access_from_initial[i] = true;
		}
		while(!stack.isEmpty())
		{
			int s = stack.pop();
			for(int i=0;i<actionCount;i++)
			{
				for(int j=transitionArray[s][i].nextSetBit(0);j>=0;j=transitionArray[s][i].nextSetBit(j+1))
				{
					if(!access_from_initial[j])
					{
						access_from_initial[j]=true;
						if(stack.search(j) == -1)
							stack.push(j);
					}
				}
			}
		}
		return access_from_initial;
	}

	public BitSet getInitialStateSet() {
		return initialStateSet;
	}

	public void setInitialStateSet(BitSet initialStateSet) {
		this.initialStateSet = initialStateSet;
	}
	
	
}
