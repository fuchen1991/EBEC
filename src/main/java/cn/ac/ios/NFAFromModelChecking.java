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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

import cn.ac.ios.antichain.AC;
import cn.ac.ios.antichain.AntichainPair;
import cn.ac.ios.bisimulation.PartitionRefinementBisimulation;
import cn.ac.ios.common.ReturnValue;
import cn.ac.ios.common.StateSetPair;
import cn.ac.ios.hk.HK;
import cn.ac.ios.hkc.HKC;
import cn.ac.ios.myqueue.HashSetBFS;
import cn.ac.ios.nfa.NFA;
import cn.ac.ios.nfa.NFAWithTwoSets;
import cn.ac.ios.parser.NFAFile;
import cn.ac.ios.simulation.olrt.OLRTSimulation;

public class NFAFromModelChecking {

	static int HKIndex = 0;
	static int HKCIndex = 1;
	static int ACIndex = 2;
	static int HKBDDIndex = 4;
	static int HKCBDDIndex = 3;
	static int HKMemIndex = 5;
	static int HKCMemIndex = 6;
	static int ACMemIndex = 7;
	static int Bisimulation = 8;
//	static int HKBisim = 9;
//	static int HKCBisim = 10;
	static int HKsim = 11;
	static int HKCsim = 12;
	static int ACSim = 13;
//	static int ACBisim = 14;
	static int Simulation = 15;
	static int HKSimTotal = 16;
//	static int HKBisimTotal = 17;
	static int HKCSimTotal = 18;
//	static int HKCBisimTotal = 19;
	static int ACSimTotal = 20;
//	static int ACBisimTotal = 21;
	
	static int HKmin = 22;
	static int HKCmin = 23;
	static int ACmin = 24;
	static int HKminTotal = 25;
	static int HKCminTotal = 26;
	static int ACminTotal = 27;
	

	int last = 16;
	
	static int algNum = 28;
	static int repeat = 4;

	static FileWriter fwBoxplot;
	static FileWriter fwData;
	
	public static void main(String[] args) throws Exception {
		fwBoxplot = new FileWriter("boxplot.txt");
		fwData = new FileWriter("data.csv");
		
		NFAFromModelChecking m = new NFAFromModelChecking();
		String[][] filename = m.readFiles();
		int count = 0;
		for(int i=0;i<filename.length;i++)
		{
			count += filename[i].length-1;
		}
		m.run(filename,count);

	}
	
	private String[][] readFiles() throws Exception
	{
		String dir = "/home/fuchen/ARMCautomata";
		String [][] filename;
		
		File d = new File(dir);
		String [] list = d.list();
		filename = new String[list.length][];
		for(int i=0;i<list.length;i++)
		{
			File f = new File(dir+ "/" + list[i]);
			String[] l = f.list();
			Arrays.sort(l, new Comparator<String>() {
				public int compare(String s1, String s2) {  
			    long diff = s1.length() - s2.length();  
			    if (diff > 0)  
			      return 1;  
			    else if (diff < 0)  
			      return -1;  
			    else  
			      return s1.compareTo(s2);  
			     }   
			});  

			int start = l.length < last ? 0 : l.length - last;
			filename[i] = new String[l.length - start];
			for(int j=start;j<l.length;j++)
			{
				filename[i][j-start] = dir+ "/" + list[i] + "/" + l[j];
			}
		}
		
		return filename;
	}
	
	private void run(String[][] filename, int count) throws IOException
	{
		String name =  "NFAFromModelChecking.txt";
		FileWriter fw = new FileWriter(name);
		
		//count = 10;/////////////////
		
		count =  count * 2;
		int index = 0;
		long[][] timeData = new long[algNum][count];
		ReturnValue[][] resultData = new ReturnValue[algNum][count];
		for(int i=0;i<algNum;i++)
		{
			timeData[i] = new long[count];
			resultData[i] = new ReturnValue[count];
		}

		for(int i=0;i<filename.length;i++)
		{
			if(index>=count)
				break;
			for(int j=0;j<filename[i].length-1;j++)
			{
				if(index>=count)
					break;
				
				//two directions
				execute(filename[i][j],filename[i][j+1],fw, timeData, resultData, index);
				++index;
				execute(filename[i][j+1],filename[i][j],fw, timeData, resultData, index);
				++index;
			}
		}
		
		sortAndPrint(fw,timeData,resultData,count);
		
		fw.flush();
		fw.close();
		fwBoxplot.flush();
		fwBoxplot.close();
		fwData.flush();
		fwData.close();
		System.out.println("over");
	}

	private void execute(String f1, String f2, FileWriter fw, long[][] timeData, ReturnValue[][] resultData, int index) throws IOException
	{
		System.out.println("process " + index);
		System.out.println(f1);
		System.out.println(f2);
		
		long[] time = new long[algNum];
		ReturnValue[] result = new ReturnValue[algNum];
		
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
		
		reader.close();
		
		nfa1 = nfa1.removeUselessState();
		nfa2 = nfa2.removeUselessState();
		NFAWithTwoSets nfaUnion = new NFAWithTwoSets(nfa1,nfa2);
		
		//to check language inclusion, take the union
		BitSet set = nfaUnion.getStateSet_1();
		set.or(nfaUnion.getStateSet_2());
		nfaUnion.setStateSet_1(set);
		
		final NFAWithTwoSets nfa = nfaUnion.removeUselessAction();
		
		long averageTime;
		
		averageTime = 0;
		PartitionRefinementBisimulation bi = null;
		for(int a=0;a<repeat;++a)
		{
			long sti = System.nanoTime();
			bi = new PartitionRefinementBisimulation(nfa);
	    	bi.computeBisimulation();
	    	long et = System.nanoTime();
	    	averageTime += et - sti;
		}
    	//BitSet[] bisim = bi.getRelation();
    	time[Bisimulation] = averageTime/repeat;
    	
    	averageTime = 0;
    	OLRTSimulation ol = null;
		for(int a=0;a<repeat;++a)
		{
			long sti = System.nanoTime();
			ol = new OLRTSimulation(nfa);
	    	ol.computeSimulation();
	    	long et = System.nanoTime();
	    	averageTime += et - sti;
		}
    	BitSet[] sim = ol.getRelationSim();
    	BitSet[] besim = ol.getRelationBeSim();
    	time[Simulation] = averageTime/repeat;

		//NFAWithTwoSets nfa_bisim = saturate(nfa, bisim);
		NFAWithTwoSets nfa_sim = saturate(nfa, sim);
		NFAWithTwoSets nfa_min = bi.getMinimizedNFA(nfa);

		averageTime = 0;
		ReturnValue rhkmem = null;
		for(int a=0;a<repeat;++a)
		{
			long startTime0 = System.nanoTime();
        	HK hkmem = new HK(nfa,new HashSetBFS<StateSetPair<BitSet>>());
        	rhkmem = hkmem.checkEquivMem();
        	long endTime0 = System.nanoTime();
        	averageTime += endTime0 - startTime0;
		}
    	time[HKMemIndex] = averageTime/repeat;
    	result[HKMemIndex] = rhkmem;
    	
    	averageTime = 0;
		ReturnValue rhksim = null;
		for(int a=0;a<repeat;++a)
		{
			long startTime0 = System.nanoTime();
        	HK hksim = new HK(nfa_sim,new HashSetBFS<StateSetPair<BitSet>>());
        	rhksim = hksim.checkEquivMem();
        	long endTime0 = System.nanoTime();
        	averageTime += endTime0 - startTime0;
		}
    	time[HKsim] = averageTime/repeat;
    	time[HKSimTotal] = time[HKsim] + time[Simulation];
    	result[HKsim] = rhksim;
    	
//    	averageTime = 0;
//		ReturnValue rhkbisim = null;
//		for(int a=0;a<repeat;++a)
//		{
//			long startTime0 = System.nanoTime();
//        	HK hkbisim = new HK(nfa_bisim,new HashSetBFS<StateSetPair<BitSet>>());
//        	rhkbisim = hkbisim.checkEquivMem();
//        	long endTime0 = System.nanoTime();
//        	averageTime += endTime0 - startTime0;
//		}
//    	time[HKBisim] = averageTime/repeat;
//    	time[HKBisimTotal] = time[HKBisim] + time[Bisimulation];
//    	result[HKBisim] = rhkbisim;
    	
    	averageTime = 0;
		ReturnValue rhkmin = null;
		for(int a=0;a<repeat;++a)
		{
			long startTime0 = System.nanoTime();
        	HK hkmin = new HK(nfa_min,new HashSetBFS<StateSetPair<BitSet>>());
        	rhkmin = hkmin.checkEquivMem();
        	long endTime0 = System.nanoTime();
        	averageTime += endTime0 - startTime0;
		}
    	time[HKmin] = averageTime/repeat;
    	time[HKminTotal] = time[HKmin] + time[Bisimulation];
    	result[HKmin] = rhkmin;
    	
    	averageTime = 0;
		ReturnValue rhkcmem = null;
		for(int a=0;a<repeat;++a)
		{
			long startTime0 = System.nanoTime();
        	HKC hkcmem = new HKC(nfa,new HashSetBFS<StateSetPair<BitSet>>());
        	rhkcmem = hkcmem.checkEquivMem();
        	long endTime0 = System.nanoTime();
        	averageTime += endTime0 - startTime0;
		}
    	time[HKCMemIndex] = averageTime/repeat;
    	result[HKCMemIndex] = rhkcmem;
    	
    	averageTime = 0;
		ReturnValue rhkcsim = null;
		for(int a=0;a<repeat;++a)
		{
			long startTime0 = System.nanoTime();
        	HKC hkcsim = new HKC(nfa_sim,new HashSetBFS<StateSetPair<BitSet>>());
        	rhkcsim = hkcsim.checkEquivMem();
        	long endTime0 = System.nanoTime();
        	averageTime += endTime0 - startTime0;
		}
    	time[HKCsim] = averageTime/repeat;
    	time[HKCSimTotal] = time[HKCsim] + time[Simulation];
    	result[HKCsim] = rhkcsim;
    	
//    	averageTime = 0;
//		ReturnValue rhkcbisim = null;
//		for(int a=0;a<repeat;++a)
//		{
//			long startTime0 = System.nanoTime();
//        	HKC hkcbisim = new HKC(nfa_bisim,new HashSetBFS<StateSetPair<BitSet>>());
//        	rhkcbisim = hkcbisim.checkEquivMem();
//        	long endTime0 = System.nanoTime();
//        	averageTime += endTime0 - startTime0;
//		}
//    	time[HKCBisim] = averageTime/repeat;
//    	time[HKCBisimTotal] = time[HKCBisim] + time[Bisimulation];
//    	result[HKCBisim] = rhkcbisim;
    	
    	averageTime = 0;
		ReturnValue rhkcmin = null;
		for(int a=0;a<repeat;++a)
		{
			long startTime0 = System.nanoTime();
        	HKC hkcmin = new HKC(nfa_min,new HashSetBFS<StateSetPair<BitSet>>());
        	rhkcmin = hkcmin.checkEquivMem();
        	long endTime0 = System.nanoTime();
        	averageTime += endTime0 - startTime0;
		}
    	time[HKCmin] = averageTime/repeat;
    	time[HKCminTotal] = time[HKCmin] + time[Bisimulation];
    	result[HKCmin] = rhkcmin;
    	

		NFAWithTwoSets nfaUnion2 = new NFAWithTwoSets(nfa1,nfa2);
		NFAWithTwoSets n2 = nfaUnion2.removeUselessAction();
		NFAWithTwoSets nfa_min2 = bi.getMinimizedNFA(n2);
		

		averageTime = 0;
		ReturnValue racmem = null;
		for(int a=0;a<repeat;++a)
		{
			long startTime0 = System.nanoTime();
        	AC acmem = new AC(n2,new HashSetBFS<AntichainPair>());
        	racmem = acmem.checkInclMem();
        	long endTime0 = System.nanoTime();
        	averageTime += endTime0 - startTime0;
		}
    	time[ACMemIndex] = averageTime/repeat;
    	result[ACMemIndex] = racmem;

		
		averageTime = 0;
		ReturnValue racsim = null;
		for(int a=0;a<repeat;++a)
		{
			long startTime0 = System.nanoTime();
        	AC acsim = new AC(n2,new HashSetBFS<AntichainPair>());
        	racsim = acsim.checkInclMemWithRelation(sim,besim);
        	long endTime0 = System.nanoTime();
        	averageTime += endTime0 - startTime0;
		}
    	time[ACSim] = averageTime/repeat;
    	time[ACSimTotal] = time[ACSim] + time[Simulation];
    	result[ACSim] = racsim;
    	
//    	averageTime = 0;
//		ReturnValue racbisim = null;
//		for(int a=0;a<repeat;++a)
//		{
//			long startTime0 = System.nanoTime();
//        	AC acbisim = new AC(n2,new HashSetBFS<AntichainPair>());
//        	racbisim = acbisim.checkInclMemWithRelation(bisim,bisim);
//        	long endTime0 = System.nanoTime();
//        	averageTime += endTime0 - startTime0;
//		}
//    	time[ACBisim] = averageTime/repeat;
//    	time[ACBisimTotal] = time[ACBisim] + time[Bisimulation];
//    	result[ACBisim] = racbisim;
    	
    	averageTime = 0;
		ReturnValue racmin = null;
		for(int a=0;a<repeat;++a)
		{
			long startTime0 = System.nanoTime();
        	AC acmin = new AC(nfa_min2,new HashSetBFS<AntichainPair>());
        	racmin = acmin.checkInclMem();
        	long endTime0 = System.nanoTime();
        	averageTime += endTime0 - startTime0;
		}
    	time[ACmin] = averageTime/repeat;
    	time[ACminTotal] = time[ACmin] + time[Bisimulation];
    	result[ACmin] = racmin;

		for(int k=0;k<algNum;k++)
		{
			timeData[k][index] = time[k];
			resultData[k][index] = result[k];
		}
		
		System.out.println(result[ACMemIndex].isResult());
	}
	
	public void sortAndPrint(FileWriter fw, long[][] time,ReturnValue[][] result,int count) throws IOException
	{
		int index1 = 0;
		int index2 = 0;
		for(int j=0;j<count;j++)
		{
			if(result[ACMemIndex][j].isResult())
			{
				index1++;
			}
			else
			{
				index2++;
			}
		}
		
		int[][] ep1 = new int[algNum][index1];
		int[][] tp1 = new int[algNum][index1];
		int[][] rp1 = new int[algNum][index1];
		long[][] t1 = new long[algNum][index1];
		int[][] ep2 = new int[algNum][index2];
		int[][] tp2 = new int[algNum][index2];
		int[][] rp2 = new int[algNum][index2];
		long[][] t2 = new long[algNum][index2];
		index1=0;
		index2=0;
		
		
		//distinguish "false" and "true"
		for(int j=0;j<count;j++)
		{
			boolean check = result[ACMemIndex][j].isResult();
			assert(check == result[HKsim][j].isResult());
			assert(check == result[HKCmin][j].isResult());
			
			if(check)
			{
				for(int i=0;i<algNum;i++)
				{
					if(result[i][j] != null)
					{
						if(result[i][j].isResult() != check)
						{
							System.out.println("ERR" + i + result[i][j].isResult());
							fw.write(String.format("ERR %d %d \n\n", j, i));
						}
		
						tp1[i][index1] = result[i][j].getTodoPairsNumber();
						ep1[i][index1] = result[i][j].getTouchedPairsNumber();
						rp1[i][index1] = result[i][j].getrPairsNumber();
					}
					t1[i][index1] = time[i][j];
				}
				++index1;
			}
			else
			{
				for(int i=0;i<algNum;i++)
				{
					if(result[i][j] != null)
					{
						if(result[i][j].isResult() != check)
						{
							System.out.println("ERR " + i + result[i][j].isResult());
							fw.write(String.format("ERR %d %d \n\n", j, i));
						}
		
						ep2[i][index2] = result[i][j].getTouchedPairsNumber();
						tp2[i][index2] = result[i][j].getTodoPairsNumber();
						rp2[i][index2] = result[i][j].getrPairsNumber();
					}
					t2[i][index2] = time[i][j];
				}
				++index2;
			}
		}

		assert(index1+index2 == count);
		
		for(int i=0;i<algNum;i++)
		{
			Arrays.sort(ep1[i]);
			Arrays.sort(t1[i]);
			Arrays.sort(tp1[i]);
			Arrays.sort(rp1[i]);
			
			Arrays.sort(ep2[i]);
			Arrays.sort(t2[i]);
			Arrays.sort(tp2[i]);
			Arrays.sort(rp2[i]);
		}
		
		fw.write(String.format("Inclusion holds: %d\n",index1));
		fw.write(String.format("             %45s       	   		%30s     	   		%30s           		%30s\n","Time (ms)","touched pairs ","pairs in todo","pairs in R"));
		fw.write(String.format("	        %11s   %11s   %11s   %11s   %11s		%7s %7s %7s %7s %7s		%7s %7s %7s %7s %7s			%7s %7s %7s %7s %7s\n","min","25%","50%","75%","max","min","25%","50%","75%","max","min","25%","50%","75%","max","min","25%","50%","75%","max"));

		
		writeResult(fw,t1,ep1,tp1,rp1,index1,HKMemIndex,"HK");
		writeResult(fw,t1,ep1,tp1,rp1,index1,HKCMemIndex,"HKC");
		writeResult(fw,t1,ep1,tp1,rp1,index1,ACMemIndex,"AC");
		fw.write("\n");
		writeResult2(fw,t1,index1,Simulation,"Sim");
		fw.write("\n");
		writeResult(fw,t1,ep1,tp1,rp1,index1,HKsim,"HKsim");
		writeResult(fw,t1,ep1,tp1,rp1,index1,HKCsim,"HKCsim");
		writeResult(fw,t1,ep1,tp1,rp1,index1,ACSim,"ACSim");
		fw.write("\n");
		writeResult2(fw,t1,index1,HKSimTotal,"HKSimTot");
		writeResult2(fw,t1,index1,HKCSimTotal,"HKCSimTot");
		writeResult2(fw,t1,index1,ACSimTotal,"ACSimTot");
		fw.write("\n");
		writeResult2(fw,t1,index1,Bisimulation,"Bisim");
		fw.write("\n");
//		writeResult(fw,t1,ep1,tp1,rp1,index1,HKBisim,"HKBisim");
//		writeResult(fw,t1,ep1,tp1,rp1,index1,HKCBisim,"HKCBisim");
//		writeResult(fw,t1,ep1,tp1,rp1,index1,ACBisim,"ACBisim");
//		fw.write("\n");
//		writeResult2(fw,t1,index1,HKBisimTotal,"HKBisTot");
//		writeResult2(fw,t1,index1,HKCBisimTotal,"HKCBisTot");
//		writeResult2(fw,t1,index1,ACBisimTotal,"ACBisTot");
//		fw.write("\n");
		writeResult(fw,t1,ep1,tp1,rp1,index1,HKmin,"HKmin");
		writeResult(fw,t1,ep1,tp1,rp1,index1,HKCmin,"HKCmin");
		writeResult(fw,t1,ep1,tp1,rp1,index1,ACmin,"ACmin");
		fw.write("\n");
		writeResult2(fw,t1,index1,HKminTotal,"HKminTot");
		writeResult2(fw,t1,index1,HKCminTotal,"HKCminTot");
		writeResult2(fw,t1,index1,ACminTotal,"ACminTot");
		fw.write("\n\n");
		
		writeData(t1,index1, "Incl");
		
		fw.write(String.format("Inclusion does NOT hold: %d\n",index2));
		fw.write(String.format("              %45s     	     		%30s     	   		%30s           		%30s\n","Time (ms)","touched pairs ","pairs in todo","pairs in R"));
		fw.write(String.format("	        %11s   %11s   %11s   %11s   %11s		%7s %7s %7s %7s %7s		%7s %7s %7s %7s %7s			%7s %7s %7s %7s %7s\n","min","25%","50%","75%","max","min","25%","50%","75%","max","min","25%","50%","75%","max","min","25%","50%","75%","max"));

		writeResult(fw,t2,ep2,tp2,rp2,index2,HKMemIndex,"HK");
		writeResult(fw,t2,ep2,tp2,rp2,index2,HKCMemIndex,"HKC");
		writeResult(fw,t2,ep2,tp2,rp2,index2,ACMemIndex,"AC");
		fw.write("\n");
		writeResult2(fw,t2,index2,Simulation,"Sim");
		fw.write("\n");
		writeResult(fw,t2,ep2,tp2,rp2,index2,HKsim,"HKSim");
		writeResult(fw,t2,ep2,tp2,rp2,index2,HKCsim,"HKCsim");
		writeResult(fw,t2,ep2,tp2,rp2,index2,ACSim,"ACsim");
		fw.write("\n");
		writeResult2(fw,t2,index2,HKSimTotal,"HKSimTot");
		writeResult2(fw,t2,index2,HKCSimTotal,"HKCSimTot");
		writeResult2(fw,t2,index2,ACSimTotal,"ACSimTot");
		fw.write("\n");
		writeResult2(fw,t2,index2,Bisimulation,"Bisim");
		fw.write("\n");
//		writeResult(fw,t2,ep2,tp2,rp2,index2,HKBisim,"HKBisim");
//		writeResult(fw,t2,ep2,tp2,rp2,index2,HKCBisim,"HKCBisim");
//		writeResult(fw,t2,ep2,tp2,rp2,index2,ACBisim,"ACBisim");
//		fw.write("\n");
//		writeResult2(fw,t2,index2,HKBisimTotal,"HKBisTot");
//		writeResult2(fw,t2,index2,HKCBisimTotal,"HKCBisTot");
//		writeResult2(fw,t2,index2,ACBisimTotal,"ACBisTot");
//		fw.write("\n");
		writeResult(fw,t2,ep2,tp2,rp2,index2,HKmin,"HKmin");
		writeResult(fw,t2,ep2,tp2,rp2,index2,HKCmin,"HKCmin");
		writeResult(fw,t2,ep2,tp2,rp2,index2,ACmin,"ACmin");
		fw.write("\n");
		writeResult2(fw,t2,index2,HKminTotal,"HKminTot");
		writeResult2(fw,t2,index2,HKCminTotal,"HKCminTot");
		writeResult2(fw,t2,index2,ACminTotal,"ACminTot");
		fw.write("\n\n");
		
		writeData(t2,index2, "Not Incl");
	}
	
	public static void writeData(long[][] time,int count, String str) throws IOException
	{
		double d = 1000000.0;
		double data[][] = new double[time.length][time[0].length];
		for(int i=0;i<time.length;++i)
		{
			for(int j=0;j<time[0].length;++j)
			{
				data[i][j] = time[i][j]/d;
			}
		}
		
		fwData.write(String.format("\n\n Time\n"));
		
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("%s,%f,%f,%f\n","no",data[HKMemIndex][i],data[HKCMemIndex][i],data[ACMemIndex][i]));
			
			fwData.write(String.format("%s,%f,%f,%f\n","sim",data[HKsim][i],data[HKCsim][i],data[ACSim][i]));
			
			fwData.write(String.format("%s,%f,%f,%f\n","bisim",data[HKmin][i],data[HKCmin][i],data[ACmin][i]));
		}
		fwData.write(String.format("\n\n\n"));
		
		fwData.write(String.format("\n\n Total Time\n"));
		
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("%s,%f,%f,%f\n","no",data[HKMemIndex][i],data[HKCMemIndex][i],data[ACMemIndex][i]));
			
			fwData.write(String.format("%s,%f,%f,%f\n","sim",data[HKSimTotal][i],data[HKCSimTotal][i],data[ACSimTotal][i]));
			
			fwData.write(String.format("%s,%f,%f,%f\n","bisim",data[HKminTotal][i],data[HKCminTotal][i],data[ACminTotal][i]));
		}
		fwData.write(String.format("\n\n\n"));
		
		fwData.write(String.format("\n\n Sim and Bisim Time\n"));
		
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("%s,%f,%f\n",str,data[Simulation][i],data[Bisimulation][i]));
		}
		fwData.write(String.format("\n\n\n"));
		
		fwData.write(String.format("\n\n\n"));
		fwData.flush();
	}
	
	public void writeResult(FileWriter fw, long[][] t,int [][] ep, int[][] tp,int[][] rp,int count, int index, String str) throws IOException
	{
		if(count == 0)
			return;
		
		int min = 0;
		int q1 = count*25/100 - 1;
		int median = count*50/100 - 1;
		int q3 = count*75/100 - 1;
		int max = count - 1;
		
		q1 = q1 > 0 ? q1 : 0;
		median = median > 0 ? median : 0;
		q3 = q3 > 0 ? q3 : 0;
		max = max > 0 ? max : 0;
		
		double[][] time = new double[t.length][t[0].length];
		for(int a=0;a<t.length;++a)
		{
			for(int b=0;b<t[0].length;++b)
			{
				time[a][b] = t[a][b] / 1000000.0;
			}
		}
		
		fw.write(String.format("%10s:	    %12f %12f %12f %12f %12f 	%7d %7d %7d %7d %7d		%7d %7d %7d %7d %7d			%7d %7d %7d %7d %7d\n",str,time[index][min],time[index][q1],time[index][median],time[index][q3],time[index][max],
				ep[index][min],ep[index][q1],ep[index][median],ep[index][q3],ep[index][max],tp[index][min],tp[index][q1],tp[index][median],tp[index][q3],tp[index][max],rp[index][min],rp[index][q1],rp[index][median],rp[index][q3],rp[index][max]));
		
		fw.flush();
		
		writeSentenceDouble(time, min,q1,median,q3,max,index);
		fwBoxplot.write("\n");
		writeSentenceInt(tp, min,q1,median,q3,max,index);
		fwBoxplot.write("\n");
		writeSentenceInt(rp, min,q1,median,q3,max,index);
		fwBoxplot.write("\n\n");
		fwBoxplot.flush();
	}
	
	public void writeResult2(FileWriter fw, long[][] t,int count, int index, String str) throws IOException
	{
		if(count == 0)
			return;
		
		int min = 0;
		int q1 = count*25/100 - 1;
		int median = count*50/100 - 1;
		int q3 = count*75/100 - 1;
		int max = count - 1;
		
		q1 = q1 > 0 ? q1 : 0;
		median = median > 0 ? median : 0;
		q3 = q3 > 0 ? q3 : 0;
		max = max > 0 ? max : 0;
		
		double[][] time = new double[t.length][t[0].length];
		for(int a=0;a<t.length;++a)
		{
			for(int b=0;b<t[0].length;++b)
			{
				time[a][b] = t[a][b] / 1000000.0;
			}
		}
		
		fw.write(String.format("%10s:	    %12f %12f %12f %12f %12f \n",str,time[index][min],time[index][q1],time[index][median],time[index][q3],time[index][max]
				));
		
		fw.flush();
		
		writeSentenceDouble(time, min,q1,median,q3,max,index);
		fwBoxplot.write("\n");
		fwBoxplot.flush();
	}

	private NFAWithTwoSets saturate(NFAWithTwoSets nfa, BitSet[] set)
	{
		NFAWithTwoSets nfa_result = new NFAWithTwoSets(nfa);
    	BitSet[][] trans = nfa_result.getTransitionArray();
    	for(int st=0;st<nfa_result.getStateCount();st++)
    	{
    		for(int lb=0;lb<nfa_result.getActionCount();lb++)
    		{
    			for(int k=trans[st][lb].nextSetBit(0);k>=0;k=trans[st][lb].nextSetBit(k+1))
    			{
    				trans[st][lb].or(set[k]);
    			}
    		}
    	}
		
		BitSet s1 = nfa_result.getStateSet_1();
    	for(int k=s1.nextSetBit(0);k>=0;k=s1.nextSetBit(k+1))
		{
			s1.or(set[k]);
		}
    	nfa_result.setStateSet_1(s1);
		
		BitSet s2 = nfa_result.getStateSet_2();
    	for(int k=s2.nextSetBit(0);k>=0;k=s2.nextSetBit(k+1))
		{
			s2.or(set[k]);
		}
    	nfa_result.setStateSet_2(s2);
    	
    	return nfa_result;
	}
	
	private static void writeSentenceDouble(double[][] data, int min, int q1, int median, int q3, int max, int index) throws IOException
	{
		fwBoxplot.write("		\\addplot+[boxplot prepared={lower whisker=" + data[index][min] + ", lower quartile=" + data[index][q1] + ",median=" + data[index][median] + 
				", upper quartile=" + data[index][q3] + ",upper whisker=" + data[index][max] + "}]coordinates {};\n");
	}
	
	private static void writeSentenceInt(int[][] data, int min, int q1, int median, int q3, int max, int index) throws IOException
	{
		fwBoxplot.write("		\\addplot+[boxplot prepared={lower whisker=" + data[index][min] + ", lower quartile=" + data[index][q1] + ",median=" + data[index][median] + 
				", upper quartile=" + data[index][q3] + ",upper whisker=" + data[index][max] + "}]coordinates {};\n");
	}
}
