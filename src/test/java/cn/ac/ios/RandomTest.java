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

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

import cn.ac.ios.antichain.AC;
import cn.ac.ios.antichain.AntichainPair;
import cn.ac.ios.bdd.HKBDD;
import cn.ac.ios.bdd.HKCBDD;
import cn.ac.ios.bdd.NFAWithTwoSetsBDD;
import cn.ac.ios.common.RandomNFA;
import cn.ac.ios.common.ReturnValue;
import cn.ac.ios.common.StateSetPair;
import cn.ac.ios.hk.HK;
import cn.ac.ios.hkc.HKC;
import cn.ac.ios.myqueue.BFS;
import cn.ac.ios.myqueue.HashSetBFS;
import cn.ac.ios.nfa.NFAWithTwoSets;
import net.sf.javabdd.BDD;

public class RandomTest {

	enum Stt {
		BFS, DFS
	}
	enum PreOperation {
		Sim, Bisim,None
	}
	
	static int HKIndex = 0;
	static int HKCIndex = 1;
	static int ACIndex = 2;
	static int HKBDDIndex = 4;
	static int HKCBDDIndex = 3;
	static int HKMemIndex = 5;
	static int HKCMemIndex = 6;
	static int ACMemIndex = 7;
	
	static int algNum = 8; 
	static int repeat = 4;
	static int count = 100;
	
	static int skip = 5;
	
	static FileWriter fwBoxplot;
	static FileWriter fwData;
	
	public static void main(String[] args) throws IOException
	{
		//Stt strategy = Stt.BFS;
		
		double[][] data = {{3,262144,2.8},{3,16384,2.8},{3,262144,2.8}};
		int g = data.length;
		
		
		String filename =  count + "result.txt";
		FileWriter fw = new FileWriter(filename);
		fwBoxplot = new FileWriter("boxplot.txt");
		fwData = new FileWriter("data.txt");

		for(int i=0;i<g;i++)
		{
			int stateNum = (int)data[i][0];
			int actionNum = (int)data[i][1];
			double density = data[i][2];
						
			fw.write("State number of the NFA: " + stateNum + "\n");
			fw.write("Action number of the NFA: " + actionNum + "\n");
			fw.write("Density of the NFA: " + density + "\n");
			fw.flush();
			
			fwData.write("State number of the NFA: " + stateNum + "\n");
			fwData.write("Action number of the NFA: " + actionNum + "\n");
			fwData.write("Density of the NFA: " + density + "\n");
			fwData.write("count = " + count + "  repeat = " + repeat + "\n");
			fwData.flush();

			long[][] time = new long[algNum][count];
			ReturnValue[][] result = new ReturnValue[algNum][count];
			int loop = 0;
			while(loop < count)
			{
				//NFA nfa1 = new RandomNFA().generateRandomNFA_second(stateNum, actionNum, transition, 0);
				//NFA nfa2 = new RandomNFA().generateRandomNFA_second(stateNum, actionNum, transition, 0);
//				NFA nfa1 = new RandomNFA().generateRandomNFA(stateNum, actionNum, density, 0);
//				NFA nfa2 = new RandomNFA().generateRandomNFA(stateNum, actionNum, density, 0);
//				NFAUnion nfaUnion = new NFAUnion(nfa1,nfa2);
				
				System.out.println(loop +" " + i);
				
				NFAWithTwoSets nfaWithTwoSets = new RandomNFA().generateRandomNFAWithTwoSets(stateNum, actionNum, density, 0);
				//NFAWithTwoSets nfaWithTwoSets = generateSpecialNFA(5);
				
				long averageTime = 0;
				
//				ReturnValue r_hk = null;
//				for(int a=0;a<repeat;++a)
//				{
//					long startTime_hk = System.nanoTime();
//					HK hk = new HK(nfaWithTwoSets,new BFS<StateSetPair<BitSet>>());
//					r_hk = hk.checkEquiv();
//					long endTime_hk = System.nanoTime();
//					averageTime += endTime_hk - startTime_hk;
//				}
//				time[HKIndex][loop] = averageTime/repeat;
//				result[HKIndex][loop] = r_hk;
				
				
				//nfaWithTwoSets.print();
				
				
				if(i<skip)
				{
					averageTime = 0;
					ReturnValue r_hkmem = null;
					for(int a=0;a<repeat;++a)
					{
						long startTime_hkmem = System.nanoTime();
						HK hkmem = new HK(nfaWithTwoSets,new HashSetBFS<StateSetPair<BitSet>>());	
						r_hkmem = hkmem.checkEquivMem();
						long endTime_hkmem = System.nanoTime();
						averageTime += endTime_hkmem - startTime_hkmem;;
					}
					time[HKMemIndex][loop] = averageTime/repeat;
					result[HKMemIndex][loop] = r_hkmem;
				}
				

//				averageTime = 0;
//				ReturnValue r_hkc = null;
//				for(int a=0;a<repeat;++a)
//				{
//					long startTime_hkc = System.nanoTime();
//					HKC hkc = new HKC(nfaWithTwoSets,new BFS<StateSetPair<BitSet>>());
//					r_hkc = hkc.checkEquiv();
//					long endTime_hkc = System.nanoTime();
//					averageTime += endTime_hkc - startTime_hkc;
//				}
//				time[HKCIndex][loop] = averageTime/repeat;
//				result[HKCIndex][loop] = r_hkc;
				
				averageTime = 0;
				ReturnValue r_hkcmem = null;
				for(int a=0;a<repeat;++a)
				{
					long startTime_hkcmem = System.nanoTime();
					HKC hkcmem = new HKC(nfaWithTwoSets,new HashSetBFS<StateSetPair<BitSet>>());
					r_hkcmem = hkcmem.checkEquivMem();
					long endTime_hkcmem = System.nanoTime();
					averageTime += endTime_hkcmem - startTime_hkcmem;
				}
				time[HKCMemIndex][loop] = averageTime/repeat;
				result[HKCMemIndex][loop] = r_hkcmem;
				
				NFAWithTwoSets nfaWithTwoSets2 = new NFAWithTwoSets(nfaWithTwoSets);
				BitSet set = nfaWithTwoSets2.getStateSet_1();
				nfaWithTwoSets2.setStateSet_1(nfaWithTwoSets2.getStateSet_2());
				nfaWithTwoSets2.setStateSet_2(set);
				
				assert(!nfaWithTwoSets2.equals(nfaWithTwoSets));
				
//				averageTime = 0;
//				ReturnValue r_ac = null;
//				for(int a=0;a<repeat;++a)
//				{
//					long startTime_ac = System.nanoTime();
//					AC ac1 = new AC(nfaWithTwoSets,new BFS<AntichainPair>());
//					r_ac = ac1.checkIncl();
//					if(r_ac.isResult())
//					{
//						AC ac2 = new AC(nfaWithTwoSets2,new BFS<AntichainPair>());
//						r_ac.add(ac2.checkIncl());
//					}
//					long endTime_ac = System.nanoTime();
//					averageTime += endTime_ac - startTime_ac;
//				}
//				time[ACIndex][loop] = averageTime/repeat;
//				result[ACIndex][loop] = r_ac;
				
				
//				averageTime = 0;
//				ReturnValue r_acmem = null;
//				for(int a=0;a<repeat;++a)
//				{
//					long startTime_acmem = System.nanoTime();
//					AC ac12 = new AC(nfaWithTwoSets,new HashSetBFS<AntichainPair>());
//					r_acmem = ac12.checkInclMem();
//					if(r_acmem.isResult())
//					{
//						AC ac22 = new AC(nfaWithTwoSets2,new HashSetBFS<AntichainPair>());
//						r_acmem.add(ac22.checkInclMem());
//					}
//					long endTime_acmem = System.nanoTime();
//					averageTime += endTime_acmem - startTime_acmem;
//				}
//				time[ACMemIndex][loop] = averageTime/repeat;
//				result[ACMemIndex][loop] = r_acmem;
				
				NFAWithTwoSetsBDD bdd = new NFAWithTwoSetsBDD(nfaWithTwoSets);
				
				averageTime = 0;
				ReturnValue r_hkbdd = null;
				for(int a=0;a<repeat;++a)
				{
					long startTime_hkbdd = System.nanoTime();
					HKBDD hkbdd = new HKBDD(bdd,new BFS<StateSetPair<BDD>>());
					r_hkbdd = hkbdd.checkEquiv();
					long endTime_hkbdd = System.nanoTime();
					averageTime += endTime_hkbdd - startTime_hkbdd;
				}
				time[HKBDDIndex][loop] = averageTime/repeat;
				result[HKBDDIndex][loop] = r_hkbdd;
				
				averageTime = 0;
				ReturnValue r_hkcbdd = null;
				for(int a=0;a<repeat;++a)
				{
					long startTime_hkcbdd = System.nanoTime();
					HKCBDD hkcbdd = new HKCBDD(bdd,new BFS<StateSetPair<BDD>>());
					r_hkcbdd = hkcbdd.checkEquiv();
					long endTime_hkcbdd = System.nanoTime();
					averageTime += endTime_hkcbdd - startTime_hkcbdd;
				}
				time[HKCBDDIndex][loop] =  averageTime/repeat;
				result[HKCBDDIndex][loop] = r_hkcbdd;
				
				loop++;
			}

			
			//writeData(time, result, count);
			writeDataForBDD(time, result, count);
			//writeDataFor4(time, result, count,i);
			
			sortAndPrint(fw,time,result,count,stateNum, actionNum, density);

		}
		fw.close();
		fwBoxplot.close();
		fwData.close();
		System.out.println("finish");
	}
	
	public static void writeData(long[][] time,ReturnValue[][] result,int count) throws IOException
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
		
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("HK,%f,%f\n",data[HKIndex][i],data[HKMemIndex][i]));
			fwData.write(String.format("HKC,%f,%f\n",data[HKCIndex][i],data[HKCMemIndex][i]));
			fwData.write(String.format("AC,%f,%f\n",data[ACIndex][i],data[ACMemIndex][i]));
		}
		fwData.write(String.format("\n todo pairs\n\n"));
		
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("HK,%d,%d\n",result[HKIndex][i].getTodoPairsNumber(),result[HKMemIndex][i].getTodoPairsNumber()));
			fwData.write(String.format("HKC,%d,%d\n",result[HKCIndex][i].getTodoPairsNumber(),result[HKCMemIndex][i].getTodoPairsNumber()));
			fwData.write(String.format("AC,%d,%d\n",result[ACIndex][i].getTodoPairsNumber(),result[ACMemIndex][i].getTodoPairsNumber()));
		}
		fwData.write(String.format("\n R pairs\n\n"));
		
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("HK,%d,%d\n",result[HKIndex][i].getrPairsNumber(),result[HKMemIndex][i].getrPairsNumber()));
			fwData.write(String.format("HKC,%d,%d\n",result[HKCIndex][i].getrPairsNumber(),result[HKCMemIndex][i].getrPairsNumber()));
			fwData.write(String.format("AC,%d,%d\n",result[ACIndex][i].getrPairsNumber(),result[ACMemIndex][i].getrPairsNumber()));
		}
		fwData.write(String.format("\n\n\n"));
		fwData.flush();
	}
	
	public static void writeDataForBDD(long[][] time,ReturnValue[][] result,int count) throws IOException
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
		fwData.write(String.format("algorithm,explicit,symbolic"));
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("HK,%f,%f\n",data[HKMemIndex][i],data[HKBDDIndex][i]));
			fwData.write(String.format("HKC,%f,%f\n",data[HKCMemIndex][i],data[HKCBDDIndex][i]));
		}
		
		fwData.write(String.format("\n todo pairs\n\n"));
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("HK,%d,%d\n",result[HKMemIndex][i].getTodoPairsNumber(),result[HKBDDIndex][i].getTodoPairsNumber()));
			fwData.write(String.format("HKC,%d,%d\n",result[HKCMemIndex][i].getTodoPairsNumber(),result[HKCBDDIndex][i].getTodoPairsNumber()));
		}
		
		fwData.write(String.format("\n R pairs\n\n"));
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("HK,%d,%d\n",result[HKMemIndex][i].getrPairsNumber(),result[HKBDDIndex][i].getrPairsNumber()));
			fwData.write(String.format("HKC,%d,%d\n",result[HKCMemIndex][i].getrPairsNumber(),result[HKCBDDIndex][i].getrPairsNumber()));
		}
		fwData.write(String.format("\n\n\n"));
		fwData.flush();
	}
	
	public static void writeDataFor3(long[][] time,ReturnValue[][] result,int count, int setting) throws IOException
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
		fwData.write(String.format("algorithm,HK,HKC,AC\n"));
		for(int i=0;i<count;++i)
		{
			if(setting<skip)
				fwData.write(String.format("setting%d,%f,%f,%f\n",setting,data[HKMemIndex][i],data[HKCMemIndex][i],data[ACMemIndex][i]));
			else
				fwData.write(String.format("setting%d,%f,%f,%f\n",setting,-1.0,data[HKCMemIndex][i],data[ACMemIndex][i]));
		}
		
		fwData.write(String.format("\n todo pairs\n\n"));
		for(int i=0;i<count;++i)
		{			
			if(setting<skip)
				fwData.write(String.format("setting%d,%d,%d,%d\n",setting,result[HKMemIndex][i].getTodoPairsNumber(),result[HKCMemIndex][i].getTodoPairsNumber(),result[ACMemIndex][i].getTodoPairsNumber()));
			else
				fwData.write(String.format("setting%d,%d,%d,%d\n",setting,-1,result[HKCMemIndex][i].getTodoPairsNumber(),result[ACMemIndex][i].getTodoPairsNumber()));
		}
		
		fwData.write(String.format("\n R pairs\n\n"));
		for(int i=0;i<count;++i)
		{
			if(setting<skip)
				fwData.write(String.format("setting%d,%d,%d,%d\n",setting,result[HKMemIndex][i].getrPairsNumber(),result[HKCMemIndex][i].getrPairsNumber(),result[ACMemIndex][i].getrPairsNumber()));
			else
				fwData.write(String.format("setting%d,%d,%d,%d\n",setting,-1,result[HKCMemIndex][i].getrPairsNumber(),result[ACMemIndex][i].getrPairsNumber()));
		}
		fwData.write(String.format("\n\n\n"));
		fwData.flush();
	}

	public static void writeDataFor4(long[][] time,ReturnValue[][] result,int count, int setting) throws IOException
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
		fwData.write(String.format("algorithm,HKC,AC\n"));
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("setting%d,%f,%f\n",setting,data[HKCMemIndex][i],data[ACMemIndex][i]));
		}
		
		fwData.write(String.format("\n todo pairs\n\n"));
		for(int i=0;i<count;++i)
		{			
			fwData.write(String.format("setting%d,%d,%d\n",setting,result[HKCMemIndex][i].getTodoPairsNumber(),result[ACMemIndex][i].getTodoPairsNumber()));
		}
		
		fwData.write(String.format("\n R pairs\n\n"));
		for(int i=0;i<count;++i)
		{
			fwData.write(String.format("setting%d,%d,%d\n",setting,result[HKCMemIndex][i].getrPairsNumber(),result[ACMemIndex][i].getrPairsNumber()));
		}
		fwData.write(String.format("\n\n\n"));
		fwData.flush();
	}
	
	public static void sortAndPrint(FileWriter fw, long[][] time,ReturnValue[][] result,int count, int stateNum, int actionNum, double density) throws IOException
	{
		//boolean check = result[HKMemIndex][0].isResult();
		boolean check = true;
		
		int[][] ep = new int[algNum][count];
		int[][] tp = new int[algNum][count];
		int[][] rp = new int[algNum][count];
		for(int i=0;i<algNum;i++)
		{
			for(int j=0;j<count;j++)
			{
				if(result[i][j] != null)
				{
					if(result[i][j].isResult() != check)
					{
						System.out.println("ERR");
						System.exit(0);
					}
	
					ep[i][j] = result[i][j].getTouchedPairsNumber();
					tp[i][j] = result[i][j].getTodoPairsNumber();
					rp[i][j] = result[i][j].getrPairsNumber();
				}
			}
		}

		for(int i=0;i<algNum;i++)
		{
			Arrays.sort(time[i]);
			Arrays.sort(ep[i]);
			Arrays.sort(tp[i]);
			Arrays.sort(rp[i]);
		}
		
		int min = 0;
		int q1 = count*25/100 - 1;
		int median = count*50/100 - 1;
		int q3 = count*75/100 - 1;
		int max = count - 1;
		
//		if(count < 4)
//		{
//			q1=0;
//			median = 0;
//			q3 = 0;
//		}
		
		fw.write(String.format("             %20s     	   				%28s     					 	%28s       		    		%28s\n","Time (nano seconds)","touched pairs ","pairs in todo","pairs in R"));
		fw.write(String.format("	        %8s %8s %8s %8s %8s		%8s %8s %8s %8s %8s		%8s %8s %8s %8s %8s			%8s %8s %8s %8s %8s\n","min","25%","50%","75%","max","min","25%","50%","75%","max","min","25%","50%","75%","max","min","25%","50%","75%","max"));
		
		writeResult(fw,time,ep,tp,rp,HKIndex,"HK",min,q1, median, q3,max);
		writeResult(fw,time,ep,tp,rp,HKMemIndex,"HKMem",min,q1, median, q3,max);
		writeResult(fw,time,ep,tp,rp,HKBDDIndex,"HKBDD",min,q1, median, q3,max);
		writeResult(fw,time,ep,tp,rp,HKCIndex,"HKC",min,q1, median, q3,max);
		writeResult(fw,time,ep,tp,rp,HKCMemIndex,"HKCMem",min,q1, median, q3,max);
		writeResult(fw,time,ep,tp,rp,HKCBDDIndex,"HKCBDD",min,q1, median, q3,max);
		writeResult(fw,time,ep,tp,rp,ACIndex,"AC",min,q1, median, q3,max);
		writeResult(fw,time,ep,tp,rp,ACMemIndex,"ACMem",min,q1, median, q3,max);
		fw.write("\n\n");
		
		writeBoxplotCode(time,tp,rp,stateNum,actionNum,density,min,q1, median, q3,max);
	}
	
	public static void writeResult(FileWriter fw, long[][] time,int [][] ep, int[][] tp,int[][] rp, int index, String str, int min, int q1, int median, int q3, int max) throws IOException
	{
		fw.write(String.format("%6s:	   %8d %8d %8d %8d %8d		%8d %8d %8d %8d %8d		%8d %8d %8d %8d %8d			%8d %8d %8d %8d %8d\n",str,time[index][min],time[index][q1],time[index][median],time[index][q3],time[index][max],
				ep[index][min],ep[index][q1],ep[index][median],ep[index][q3],ep[index][max],tp[index][min],tp[index][q1],tp[index][median],tp[index][q3],tp[index][max],rp[index][min],rp[index][q1],rp[index][median],rp[index][q3],rp[index][max]));
		
		fw.flush();
	}
	
	public static void writeBoxplotCode(long[][] time, int[][] tp,int[][] rp, int stateNum, int actionNum, double density, int min, int q1, int median, int q3, int max) throws IOException
	{
		fwBoxplot.write("\\begin{figure}[H]\n");
		fwBoxplot.write("\\centering\n");
		fwBoxplot.write("\\subfigure[]{\n");
		fwBoxplot.write("\\begin{tikzpicture}\n");
		fwBoxplot.write("	\\begin{axis}[\n");
		fwBoxplot.write("		xmode=normal,ymode=log,boxplot/draw direction=y,\n");
		fwBoxplot.write("		xlabel=algorithms,ylabel=time (ms),xtick={1,2,3},xticklabels={HK,HKC,AC},width = 7.1cm]\n");
		writeBoxplotElementTime(time,min,q1, median, q3,max);
//		fwBoxplot.write("	\\end{axis}\n");
//		fwBoxplot.write("\\end{tikzpicture}\n");
//		fwBoxplot.write("}\n");
//		fwBoxplot.write("\\subfigure[]{\n");
//		fwBoxplot.write("\\begin{tikzpicture}\n");
//		fwBoxplot.write("	\\begin{axis}[\n");
//		fwBoxplot.write("		xmode=normal,ymode=log,boxplot/draw direction=y,\n");
//		fwBoxplot.write("		xlabel=algorithms,ylabel=pairs in todo,xtick={1,2,3,4},xticklabels={HK,HKBDD,HKC,HKCBDD},width = 7.1cm]\n");
//		writeBoxplotElementNumber(tp,min,q1, median, q3,max);
//		fwBoxplot.write("	\\end{axis}\n");
//		fwBoxplot.write("\\end{tikzpicture}\n");
//		fwBoxplot.write("}\n");
//		fwBoxplot.write("\\subfigure[]{\n");
//		fwBoxplot.write("\\begin{tikzpicture}\n");
//		fwBoxplot.write("	\\begin{axis}[\n");
//		fwBoxplot.write("		xmode=normal,ymode=log,boxplot/draw direction=y,\n");
//		fwBoxplot.write("		xlabel=algorithms,ylabel=pairs in R,xtick={1,2,3,4},xticklabels={HK,HKBDD,HKC,HKCBDD},width = 7.1cm]\n");
//		writeBoxplotElementNumber(rp,min,q1, median, q3,max);
//		fwBoxplot.write("	\\end{axis}\n");
//		fwBoxplot.write("\\end{tikzpicture}\n");
//		fwBoxplot.write("}\n");
//		fwBoxplot.write("\\caption{states:"+ stateNum +", letters:"+ actionNum +", density: " + density + "}\n");
//		fwBoxplot.write("\\label{fig:MEM1}\n");
//		fwBoxplot.write("\\end{figure}\n");
		fwBoxplot.flush();
	}
	
	private static void writeBoxplotElementTime(long[][] time, int min, int q1, int median, int q3, int max) throws IOException
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
		
		writeSentenceDouble(data,min,q1, median, q3,max,HKMemIndex);
		writeSentenceDouble(data,min,q1, median, q3,max,HKCMemIndex);
		writeSentenceDouble(data,min,q1, median, q3,max,ACMemIndex);
	}
	
	private static void writeBoxplotElementNumber(int[][] data, int min, int q1, int median, int q3, int max) throws IOException
	{
		writeSentenceInt(data,min,q1, median, q3,max,HKMemIndex);
		writeSentenceInt(data,min,q1, median, q3,max,HKCMemIndex);
		writeSentenceInt(data,min,q1, median, q3,max,ACMemIndex);
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

	public static NFAWithTwoSets generateSpecialNFA(int k)
	{
		int num = (int) Math.pow(2, k);
		NFAWithTwoSets nfa = new NFAWithTwoSets(3,num);
		BitSet[][] tran = new BitSet[3][num];
		for(int i=0;i<3;i++)
		{
			for(int j=0;j<num;j++)
			{
				tran[i][j] = new BitSet();
			}
		}
		for(int i=0;i<2;i++)
		{
			for(int j=0;j<num;j++)
			{
				tran[i][j] = new BitSet();
				tran[i][j].set(2);
			}
		}
		nfa.setTransitionArray(tran);
		
		BitSet acc = new BitSet();
		acc.set(2);
		nfa.setAcceptStateSet(acc);
		
		BitSet i1 = new BitSet();
		i1.set(0);
		nfa.setStateSet_1(i1);
		
		BitSet i2 = new BitSet();
		i2.set(1);
		nfa.setStateSet_2(i2);

		return nfa;
	}
}
