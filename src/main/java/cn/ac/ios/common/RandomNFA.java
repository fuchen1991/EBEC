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
package cn.ac.ios.common;

import java.util.BitSet;
import java.util.Random;

import cn.ac.ios.nfa.NFA;
import cn.ac.ios.nfa.NFAWithTwoSets;

public class RandomNFA {
	Random rand = new Random();
	
	public NFA generateRandomNFA(int stateNum, int actionNum, double density, double accPro)
	{
		double d = density / stateNum;
		NFA nfa = new NFA(stateNum,actionNum);
		BitSet init = new BitSet();
		init.set(1);
		nfa.setInitialStateSet(init);
		BitSet[][] tran = new BitSet[stateNum][actionNum];
		for(int i=0;i<stateNum;i++)
		{
			for(int j=0;j<actionNum;j++)
			{
				tran[i][j] = randomBitSet(stateNum,d);
			}
		}
		nfa.setTransitionArray(tran);
		nfa.setAcceptStateSet(randomBitSet(stateNum,accPro));
		
		return nfa;
	}
	
	public NFAWithTwoSets generateRandomNFAWithTwoSets(int stateNum, int actionNum, double density, double accPro)
	{
		double d = density / stateNum;
		NFAWithTwoSets nfa = new NFAWithTwoSets(stateNum,actionNum);
		BitSet init1 = new BitSet();
		init1.set(1);
		BitSet init2 = new BitSet();
		init2.set(2);
		nfa.setStateSet_1(init1);
		nfa.setStateSet_2(init2);
		
		BitSet[][] tran = new BitSet[stateNum][actionNum];
		for(int i=0;i<stateNum;i++)
		{
			for(int j=0;j<actionNum;j++)
			{
				tran[i][j] = randomBitSet(stateNum,d);
			}
		}
		nfa.setTransitionArray(tran);
		nfa.setAcceptStateSet(randomBitSet(stateNum,accPro));
		
		return nfa;
	}
	
	
	private BitSet randomBitSet(int size, double d)
	{
		BitSet set = new BitSet(size);
		for(int i=0;i<size;i++)
		{
			if(d > rand.nextDouble())
			{
				set.set(i);
			}
		}
		
		return set;
	}
	
	public NFA generateRandomNFA_second(int stateNum, int actionNum, int transitionNum, double accPro)
	{
		NFA nfa = new NFA(stateNum,actionNum);
		BitSet init = new BitSet();
		init.set(1);
		nfa.setInitialStateSet(init);
		BitSet[][] tran = new BitSet[stateNum][actionNum];
		for(int i=0;i<stateNum;i++)
		{
			for(int j=0;j<actionNum;j++)
			{
				tran[i][j] = new BitSet();
			}
		}
		
		for(int i=0;i<transitionNum;i++)
		{
			int src = rand.nextInt(stateNum);
			int action = rand.nextInt(actionNum);
			int dst = rand.nextInt(stateNum);
			
			if(tran[src][action].get(dst))
			{
				i--;
				continue;
			}
			else
			{
				tran[src][action].set(dst);
			}
			
		}
		
		nfa.setTransitionArray(tran);
		nfa.setAcceptStateSet(randomBitSet(stateNum,accPro));
		
		return nfa;
	}
}
