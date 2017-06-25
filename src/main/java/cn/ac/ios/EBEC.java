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
package cn.ac.ios;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.BitSet;
import java.util.Scanner;

import cn.ac.ios.antichain.AC;
import cn.ac.ios.antichain.AntichainPair;
import cn.ac.ios.common.ReturnValue;
import cn.ac.ios.common.StateSetPair;
import cn.ac.ios.hk.HK;
import cn.ac.ios.hkc.HKC;
import cn.ac.ios.myqueue.BFS;
import cn.ac.ios.myqueue.DFS;
import cn.ac.ios.myqueue.HashSetBFS;
import cn.ac.ios.nfa.NFA;
import cn.ac.ios.nfa.NFAWithTwoSets;
import cn.ac.ios.parser.NFAFile;
import cn.ac.ios.simulation.olrt.OLRTSimulation;

public class EBEC {

	enum Stt {
		BFS, DFS
	}
	enum PreOperation {
		Sim, Bisim,None
	}
	
	public static void main(String[] args) {
		
		boolean basicTest = true;
		
		Scanner input = new Scanner(System.in);
		String op = basicTest ? "file" : input.next();
		if(op.equals("file"))
		{
			Stt strategy = Stt.BFS;
			PreOperation preOperation = PreOperation.None;
			String s = basicTest ? "-bfs" : input.next();
			if(s.equals("-dfs"))
				strategy = Stt.DFS;
			
			s = basicTest ? "no" : input.next();
			if(s.equals("-sim"))
				preOperation = PreOperation.Sim;
			else if(s.equals("-bisim"))
				preOperation = PreOperation.Bisim;
			
			String f1 = basicTest ? "/home/fuchen/ARMCautomata/IBakery-4P-BinEnc-FwBad-Nondet-Partial/armcNFA_inclTest_1" : input.next();
			String f2 = basicTest ? "/home/fuchen/ARMCautomata/IBakery-4P-BinEnc-FwBad-Nondet-Partial/armcNFA_inclTest_2" : input.next();
			Reader reader = null;
			NFA nfa1 = null, nfa2=null;
			try {
				reader = new InputStreamReader(new FileInputStream(f1));
				nfa1 = NFAFile.parse(reader);
				reader = new InputStreamReader(new FileInputStream(f2));
				nfa2 = NFAFile.parse(reader);
			} catch (Exception e) {
				e.printStackTrace();
			}
			new EBEC().checkNFA(nfa1,nfa2, strategy, preOperation);
		}
		input.close();
	}
	private void checkNFA(NFA nfa1, NFA nfa2, Stt strategy, PreOperation preOperation)
	{
		nfa1 = nfa1.removeUselessState();
		nfa2 = nfa2.removeUselessState();
		NFAWithTwoSets nfaUnion = new NFAWithTwoSets(nfa1,nfa2);
		
		nfaUnion = nfaUnion.removeUselessAction();
		
		System.out.println("State number of the union NFA: " + nfaUnion.getStateCount() + "");
		System.out.println("Action number of the union NFA: " + nfaUnion.getActionCount() + "\n");
		
		System.out.println("HKMem: ");
		HK hk2;
		if(strategy == Stt.BFS)
			hk2 = new HK(nfaUnion,new HashSetBFS<StateSetPair<BitSet>>());
		else
			hk2 = new HK(nfaUnion,new HashSetBFS<StateSetPair<BitSet>>());
		long startTime6 = System.currentTimeMillis();
		ReturnValue r6 = hk2.checkEquivMem();
		long endTime6 = System.currentTimeMillis();
		System.out.print("The result is: " + r6.isResult() + "  ");
		System.out.println("Time: " + (endTime6 - startTime6) + " ms\n");
		
		
		System.out.println("HKCMem: ");
		HKC hkc2;
		hkc2 = new HKC(nfaUnion,new HashSetBFS<StateSetPair<BitSet>>());
		long startTime22 = System.currentTimeMillis();
		ReturnValue r22 = hkc2.checkEquivMem();
		long endTime22 = System.currentTimeMillis();
		System.out.print("The result is: " + r22.isResult() + "  ");
		System.out.println("Time: " + (endTime22 - startTime22) + " ms  \n");

		System.out.println("Antichain: ");
		NFAWithTwoSets nfaUnion2 = new NFAWithTwoSets(nfa1,nfa2);
		nfaUnion2 = nfaUnion2.removeUselessAction();
		AC ac1;
		if(strategy == Stt.BFS)
		{
			ac1 = new AC(nfaUnion2,new BFS<AntichainPair>());
		}
		else
		{
			ac1 = new AC(nfaUnion2,new DFS<AntichainPair>());
		}
		long startTime3 = System.currentTimeMillis();
		ReturnValue r3 = ac1.checkIncl();
		long endTime3 = System.currentTimeMillis();
		System.out.print("The result is: " + r3.isResult() + "  ");
		System.out.println("Time: " + (endTime3 - startTime3) + " ms\n");
		
		
		System.out.println("AntichainMem: ");
		AC ac12;
		ac12 = new AC(nfaUnion2,new HashSetBFS<AntichainPair>());
		long startTime32 = System.currentTimeMillis();
		ReturnValue r32 = ac12.checkInclMem();
		long endTime32 = System.currentTimeMillis();
		System.out.print("The result is: " + r32.isResult() + "  ");
		System.out.println("Time: " + (endTime32 - startTime32) + " ms\n");
		
		OLRTSimulation ol = null;
		long sti = System.currentTimeMillis();
		ol = new OLRTSimulation(nfaUnion2);
    	ol.computeSimulation();
    	long et = System.currentTimeMillis();
    	System.out.println("Sim Time: " + (et - sti) + " ms\n");

		
	}
}
