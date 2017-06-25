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
package cn.ac.ios.bisimulation;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.BitSet;

import cn.ac.ios.nfa.NFA;
import cn.ac.ios.parser.NFAFile;
import cn.ac.ios.simulation.NaiveSimulation;
import cn.ac.ios.simulation.RefinedSimulation;
import cn.ac.ios.simulation.olrt.OLRTSimulation;

public class BismulationTest {

	static BitSet[] naive,refine,naiveBisimulation,bisimulation,bisimulationFromCpp,olrt;
	
	public static void main(String[] args) throws InterruptedException
	{
		String f = "/home/fuchen/ARMCautomata/IBakery4pBinEnc-FbtOneOne-Nondet/armcNFA_inclTest_100";
		Reader reader = null;
		NFA nfa = null;
		try {
			reader = new InputStreamReader(new FileInputStream(f));
			nfa = NFAFile.parse(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		nfa = nfa.removeUselessState();
		nfa = nfa.removeUselessAction();
     
		for(int i=0;i<5;++i)
		{
			RefinedSimulation rs = new RefinedSimulation(nfa);
	        long startTime1 = System.currentTimeMillis();
	        rs.computeSimulation();
	    	long endTime1 = System.currentTimeMillis();
	    	System.out.println("Refined simulation Time: " + (endTime1 - startTime1) + " ms\n");
	    	refine = rs.getRelation();
		}

		for(int i=0;i<5;++i)
		{
			long startTime4 = System.currentTimeMillis();
			OLRTSimulation ol = new OLRTSimulation(nfa);
	        ol.computeSimulation();
	    	long endTime4 = System.currentTimeMillis();
	    	System.out.println("OLRT simulation Time: " + (endTime4 - startTime4) + " ms\n");
	    	olrt = ol.getRelationSim();
		}
		
		for(int i=0;i<5;++i)
		{
	     	NaiveSimulation ns = new NaiveSimulation(nfa);
			long startTime0 = System.currentTimeMillis();
	    	ns.computeSimulation();
	    	long endTime0 = System.currentTimeMillis();
	    	naive = ns.getRelation();
	        System.out.println("Naive simulation Time: " + (endTime0 - startTime0) + " ms\n");
		}
     	
    	boolean checkSim = equal(naive,refine);
    	System.out.println("naive == refine ?? -> " + checkSim);
    	boolean checkSim2 = equal(refine,olrt);
    	System.out.println("refine == olrt ?? -> " + checkSim2 + "\n\n");
    	
//    	NaiveBisimulation na = new NaiveBisimulation(nfa);
//    	long startTime2 = System.currentTimeMillis();
//    	na.computeBisimulation();
//    	long endTime2 = System.currentTimeMillis();
//    	naiveBisimulation = na.getRelation();
//    	System.out.println("Naive bisimilarity Time: " + (endTime2 - startTime2) + " ms\n");
//    	
//    	PartitionRefinementBisimulation bi = new PartitionRefinementBisimulation(nfa);
//    	long startTime3 = System.currentTimeMillis();
//    	bi.computeBisimulation();
//    	long endTime3 = System.currentTimeMillis();
//    	bisimulation = bi.getRelation();
//    	System.out.println("Bisimilarity Time: " + (endTime3 - startTime3) + " ms\n");
    	
//    	boolean checkBisim = equal(naiveBisimulation,bisimulation);
//    	System.out.println("naiveBisimulation == partitionRefinement ?? -> " + checkBisim);
//    	checkBisim = equal(naiveBisimulation,bisimulationFromCpp);
//    	System.out.println(checkBisim);
	}
	
	private static boolean equal(BitSet[] s1, BitSet[] s2)
	{
		if(s1.length != s2.length)
			return false;
		
		for(int i=0;i<s1.length;i++)
		{
//			if(!s1[i].equals(s2[i]))
//				return false;
			for(int j=0;j<s1[i].size();++j)
			{
				if(s1[i].get(j) != s2[i].get(j))
					return false;
			}
		}
		return true;
	}
}
