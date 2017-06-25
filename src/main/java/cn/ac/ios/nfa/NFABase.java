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

import java.util.Arrays;
import java.util.BitSet;

public class NFABase {
	protected int stateCount;
	protected int actionCount;
	protected BitSet[][] transitionArray;
	protected BitSet acceptStateSet;

	public NFABase(){}
	
	public NFABase(int stateCount, int actionCount) {
		this.stateCount = stateCount;
		this.actionCount = actionCount;
		transitionArray = new BitSet[stateCount][actionCount];
		for(int i=0;i<stateCount;i++)
			for(int j=0;j<actionCount;j++)
				transitionArray[i][j] = new BitSet(stateCount);
	}
	
	public NFABase reverse()
	{
		NFABase nfa = new NFABase(stateCount, actionCount);
		BitSet tr[][] = new BitSet[stateCount][actionCount];
		for(int i=0;i<stateCount;i++)
		{
			for(int j=0;j<actionCount;j++)
			{
				tr[i][j] = new BitSet(stateCount);
			}
		}
		
		for(int i=0;i<stateCount;i++)
		{
			for(int j=0;j<actionCount;j++)
			{
				for(int k=transitionArray[i][j].nextSetBit(0);k>=0;k=transitionArray[i][j].nextSetBit(k+1))
				{
					tr[k][j].set(i);
				}
			}
		}
		
		nfa.setTransitionArray(tr);
		return nfa;
	}
	
	public void print()
	{
		System.out.println("state count == " + this.stateCount);
		System.out.println("action count == " + this.actionCount);
		
		System.out.println("accepting states: " + this.getAcceptStateSet());
		
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
	
	public BitSet getDstStateSetOfASet(BitSet bs, int action)
	{
		BitSet bitSet = new BitSet(stateCount);
		for(int i=bs.nextSetBit(0);i>=0;i=bs.nextSetBit(i+1))
		{
			bitSet.or(transitionArray[i][action]);
		}
		return bitSet;
	}

	public BitSet getDstStateSetOfAState(int state, int action)
	{
		return transitionArray[state][action];
	}
	
	public int getStateCount() {
		return stateCount;
	}

	public int getActionCount() {
		return actionCount;
	}

	public BitSet[][] getTransitionArray() {
		return transitionArray;
	}

	public void setTransitionArray(BitSet[][] transitionArray) {
		this.transitionArray = transitionArray;
	}

	public BitSet getAcceptStateSet() {
		return acceptStateSet;
	}

	public void setAcceptStateSet(BitSet acceptStateSet) {
		this.acceptStateSet = acceptStateSet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((acceptStateSet == null) ? 0 : acceptStateSet.hashCode());
		result = prime * result + actionCount;
		result = prime * result + stateCount;
		result = prime * result + Arrays.deepHashCode(transitionArray);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NFABase other = (NFABase) obj;
		if (acceptStateSet == null) {
			if (other.acceptStateSet != null)
				return false;
		} else if (!acceptStateSet.equals(other.acceptStateSet))
			return false;
		if (actionCount != other.actionCount)
			return false;
		if (stateCount != other.stateCount)
			return false;
		if (!Arrays.deepEquals(transitionArray, other.transitionArray))
			return false;
		return true;
	}


}
