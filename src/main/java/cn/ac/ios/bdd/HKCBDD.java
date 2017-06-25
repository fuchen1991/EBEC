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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.ac.ios.common.ReturnValue;
import cn.ac.ios.common.StateSetPair;
import cn.ac.ios.myqueue.MyQueue;
import net.sf.javabdd.BDD;

public class HKCBDD {

	private NFAWithTwoSetsBDD bdd;

	private List<StateSetPair<BDD>> r;
	private MyQueue<StateSetPair<BDD>> todo;
	private BDD transition;
	private BDD var;

	public HKCBDD(NFAWithTwoSetsBDD bdd, MyQueue<StateSetPair<BDD>> todo)
	{
		this.bdd = bdd;
		this.todo = todo;
	}
	
	public ReturnValue checkEquiv()
	{
		todo.clear();
		r = new ArrayList<StateSetPair<BDD>>();
		Map<BDD,BDD> transitionCache = new HashMap<BDD,BDD>();
		transition = bdd.getTransition();
		var = bdd.getVarStates().getVariableBDD(0);
		todo.push(new StateSetPair<BDD>(bdd.getaInit(),bdd.getbInit()));
		
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
			
//			x = x.and(bdd.getTransition());
//			x = x.exist(bdd.getVarStates().getVariableBDD(0));
//			y = y.and(bdd.getTransition());
//			y = y.exist(bdd.getVarStates().getVariableBDD(0));
			
			//x.free();
			//y.free();
			unify(x,y);
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
	
	private void unify(BDD x, BDD y)
	{
		List<StateSetPair<BDD>> rx = new ArrayList<StateSetPair<BDD>>(r);
		List<StateSetPair<BDD>> ry = new ArrayList<StateSetPair<BDD>>(r);
		BDD xNorm = getNormalFormWithY(x.id(),y,rx);
		BDD yNorm = getNormalFormWithY(y.id(),x,ry);
		
		if(xNorm == null && yNorm == null)
		{
			return;
		}
		else
		{
			BDD succX,succY;
			
			if(xNorm == null)
			{
				yNorm = yNorm.or(x);
				yNorm = getNormalForm(yNorm,ry);
				r.add(new StateSetPair<BDD>(y.id(),yNorm.id()));
			}
			else if(yNorm == null)
			{
				xNorm = xNorm.or(y);
				xNorm = getNormalForm(xNorm,rx);
				r.add(new StateSetPair<BDD>(x.id(),xNorm.id()));
			}
			else
			{
				xNorm = xNorm.or(yNorm);
				xNorm = getNormalForm(xNorm,rx);
				r.add(new StateSetPair<BDD>(x.id(),xNorm.id()));
				r.add(new StateSetPair<BDD>(y.id(),xNorm.id()));
			}
			
			succX = x;
			succY = y;
			
//			if(xNorm!=null)
//				xNorm.free();
//			if(yNorm!=null)
//				yNorm.free();
			
			int minVarX = (succX.isOne() || succX.isZero()) ? bdd.getActionBitNum() : succX.var();
			int minVarY = (succY.isOne() || succY.isZero()) ? bdd.getActionBitNum() : succY.var();
			
			if(minVarX>=bdd.getActionBitNum() && minVarY>=bdd.getActionBitNum())
			{
				//swap variables
				BDD xx = succX.replace(bdd.getBddPairing());
				BDD yy = succY.replace(bdd.getBddPairing());
				
				todo.push(new StateSetPair<BDD>(xx,yy));
			}
			else if(minVarX == minVarY)
			{
				//BDD l1 = succX.low(), l2 = succY.low(), h1 = succX.high(), h2 = succY.high();
				//succX.free();
				//succY.free();
//				unify(l1,l2);
//				unify(h1,h2);
				unify(succX.low(),succY.low());
				unify(succX.high(),succY.high());
			}
			else if(minVarX < minVarY)
			{
				//BDD l1 = succX.low(), h1 = succX.high();
				//succX.free();
				unify(succX.low(),succY);
				unify(succX.high(),succY);
			}
			else//minVarX > minVarY
			{
				//BDD l2 = succY.low(), h2 = succY.high();
				//succY.free();
				unify(succX,succY.low());
				unify(succX,succY.high());
			}
		}
	}
	
	private BDD getNormalFormWithY(BDD x, BDD y, List<StateSetPair<BDD>> r)
	{
		if(isSubset(y,x))
		{
			//x.free();
			return null;
		}
		
		BDD origin = x.id();
		x = passRule(x,r);
		if(origin.equals(x))
		{
			//origin.free();
			return x;
		}
		
		//origin.free();
		return getNormalFormWithY(x,y,r);
	}
	
	private BDD getNormalForm(BDD x, List<StateSetPair<BDD>> r)
	{
		BDD origin = x.id();
		x = passRule(x,r);
		if(origin.equals(x))
		{
			//origin.free();
			return x;
		}
		
		//origin.free();
		return getNormalForm(x,r);
	}
	
	private BDD passRule(BDD x, List<StateSetPair<BDD>> r)
	{
		for(int i=r.size()-1;i>=0;i--)
		{
			StateSetPair<BDD> pair = r.get(i);
			if(isSubset(pair.getX(),x))
			{
				x = x.or(pair.getY());
				r.remove(i);
			}
		}
		return x;
	}
	
	private boolean isSubset(BDD x, BDD y)
	{
		//return (y.not().and(x)).isZero();
		BDD tmp = x.or(y);
		return (tmp.equals(y));
	}
}
