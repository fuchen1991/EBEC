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

import java.util.BitSet;

import cn.ac.ios.nfa.NFABase;

public class NaiveSimulation {

	private NFABase nfa;
	private boolean[][] rel;
	
	public NaiveSimulation(NFABase nfa)
	{
		this.nfa = nfa;
	}
	
	public void computeSimulation()
	{
		int n = this.nfa.getStateCount();
		int a = this.nfa.getActionCount();
		rel = new boolean[n][n];
		for(int i=0;i<n;i++)
		{
			for(int j=0;j<n;j++)
			{
				if(i==j)
					rel[i][j] = true;
				else
				{
					if(nfa.getAcceptStateSet().get(i) && !nfa.getAcceptStateSet().get(j))
						rel[i][j] = false;
					else
						rel[i][j] = true;
				}
			}
		}
		
		while(true)
		{
			boolean changed = false;
			for(int i=0;i<n;i++)
			{
				for(int j=0;j<n;j++)
				{
					if(i != j && rel[i][j])
					{
						for(int k=0;k<a;k++)
						{
							BitSet des = nfa.getDstStateSetOfAState(i, k);
							BitSet candidate = nfa.getDstStateSetOfAState(j, k);
							for(int m = des.nextSetBit(0);m>=0;m=des.nextSetBit(m+1))
							{
								boolean isSimulated = false;
								for(int l = candidate.nextSetBit(0);l>=0;l=candidate.nextSetBit(l+1))
								{
									if(rel[m][l])
									{
										isSimulated = true;
										break;
									}
								}
								if(!isSimulated)
								{
									rel[i][j] = false;
									changed = true;
									break;
								}
							}
							if(!rel[i][j])
								break;
						}
					}
				}
			}
			
			
			if(!changed)
				break;
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
				if(rel[j][i])
				{
					set[i].set(j);
				}
			}
		}
		return set;
	}
}
