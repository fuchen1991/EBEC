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
import java.util.List;

public class Block {

	List<StateElement> tmp;	
	StateElement firstState;

	int[] in_labels_count;
	BitSet in_labels;
	
	int size;
	int index;
	
	Block parent;
	
	HashMapCounter counter;
	RemoveList[] remove;
	
	MyArrayList[] inLabelsOfEachState;
	
	public Block(StateElement e, int size, MyArrayList[] inLabelsOfEachState, int labelSize, int index)
	{
		this.index = index;
		this.inLabelsOfEachState = inLabelsOfEachState;

		in_labels_count = new int[labelSize];
		in_labels = new BitSet(labelSize);
		
		this.size = size;
		this.firstState = e;
		
		do
		{
			e.block = this;
			
			List<Integer> list = inLabelsOfEachState[e.index].getList();
			for(int lb : list)
			{
				++in_labels_count[lb];
				if(in_labels_count[lb] == 1)
					in_labels.set(lb);
			}

			e = e.next;
		} while (e != this.firstState);
		
	}
	
	public Block(StateElement e, int size, Block parent,MyArrayList[] inLabelsOfEachState)
	{
		this.firstState = e;
		this.size = size;
		this.parent = parent;
		this.inLabelsOfEachState = inLabelsOfEachState;
		
		in_labels_count = new int[parent.in_labels_count.length];
		in_labels = new BitSet(parent.in_labels_count.length);
		
		//update inLabels here
		
		do
		{
			e.block = this;
			List<Integer> list = inLabelsOfEachState[e.index].getList();
			for(int lb : list)
			{
				--parent.in_labels_count[lb];
				if(parent.in_labels_count[lb] == 0)
				{
					parent.in_labels.clear(lb);
				}
				
				++in_labels_count[lb];
				if(in_labels_count[lb] == 1)
					in_labels.set(lb);
			}

			e = e.next;
		} while (e != this.firstState);
		
	}
	
	public void moveToTmp(StateElement state)
	{
		if(tmp == null)
			tmp = new ArrayList<StateElement>();
		tmp.add(state);
	}
	
	
	//trySplit does NOT update inLabels
	public Block trySplit()
	{
		int sz = this.tmp.size();
		assert(sz != 0);
		
		if(sz == this.size)
		{
			this.tmp.clear();
			return null;
		}
		
		StateElement last = tmp.get(sz - 1);
		tmp.remove(sz - 1);
		this.firstState = last.next;
		StateElement.link(last.pre, last.next);
		
		if(tmp.isEmpty())
		{
			StateElement.link(last, last);
			
			--this.size;
			
			return new Block(last, 1, this,this.inLabelsOfEachState);
		}
		
		StateElement elem = last;
		for(StateElement state : tmp)
		{
			this.firstState = state.next;
			
			StateElement.link(state.pre, state.next);
			StateElement.link(elem, state);
			
			elem = state;
		}
		
		StateElement.link(elem, last);
		
		int newBlockSize = sz;
		this.tmp.clear();
		
		assert(newBlockSize < this.size);
		
		this.size -= newBlockSize;
		
		return new Block(last, newBlockSize, this,this.inLabelsOfEachState);
	}
	
}
