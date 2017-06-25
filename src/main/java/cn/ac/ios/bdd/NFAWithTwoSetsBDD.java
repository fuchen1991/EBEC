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

import cn.ac.ios.nfa.NFAWithTwoSets;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

public class NFAWithTwoSetsBDD {
	private BDDFactory factory;
	private BDD acc;
	private BDD transition;
	private BDD aInit;
	private BDD bInit;
	private int actionBitNum;
	private Variable varActions;
	private Variable varStates;
	private BDDPairing bddPairing;
	
	public NFAWithTwoSetsBDD(NFAWithTwoSets union)
	{
		int stateNum = union.getStateCount();
		int stateBitNum = Integer.SIZE - Integer.numberOfLeadingZeros(stateNum-1);
		int actionNum = union.getActionCount();
		this.actionBitNum = Integer.SIZE - Integer.numberOfLeadingZeros(actionNum-1);
		
		factory = BDDFactory.init("java",80000000, 10000000);
		factory.setMaxIncrease(20000000);
		varActions = new Variable(factory,actionBitNum,1);
		varStates = new Variable(factory,stateBitNum,2);

		transition = factory.zero();
		acc = factory.zero();
		aInit = factory.zero();
		bInit = factory.zero();
		for(int i=0;i<stateNum;i++)
		{
			BDD stateBDD = varStates.createBDDForValue(i, 0);
			for(int j = 0; j < actionNum;j++)
			{
				BitSet bs = union.getTransitionArray()[i][j];
				for(int k=bs.nextSetBit(0);k>=0;k=bs.nextSetBit(k+1))
				{
					BDD succBDD = varStates.createBDDForValue(k, 1);
					BDD labelBDD = varActions.createBDDForValue(j, 0);
					
					transition = transition.or(stateBDD.and(succBDD.and(labelBDD)));
					succBDD.free();
					labelBDD.free();
				}
			}
			if(union.getAcceptStateSet().get(i))
				acc = acc.or(stateBDD);
			
			if(union.getStateSet_1().get(i))
				aInit = aInit.or(stateBDD);
			
			if(union.getStateSet_2().get(i))
				bInit = bInit.or(stateBDD);
			
					
			stateBDD.free();
		}
		
		bddPairing = factory.makePair();
		for(int i=0;i<stateBitNum;i++)
		{
			bddPairing.set(varStates.getVariables().get(1).get(i).var(), varStates.getVariables().get(0).get(i).var());
		}
	}
	
	public BDD getAcc() {
		return acc;
	}

	public BDD getTransition() {
		return transition;
	}

	public int getActionBitNum() {
		return actionBitNum;
	}

	public BDDPairing getBddPairing() {
		return bddPairing;
	}

	public BDD getaInit() {
		return aInit;
	}

	public BDD getbInit() {
		return bInit;
	}

	public Variable getVarStates() {
		return varStates;
	}

	public BDDFactory getFactory() {
		return factory;
	}

}
