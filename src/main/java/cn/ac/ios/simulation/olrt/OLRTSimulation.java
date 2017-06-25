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
package cn.ac.ios.simulation.olrt;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import cn.ac.ios.nfa.NFABase;

public class OLRTSimulation {

	private List<List<Integer>> initPartition;
	private List<Block> partition;
	private MyArrayList[][] succ;
	private MyArrayList[][] pre;
	private int stateSize;
	private int labelSize;
	private BitSetBinaryRelation relation;
	private StateElement[] indexToStateElememt;
	
	private MyArrayList[] inLabelsOfEachState;
	
	private LinkedList<RemoveQueueElement> removeQueue;
	
	private List<List<StateElement>> delta;
	
	public OLRTSimulation(NFABase nfa)
	{
		stateSize = nfa.getStateCount();
		labelSize = nfa.getActionCount();
		
		initPartition = new ArrayList<List<Integer>>();
		List<Integer> acc = new ArrayList<Integer>();
		List<Integer> nonacc = new ArrayList<Integer>();
		BitSet accSet = nfa.getAcceptStateSet();
		for(int st=0;st<stateSize;++st)
		{
			if(accSet.get(st))
			{
				acc.add(st);
			}
			else
			{
				nonacc.add(st);
			}
		}
		initPartition.add(acc);
		initPartition.add(nonacc);
		
		//CAUTION! The length of this matrix is changed
		succ = new MyArrayList[labelSize][stateSize];
		pre = new MyArrayList[labelSize][stateSize];
		
		delta = new ArrayList<List<StateElement>>();
		
		for(int lb=0;lb<labelSize;++lb)
		{
			for(int st=0;st<stateSize;++st)
			{
				succ[lb][st] = new MyArrayList();
				pre[lb][st] = new MyArrayList();
			}
			
			delta.add(new ArrayList<StateElement>());
		}
		
		inLabelsOfEachState = new MyArrayList[stateSize];
		for(int i=0;i<stateSize;++i)
		{
			inLabelsOfEachState[i] = new MyArrayList();
		}
		
		BitSet[][] trans = nfa.getTransitionArray();
		for(int st=0;st<stateSize;++st)
		{
			for(int lb=0;lb<labelSize;++lb)
			{		
				for(int k=trans[st][lb].nextSetBit(0);k>=0;k=trans[st][lb].nextSetBit(k+1))
				{
					succ[lb][st].add(k);
					pre[lb][k].add(st);
				}
			}
		}
		
		for(int st=0;st<stateSize;++st)
		{
			for(int lb=0;lb<labelSize;++lb)
			{
				if(!pre[lb][st].isEmpty())
				{
					inLabelsOfEachState[st].add(lb);
				}
			}
		}
		
		relation = new BitSetBinaryRelation(2,stateSize);
		relation.set(0, 0, true);
		relation.set(1, 1, true);
		relation.set(1, 0, true);
		
		init();
	}
	
	public void init()
	{
		indexToStateElememt = new StateElement[stateSize];
		for(int i=0;i<stateSize;++i)
		{
			indexToStateElememt[i] = new StateElement(i);
		}
		
		partition = new ArrayList<Block>();
		for(int i=0;i<this.initPartition.size();++i)
		{
			makeBlock(this.initPartition.get(i),i);
		}
		
		removeQueue = new LinkedList<RemoveQueueElement>();
		
		for(int lb=0;lb<labelSize;++lb)
		{
			for(int st=0;st<stateSize;++st)
			{
				if(!succ[lb][st].isEmpty())
					delta.get(lb).add(indexToStateElememt[st]);
			}
		}
	}
	
	public BitSet[] getRelationSim()
	{
		BitSet[] result = new BitSet[stateSize];
		for(int st=0;st<stateSize;++st)
		{
			result[st] = new BitSet(stateSize);
			
			Block block = this.indexToStateElememt[st].block;
			
			for(int row = 0;row<this.partition.size();++row)
			{
				if(this.relation.get(row, block.index))
				{
					Block b2 = partition.get(row);
					
					assert(b2.index == row);
					
					StateElement elem = b2.firstState;
					do
					{
						result[st].set(elem.index);
						
						elem = elem.next;
					}while(elem != b2.firstState);
				}
			}
			
			assert(result[st].get(st));
		}
		
		return result;
	}
	
	public BitSet[] getRelationBeSim()
	{
		BitSet[] result = new BitSet[stateSize];
		for(int st=0;st<stateSize;++st)
		{
			result[st] = new BitSet(stateSize);
			
			Block block = this.indexToStateElememt[st].block;
			
			for(int col = 0;col<this.partition.size();++col)
			{
				if(this.relation.get(block.index, col))
				{
					Block b2 = partition.get(col);
					
					assert(b2.index == col);
					
					StateElement elem = b2.firstState;
					do
					{
						result[st].set(elem.index);
						
						elem = elem.next;
					}while(elem != b2.firstState);
				}
			}
			
			assert(result[st].get(st));
		}
		
		BitSet[] sim = this.getRelationSim();
		for(int i=0;i<stateSize;++i)
		{
			for(int st=sim[i].nextSetBit(0);st>=0;st=sim[i].nextSetBit(st+1))
			{
				assert(result[st].get(i));
			}
		}
		
		return result;
	}
	
	
	private void initRelation()
	{
		//boolean[][] mask = new boolean[this.partition.size()][labelSize];
		for(Block block : this.partition)
		{
			for(int lb=0;lb<labelSize;++lb)
			{
				//if one state has a-successor, the other states in the same block should also have
				if(!succ[lb][block.firstState.index].isEmpty())
				{
					//assert(!succ[lb][block.firstState.next.index].isEmpty());
					//assert(!succ[lb][block.firstState.pre.index].isEmpty());
					
					//mask[block.index][lb] = true;
					
					for(int bIndex = relation.first(block.index);bIndex>=0;bIndex=relation.next(block.index, bIndex))
					{
						if(succ[lb][this.partition.get(bIndex).firstState.index].isEmpty())
						{
							relation.set(block.index, bIndex, false);
						}
					}
				}
			}
		}
				
	}
	
	private void initCounterAndRemove()
	{
		for(Block block : this.partition)
		{
			block.counter = new HashMapCounter( stateSize);
			block.remove = new RemoveList[labelSize];
			
			boolean[] relatedBlocks = new boolean[this.partition.size()];
			for(int i=relation.first(block.index);i>=0;i=relation.next(block.index, i))
			{
				relatedBlocks[i] = true;
			}
			
			for(int lb=block.in_labels.nextSetBit(0);lb>=0;lb=block.in_labels.nextSetBit(lb+1))
			{
				for(StateElement st : delta.get(lb))
				{
					//counter
					int c = 0;
					ArrayList<Integer> dst = succ[lb][st.index].getList();
					for(int state : dst)
					{
						//if(relation.get(block.index, indexToStateElememt[state].block.index))
						if(relatedBlocks[indexToStateElememt[state].block.index])
							++c;
					}
					if(0 != c)
					{
						block.counter.set(lb, st.index, c);
					}
					else
					{
						if(block.remove[lb] == null)
						{
							block.remove[lb] = new RemoveList();
						}
						block.remove[lb].add(st);
						
						if(block.remove[lb].size() == 1)
							removeQueue.add(new RemoveQueueElement(block,lb));
					}
				}
			}
		}
	}
	
	public void computeSimulation()
	{
		for(int lb=0;lb<labelSize;++lb)
		{
			this.fastSplit(delta.get(lb));
		}
		
		//printPartitionAndRelation();
		
		assert(this.relation.size == this.partition.size());
		
		initRelation();
		
		//printPartitionAndRelation();
		
		initCounterAndRemove();
		
		//printRemoveQueue();
		//printCounterAndRemove();
		
		while(!removeQueue.isEmpty())
		{
			RemoveQueueElement e = removeQueue.poll();
			int lb = e.label;
			List<StateElement> remove = e.block.remove[lb].getList();
				
			//printRemoveQueueElement(e);
			
			e.block.remove[lb] = null;	
			List<Block> preBlocks = buildPre(e.block.firstState,lb);
			boolean[] removeMask = split(remove);
			
			//printPartitionAndRelation();
			
			for(Block b1 : preBlocks)
			{
				for(int col=relation.first(b1.index);col>=0;col=relation.next(b1.index, col))
				{
					if(!removeMask[col])
						continue;

					assert(b1.index != col);
					
					relation.set(b1.index, col,false);
					Block b2 = this.partition.get(col);
					
					assert(b2.index == col);
					
					for(int a = b2.in_labels.nextSetBit(0); a>=0; a = b2.in_labels.nextSetBit(a+1))
					{
						if(!b1.in_labels.get(a))
							continue;
						
						StateElement elem = b2.firstState;
						
						do
						{
							for(int preState : this.pre[a][elem.index].getList())
							{
								//b1.counter.decrease(a, preState);
								
								if(b1.counter.decrease(a, preState))
								{
//									if(b1.remove[a] == null)
//										b1.remove[a] = new RemoveList();
//									
//									b1.remove[a].add(this.indexToStateElememt[preState]);
//									
//									if(b1.remove[a].size() == 1)
//										this.removeQueue.add(new RemoveQueueElement(b1,a));
									
									if(b1.remove[a] == null || b1.remove[a].isEmpty())
									{
										b1.remove[a] = new RemoveList();
										b1.remove[a].add(this.indexToStateElememt[preState]);
										this.removeQueue.add(new RemoveQueueElement(b1,a));
									}
									else
									{
										b1.remove[a].add(this.indexToStateElememt[preState]);
									}
								}
							}
							elem = elem.next;
						} while(elem != b2.firstState);
					}
					
				}
			}
			
		}
		//printPartitionAndRelation();
		
		//System.out.println(this.partition.size());
	}
	
	List<Block> buildPre(StateElement state, int label)
	{
		List<Block> preBlocks = new ArrayList<Block>();
		boolean[] blockMask = new boolean[this.partition.size()];

		StateElement elem = state;
		do
		{
			List<Integer> src = pre[label][elem.index].getList();
			
			
			for(int st : src)
			{
				Block block = indexToStateElememt[st].block;
				
				if(blockMask[block.index])
					continue;
				
				blockMask[block.index] = true;
				preBlocks.add(block);
			}
			
			elem = elem.next;
		} while(elem != state);
		
		return preBlocks;
	}
	
	List<Block> internalSplit(List<StateElement> remove)
	{
		boolean[] blockMask = new boolean[this.partition.size()];
		
		List<Block> modifiedBlocks = new ArrayList<Block>();
		
		for(StateElement e : remove)
		{
			Block block = e.block;
			block.moveToTmp(e);
			
			if(!blockMask[block.index])
			{
				blockMask[block.index] = true;
				modifiedBlocks.add(block);
			}
		}
		
		return modifiedBlocks;
	}
	
	void makeBlock(List<Integer> states, int blockIndex)
	{
		int sz = states.size();
		assert(sz > 0);
		
		StateElement list = indexToStateElememt[states.get(sz-1)];
		
		for(int i=0;i<sz;++i)
		{
			StateElement.link(list, indexToStateElememt[states.get(i)]);
			
			list = list.next;
		}

		this.partition.add(new Block(list,sz,inLabelsOfEachState, labelSize, blockIndex));
	}
	
	void fastSplit(List<StateElement> remove)
	{
		List<Block> modifiedBlocks = internalSplit(remove);
		
		for(Block block : modifiedBlocks)
		{
			Block newBlock = block.trySplit();
			
			if(newBlock == null)
				continue;
			
			newBlock.index = this.partition.size();
			this.partition.add(newBlock);
			
			this.relation.split(block.index, newBlock.index);
		}
	}
	
	boolean[] split(List<StateElement> remove)
	{
		List<Block> modifiedBlocks = internalSplit(remove);
		//the size of blocks can not be larger than stateSize
		boolean[] removeMask = new boolean[stateSize];
		
		for(Block block : modifiedBlocks)
		{
			Block newBlock = block.trySplit();
			if(newBlock == null)
			{
				removeMask[block.index] = true;
				continue;
			}
			
			newBlock.index = this.partition.size();
			newBlock.counter = new HashMapCounter(stateSize);
			
			this.partition.add(newBlock);
			this.relation.split(block.index, newBlock.index);
			removeMask[newBlock.index] = true;
			newBlock.counter.copyCount(block.counter);
			
			newBlock.remove = new RemoveList[labelSize];
			for(int lb = newBlock.in_labels.nextSetBit(0);lb>=0;lb = newBlock.in_labels.nextSetBit(lb+1))
			{
				if(block.remove[lb] == null || block.remove[lb].isEmpty())
				{
					continue;
				}
				
				newBlock.remove[lb] = new RemoveList(block.remove[lb]);
				
				this.removeQueue.add(new RemoveQueueElement(newBlock,lb));
			}
		}

		return removeMask;
	}
	
	public void printPartitionAndRelation()
	{
		for(int i=0;i<this.partition.size();++i)
		{
			Block b = this.partition.get(i);
			StateElement elem = b.firstState;
			System.out.print("{" );
			do
			{
				System.out.print(" " + elem.index);
				elem = elem.next;
			} while(elem != b.firstState);
			System.out.print("}  " );
		}
		System.out.print("\n" );
		for(int i=0;i<this.partition.size();++i)
		{
			for(int j=0;j<this.partition.size();++j)
			{
				System.out.print(" " + relation.get(i, j));
			}
			System.out.print("\n" );
		}
	}
	
//	public void printCounterAndRemove()
//	{
//		for(int i=0;i<this.partition.size();++i)
//		{
//			Block b = this.partition.get(i);
//			StateElement elem = b.firstState;
//			System.out.print("{" );
//			do
//			{
//				System.out.print(" " + elem.index);
//				elem = elem.next;
//			} while(elem != b.firstState);
//			System.out.print("} \n" );
//			
//			for(int lb=0;lb<b.counter.labelSize;++lb)
//			{
//				for(int st=0;st<b.counter.stateSize;++st)
//				{
//					System.out.print(" " + b.counter.get(lb, st));
//				}
//				System.out.print(" \n" );
//			}
//			
//			for(int lb=0;lb<b.counter.labelSize;++lb)
//			{
//				if(b.remove[lb] == null)
//					continue;
//				
//				List<StateElement> list = b.remove[lb].getList();
//				System.out.print(lb + ":");
//				for(StateElement el : list)
//				{
//					System.out.print(el.index + " ");
//				}
//				System.out.print(" \n" );
//			}
//			
//			System.out.print(" \n" );
//		}
//	}
	
	public void printRemoveQueue()
	{
		int size = this.removeQueue.size();
		System.out.println("remove queue size: " + size);
		
		for(int i=0;i<size;++i)
		{
			RemoveQueueElement e = this.removeQueue.get(i);
			System.out.println("block index: " + e.block.index + " label: " + e.label + "   remove: ");
			List<StateElement> l = e.block.remove[e.label].getList();
			for(StateElement st : l)
			{
				System.out.print(st.index + " ");
			}
			System.out.println();
		}
	}
	
	public void printRemoveQueueElement(RemoveQueueElement e)
	{
		System.out.println("block index: " + e.block.index + " label: " + e.label + "   remove: ");
		List<StateElement> l = e.block.remove[e.label].getList();
		for(StateElement st : l)
		{
			System.out.print(st.index + " ");
		}
		System.out.println();
	}
}
