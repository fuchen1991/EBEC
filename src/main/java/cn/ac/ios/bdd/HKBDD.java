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

import java.util.HashMap;
import java.util.Map;

import cn.ac.ios.common.ReturnValue;
import cn.ac.ios.common.StateSetPair;
import cn.ac.ios.myqueue.MyQueue;
import net.sf.javabdd.BDD;

public class HKBDD {
	private NFAWithTwoSetsBDD bdd;

	private Map<BDD,BDD> r;
	private MyQueue<StateSetPair<BDD>> todo;
	private BDD transition;
	private BDD var;

	
	public HKBDD(NFAWithTwoSetsBDD bdd, MyQueue<StateSetPair<BDD>> todo)
	{
		this.bdd = bdd;
		this.todo = todo;
	}
	
	public ReturnValue checkEquiv()
	{
		todo.clear();
		r = new HashMap<BDD,BDD>();
		Map<BDD,BDD> transitionCache = new HashMap<BDD,BDD>();
		todo.push(new StateSetPair<BDD>(bdd.getaInit(),bdd.getbInit()));
		transition = bdd.getTransition();
		var = bdd.getVarStates().getVariableBDD(0);
		//this.bdd.getTransition().printDot();
		
		int tp = 0;
		while(!todo.isEmpty())
		{
			tp++;
			StateSetPair<BDD> pair = todo.pop();
			BDD x = pair.getX();
			BDD y = pair.getY();
			
			if(output(x) != output(y))
			{
				return new ReturnValue(false, -1, tp, r.size());
			}
			
//			BDD delta_x = x.and(bdd.getTransition());
//			delta_x = delta_x.exist(bdd.getVarStates().getVariableBDD(0));
//			BDD delta_y = y.and(bdd.getTransition());
//			delta_y = delta_y.exist(bdd.getVarStates().getVariableBDD(0));
//			
			BDD xTransition;
			if(transitionCache.get(x) != null)
			{
				xTransition = transitionCache.get(x);
				//System.out.println("sssssss" + (c++));
			}
			else
			{
				xTransition = x.and(transition).exist(var);
				//xTransition = xTransition.exist(var);
				transitionCache.put(x, xTransition);
				//x = xTransition;
			}
			
			BDD yTransition;
			if(transitionCache.get(y) != null)
				yTransition = transitionCache.get(y);
			else
			{
				yTransition = y.and(transition).exist(var);
				//yTransition = yTransition.exist(var);
				transitionCache.put(y, yTransition);
				//y = yTransition;
			}
			//System.out.println("ttttttt" + (d++));
			
			//x = x.and(transition);
			//x = x.exist(bdd.getVarStates().getVariableBDD(0));
			//y = y.and(transition);
			//y = y.exist(bdd.getVarStates().getVariableBDD(0));
			
			//x.free();
			//y.free();
			unify(xTransition,yTransition);
		}
		//System.out.println("Node num: " + bdd.getFactory().getNodeNum());
		
		return new ReturnValue(true, -1, tp, r.size());
	}
	
	private boolean output(BDD x)
	{
		if(x.and(bdd.getAcc()).isZero())
			return false;
		return true;
	}
	
	private void unify(BDD _x, BDD _y)
	{
		BDD succX = getParent(_x);
		BDD succY = getParent(_y);
		
		if(!succX.equals(succY))
		{
			int minVarX = (succX.isOne() || succX.isZero()) ? bdd.getActionBitNum() : succX.var();
			int minVarY = (succY.isOne() || succY.isZero()) ? bdd.getActionBitNum() : succY.var();
			
			if(minVarX>=bdd.getActionBitNum() && minVarY>=bdd.getActionBitNum())
			{
				r.put(succX, succY);
				
				//swap variables
				BDD x = succX.replace(bdd.getBddPairing());
				BDD y = succY.replace(bdd.getBddPairing());
				
				todo.push(new StateSetPair<BDD>(x,y));
			}
			else if(minVarX == minVarY)
			{
				r.put(succX, succY);
				unify(succX.low(),succY.low());
				unify(succX.high(),succY.high());
			}
			else if(minVarX < minVarY)
			{
				r.put(succX, succY);
				unify(succX.low(),succY);
				unify(succX.high(),succY);
			}
			else//minVarX > minVarY
			{
				r.put(succY,succX);
				unify(succX,succY.low());
				unify(succX,succY.high());
			}
		}
	}
	
	private BDD getParent(BDD x)
	{
		BDD tmp = r.get(x);
		if(tmp == null)
			return x;
		else
		{
			BDD tmp2 = r.get(tmp);
			if(tmp2 == null)
				return tmp;
			else
			{
				r.put(x, tmp2);
				return getParent(tmp2);
			}
		}
	}
	
}
