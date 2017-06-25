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
package cn.ac.ios.simulation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.ac.ios.nfa.NFABase;

/*algorithm proposed by M. Henzinger, T. Henzinger, P. Kopke 
 * in "Computing Simulations on Finite and Infinite Graphs",  in Proc. FOCS'95
*/
public class RefinedSimulation {

	private NFABase nfa;
	private BitSet[] sim;
	
	public RefinedSimulation(NFABase nfa)
	{
		this.nfa = nfa;
	}
	
	public void computeSimulation()
	{
		Set<Integer> indices = new HashSet<Integer>();
		
		int size = this.nfa.getStateCount();
		int actionCount = this.nfa.getActionCount();
		BitSet acc = this.nfa.getAcceptStateSet();
		BitSet[][] tran = this.nfa.getTransitionArray();
		NFABase reverseNFA = nfa.reverse();
		BitSet[][] tranReverse = reverseNFA.getTransitionArray();
		
		BitSet[] inActions = new BitSet[size];
		BitSet[] canDoA = new BitSet[actionCount];
		
		for(int j=0;j<actionCount;j++)
		{
			canDoA[j] = new BitSet();
		}
		
		for(int i=0;i<size;i++)
		{
			inActions[i] = new BitSet();
			for(int j=0;j<actionCount;j++)
			{
				if(!tranReverse[i][j].isEmpty())
				{
					inActions[i].set(j);
					
				}
				if(!tran[i][j].isEmpty())
				{
					canDoA[j].set(i);
				}
			}
		}
		
		BitSet[] prevsim = new BitSet[size];
		sim = new BitSet[size];

		for(int i=0;i<size;i++)
		{
			prevsim[i] = new BitSet(size);
			prevsim[i].set(0, size);
			
			sim[i] = new BitSet(size);
			
			if(acc.get(i))
			{
				sim[i] = (BitSet) acc.clone();
				indices.add(i);
			}
			else
			{
				sim[i].set(0, size);
			}
		}
		for(int j=0;j<actionCount;j++)
		{
			for(int v=canDoA[j].nextSetBit(0);v>=0;v=canDoA[j].nextSetBit(v+1))
			{
				if(!isSubset(sim[v],canDoA[j]))
				{
					sim[v].and(canDoA[j]);
					indices.add(v);
				}
			}
		}

		int index;
		while(!indices.isEmpty())
		{
			index = indices.iterator().next();
			indices.remove(index);
			
			BitSet tmp = (BitSet) sim[index].clone();
			for(int j=inActions[index].nextSetBit(0);j>=0;j=inActions[index].nextSetBit(j+1))
			{
				BitSet pre = tranReverse[index][j];
				
				BitSet remove = reverseNFA.getDstStateSetOfASet(prevsim[index], j);
				remove.andNot(reverseNFA.getDstStateSetOfASet(sim[index], j));
				
				for(int k=pre.nextSetBit(0);k>=0;k=pre.nextSetBit(k+1))
				{
					if(sim[k].intersects(remove))
					{
						indices.add(k);
						sim[k].andNot(remove);
					}
				}
			}
			
			prevsim[index] = tmp;
		}
	}
	
	public BitSet[] getRelation()
	{
		int n = this.nfa.getStateCount();
		
		BitSet[] set = new BitSet[n];
		for(int i=0;i<n;i++)
		{
			if(set[i]==null)
				set[i] = new BitSet();
			for(int j=0;j<n;j++)
			{
				if(sim[j].get(i))
				{
					set[i].set(j);
				}
			}
		}
		return set;
	}
	
	public BitSet[] compute2()//slow
	{
		int size = this.nfa.getStateCount();
		BitSet[] prevsim = new BitSet[size];
		BitSet[] sim = new BitSet[size];
		int actionCount = this.nfa.getActionCount();
		
		for(int i=0;i<size;i++)
		{
			prevsim[i] = new BitSet(size);
			prevsim[i].set(0, size);
			
			sim[i] = new BitSet(size);
			
			if(this.nfa.getAcceptStateSet().get(i))
			{
				sim[i] = (BitSet) this.nfa.getAcceptStateSet().clone();
			}
			else
			{
				sim[i].set(0, size);
			}
			
			for(int j=0;j<actionCount;j++)
			{
				if(!this.nfa.getTransitionArray()[i][j].isEmpty())
				{
					for(int k=sim[i].nextSetBit(0);k>=0;k=sim[i].nextSetBit(k+1))
					{
						if(this.nfa.getTransitionArray()[k][j].isEmpty())
						{
							sim[i].clear(k);
						}
					}
				}
			}
		}
		
		NFABase reverseNFA = nfa.reverse();
        
		int index;
		while((index = getNonEqualIndex(prevsim,sim)) != -1)
		{
			BitSet tmp = (BitSet) sim[index].clone();
			for(int j=0;j<this.nfa.getActionCount();j++)
			{
				BitSet pre = reverseNFA.getDstStateSetOfAState(index, j);
				if(pre.isEmpty())
					continue;
				
				BitSet remove = reverseNFA.getDstStateSetOfASet(prevsim[index], j);
				remove.andNot(reverseNFA.getDstStateSetOfASet(sim[index], j));
				
				
				for(int k=pre.nextSetBit(0);k>=0;k=pre.nextSetBit(k+1))
				{
					sim[k].andNot(remove);
				}
			}
			
			prevsim[index] = tmp;
		}

		BitSet[] simulateSet = new BitSet[size];
		for(int i=0;i<size;i++)
		{
			if(simulateSet[i]==null)
				simulateSet[i] = new BitSet();
			for(int j=0;j<size;j++)
			{
				if(sim[j].get(i))
				{
					simulateSet[i].set(j);
				}
			}
		}
		
		return simulateSet;
	}
	
	public BitSet[] compute3()
	{
		Set<Integer> indices = new HashSet<Integer>();
		
		int size = this.nfa.getStateCount();
		int actionCount = this.nfa.getActionCount();
		
		BitSet acc = this.nfa.getAcceptStateSet();
		BitSet[][] tran = this.nfa.getTransitionArray();
		
		List<Set<Integer>> prevsim = new ArrayList<Set<Integer>>();
		List<Set<Integer>> sim = new ArrayList<Set<Integer>>();

		
		for(int i=0;i<size;i++)
		{
			Set<Integer> set = new HashSet<Integer>();
			for(int k=0;k<size;k++)
				set.add(k);
			prevsim.add(set);
			
			
			Set<Integer> ss = new HashSet<Integer>();
			
			if(acc.get(i))
			{
				for(int k=acc.nextSetBit(0);k>=0;k=acc.nextSetBit(k+1))
					ss.add(k);
				indices.add(i);
			}
			else
			{
				for(int k=0;k<size;k++)
					ss.add(k);
			}
		
			for(int j=0;j<actionCount;j++)
			{
				if(!tran[i][j].isEmpty())
				{
					Set<Integer> ssCopy = new HashSet<Integer>(ss);
					for(int s : ssCopy)
					{
						if(tran[s][j].isEmpty())
						{
							ss.remove(s);
							indices.add(i);
						}
					}
				}
			}
			sim.add(ss);
		}
		
		NFABase reverseNFA = nfa.reverse();
		BitSet[][] tranReverse = reverseNFA.getTransitionArray();
		
        
		int index;
		while(!indices.isEmpty())
		{
			index = indices.iterator().next();
			indices.remove(index);
			
			Set<Integer> tmp = new HashSet<Integer>(sim.get(index));
			
			for(int j=0;j<actionCount;j++)
			{
				BitSet pre = tranReverse[index][j];
				if(pre.isEmpty())
					continue;
				
				
				Set<Integer> remove = new HashSet<Integer>();
				for(int s : prevsim.get(index))
				{
					if(!sim.get(index).contains(s))
					{
						BitSet set = tranReverse[s][j];
						for(int n=set.nextSetBit(0);n>=0;n=set.nextSetBit(n+1))
						{
							remove.add(n);
						}
					}
				}
				for(int s : sim.get(index))
				{
					BitSet set = tranReverse[s][j];
					for(int n=set.nextSetBit(0);n>=0;n=set.nextSetBit(n+1))
					{
						remove.remove(n);
					}
				}

				
				for(int k=pre.nextSetBit(0);k>=0;k=pre.nextSetBit(k+1))
				{
					boolean f = false;
					for(int s: remove)
					{
						if(!f && sim.get(k).contains(s))
						{
							f = true;
						}
						sim.get(k).remove(s);
					}
					if(f)
						indices.add(k);
					
				}
			}
			
			prevsim.set(index, tmp);
		}

		BitSet[] simulateSet = new BitSet[size];
		for(int i=0;i<size;i++)
		{
			if(simulateSet[i]==null)
				simulateSet[i] = new BitSet();
			for(int j=0;j<size;j++)
			{
				if(sim.get(j).contains(i))
				{
					simulateSet[i].set(j);
				}
			}
		}
		
		return simulateSet;
	}
	
	
	private int getNonEqualIndex(BitSet[] prevsim,BitSet[] sim)
	{
		for(int i=0;i<prevsim.length;i++)
		{
			if(!prevsim[i].equals(sim[i]))
				return i;
		}
		return -1;
	}
	
	private boolean isSubset(BitSet s1, BitSet s2)
	{
		BitSet t = (BitSet) s1.clone();
		t.or(s2);
		return t.equals(s2);
	}
}
