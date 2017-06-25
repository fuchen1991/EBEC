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
import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class Variable {
	private List<List<BDD>> variables;
	private int copies;
	private BDDFactory factory;
	
	public Variable(BDDFactory f, int bitNum, int c)
	{
		this.factory = f;
		this.copies = c;
		
		//int bitNum = Integer.SIZE - Integer.numberOfLeadingZeros(size-1);
		variables = new ArrayList<List<BDD>>();
		for(int i=0;i<copies;i++)
		{
			variables.add(new ArrayList<BDD>());
		}
		
		int varNum = factory.varNum();
		factory.extVarNum(bitNum*copies);
		
		for(int i=0;i<bitNum;i++)
		{
			for(int j=0;j<copies;j++)
			{
				BDD bdd = factory.ithVar(varNum);
				variables.get(j).add(bdd);
				varNum++;
			}
		}
		
	}
	
	public BDD createBDDForValue(int value, int copy)
	{
		int bit = 1;
		BDD bdd = factory.one();
		for(int i=0;i<variables.get(copy).size();i++)
		{
			BDD bddVar =  variables.get(copy).get(i);
			
			if((value & bit) == 0)
				bdd = bdd.and(bddVar.not());
			else
				bdd = bdd.and(bddVar);
			bit <<= 1;
		}
		
		return bdd;
	}

	public List<List<BDD>> getVariables() {
		return variables;
	}
	
	public BDD getVariableBDD(int copy)
	{
		BDD result = factory.one();
		for(BDD var : this.variables.get(copy))
		{
			result = result.and(var);
		}
		
		return result;
	}
}
