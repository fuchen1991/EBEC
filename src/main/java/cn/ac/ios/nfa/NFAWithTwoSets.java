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

public class NFAWithTwoSets extends NFABase {
	private BitSet stateSet_1;
	private BitSet stateSet_2;
	
	public NFAWithTwoSets() {}
	
	public NFAWithTwoSets(int stateCount, int actionCount)
	{
		super(stateCount,actionCount);
	}
	
	public NFAWithTwoSets(NFAWithTwoSets union)
	{
		super(union.stateCount,union.actionCount);
		this.acceptStateSet = (BitSet) union.acceptStateSet.clone();
		this.stateSet_1 = (BitSet) union.stateSet_1.clone();
		this.stateSet_2 = (BitSet) union.stateSet_2.clone();
		
		this.transitionArray = new BitSet[stateCount][actionCount];
		for(int i=0;i<stateCount;i++)
		{
			for(int j=0;j<actionCount;j++)
			{
				transitionArray[i][j] = (BitSet) union.getDstStateSetOfAState(i, j).clone();
			}
		}
	}
	
	public NFAWithTwoSets(NFA nfa1, NFA nfa2)
	{
		this.stateCount = nfa1.getStateCount() + nfa2.getStateCount();
		this.actionCount = Math.max(nfa1.getActionCount(), nfa2.getActionCount());
		this.acceptStateSet = (BitSet) nfa1.getAcceptStateSet().clone();
		this.stateSet_1 = (BitSet) nfa1.getInitialStateSet().clone();
		
		int size = nfa1.getStateCount();
		this.acceptStateSet.or(shiftSet(nfa2.getAcceptStateSet(),size));
		this.stateSet_2 = shiftSet(nfa2.getInitialStateSet(),size);
		
		this.transitionArray = new BitSet[stateCount][actionCount];
		for(int i=0;i<size;i++)
		{
			for(int j=0;j<nfa1.getActionCount();j++)
			{
				transitionArray[i][j] = (BitSet) nfa1.getDstStateSetOfAState(i, j).clone();
			}
		}
		for(int i=0;i<nfa2.getStateCount();i++)
		{
			for(int j=0;j<nfa2.getActionCount();j++)
			{
				transitionArray[i+size][j] = shiftSet(nfa2.getDstStateSetOfAState(i, j),size);
			}
		}
	}
	
	public void print()
	{
		System.out.println("state count == " + this.stateCount);
		System.out.println("action count == " + this.actionCount);
		
		System.out.println("accepting states: " + this.getAcceptStateSet());
		System.out.println("initial states: " + this.stateSet_1);
		System.out.println("initial states: " + this.stateSet_2);
		
		System.out.println("transitions: ");
		for(int i=0;i<this.stateCount;i++)
		{
			for(int j=0;j<this.actionCount;j++)
			{
				if(!this.transitionArray[i][j].isEmpty())
					System.out.println(i + " -> " + j + " -> " + this.transitionArray[i][j]);
			}
		}
	}
	
	public boolean isAccept(BitSet x_)
	{
		BitSet x = (BitSet) x_.clone();
		x.and(this.acceptStateSet);
		return !x.isEmpty();
	}
	
	//shift every element i in set to i+n
	private BitSet shiftSet(BitSet set, int n)
	{
		BitSet r = new BitSet();
		if(set!=null)
		{
			for(int i=set.nextSetBit(0);i>=0;i=set.nextSetBit(i+1))
			{
				r.set(i+n);
			}
		}
		return r;
	}
	
	public NFAWithTwoSets removeUselessAction()
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
		
		NFAWithTwoSets nfaUnion = new NFAWithTwoSets(stateCount,newActionCount);
		nfaUnion.acceptStateSet = this.acceptStateSet;
		nfaUnion.stateSet_1 = this.stateSet_1;
		nfaUnion.stateSet_2 = this.stateSet_2;
		
		int k=0;
		for(int i=0;i<this.actionCount;i++)
		{
			if(isUseful[i])
			{
				for(int j=0;j<this.stateCount;j++)
				{
					nfaUnion.transitionArray[j][k] = this.transitionArray[j][i];
				}
				k++;
			}
		}
			
		return nfaUnion;
	}
	
	public BitSet getStateSet_1() {
		return stateSet_1;
	}

	public void setStateSet_1(BitSet stateSet_1) {
		this.stateSet_1 = stateSet_1;
	}

	public BitSet getStateSet_2() {
		return stateSet_2;
	}

	public void setStateSet_2(BitSet stateSet_2) {
		this.stateSet_2 = stateSet_2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((stateSet_1 == null) ? 0 : stateSet_1.hashCode());
		result = prime * result + ((stateSet_2 == null) ? 0 : stateSet_2.hashCode());
		result = prime * result + super.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NFAWithTwoSets other = (NFAWithTwoSets) obj;
		if (stateSet_1 == null) {
			if (other.stateSet_1 != null)
				return false;
		} else if (!stateSet_1.equals(other.stateSet_1))
			return false;
		if (stateSet_2 == null) {
			if (other.stateSet_2 != null)
				return false;
		} else if (!stateSet_2.equals(other.stateSet_2))
			return false;
		return super.equals(other);
	}

	
}
