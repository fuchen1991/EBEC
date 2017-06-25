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
package cn.ac.ios.bdd;

import java.util.BitSet;

import cn.ac.ios.nfa.NFA;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class NFABDD {
	private BDD init;
	private BDD stateSpace;
	private BDD transition;
	private BDD acc;
	
	private BDDFactory factory;

	public NFABDD(BDDFactory f, NFA nfa, Variable varStates, Variable varActions)
	{
		this.factory = f;
		
		int n = nfa.getStateCount();
		int m = nfa.getActionCount();
		
		transition = factory.zero();
		init = factory.zero();
		acc = factory.zero();
		stateSpace = factory.zero();
		for(int i=0;i<n;i++)
		{
			BDD stateBDD = varStates.createBDDForValue(i, 0);
			for(int j = 0; j < m;j++)
			{
				BitSet bs = nfa.getTransitionArray()[i][j];
				for(int k=bs.nextSetBit(0);k>=0;k=bs.nextSetBit(k+1))
				{
					BDD succBDD = varStates.createBDDForValue(k, 1);
					BDD labelBDD = varActions.createBDDForValue(j, 0);
					
					transition = transition.or(stateBDD.and(succBDD.and(labelBDD)));
					succBDD.free();
					labelBDD.free();
				}
			}
			if(nfa.getAcceptStateSet().get(i))
				acc = acc.or(stateBDD);
			if(nfa.getInitialStateSet().get(i))
				init = init.or(stateBDD);
			
			stateSpace = stateSpace.or(stateBDD);
			stateBDD.free();
		}
	}

	public BDD getInit() {
		return init;
	}

	public BDD getTransition() {
		return transition;
	}

	public BDD getAcc() {
		return acc;
	}
}
