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

import java.util.BitSet;

import cn.ac.ios.nfa.NFAWithTwoSets;

public class PartitionRefinementBisimulation {
	
	RefinablePartition B = new RefinablePartition();
	RefinablePartition T = new RefinablePartition();
	int nr_states=0, nr_labels=0, nr_l_trans=0, nr_w_trans=0, nr_init_blocks=0;
	int nr_l_max1=Integer.MAX_VALUE/2 + 1;
	MyArray l_tails = new MyArray(), l_labels = new MyArray(), l_heads = new MyArray();
	MyArray in_l_trans = new MyArray(), in_l_ends = new MyArray();
	MyArray touched_states = new MyArray(), touched_sets = new MyArray();
	int cord_splitter=2;
	int nr_counters=0;
	MyArray links = new MyArray(), t_counts = new MyArray(), s_counts = new MyArray();
	int bisim_splitter=1;
	phase_type phase = phase_type.initial;
	int l_cnt=0,nr_l_out=0;
	MyArray init_block_borders = new MyArray();
	
	private BitSet[][] tran;
	
	enum phase_type
	{
		initial, sizes_given, minimized;
	}
	
	public PartitionRefinementBisimulation(NFAWithTwoSets nfa)
	{
		nr_states = nfa.getStateCount();
		nr_labels = nfa.getActionCount();
		int num = 0;
		for(int i=0;i<nr_states;++i)
		{
			for(int j=0;j<nr_labels;++j)
			{
				num += nfa.getTransitionArray()[i][j].cardinality();
			}
		}
		nr_l_trans = num;
		nr_init_blocks = 2;
		phase = phase_type.sizes_given;
		
		//for bisimulation, states, labels and blocks are all starting from 1, but other parts are from 0.
		for(int i=0;i<nr_states;++i)
		{
			for(int j=0;j<nr_labels;++j)
			{
				BitSet heads = nfa.getTransitionArray()[i][j];
				for(int k=heads.nextSetBit(0);k>=0;k=heads.nextSetBit(k+1))
				{
					if( 0 == l_cnt )
					{
						l_tails.make_size( nr_l_trans );
					    l_labels.make_size( nr_l_trans );
					    l_heads.make_size( nr_l_trans );
					}

					++l_cnt;

					l_tails.setItem(l_cnt, i+1);
					l_heads.setItem(l_cnt, k+1);
					l_labels.setItem(l_cnt, j+1);

					if( l_cnt == nr_l_trans )
					{
						T.elems.make_size( nr_l_trans ); 
						T.ends.make_size( nr_l_trans );
						group_l_transitions();
					}
				}
			}
		}
		
		if( !B.sets.active() )
		{
		    B.sets.make_size( nr_states );
		    for( int st = 1; st <= nr_states; ++st )
		    { 
		    	B.sets.setItem(st, 1);
		    }
		}
		
		BitSet acc = nfa.getAcceptStateSet();
		for(int i=acc.nextSetBit(0);i>=0;i=acc.nextSetBit(i+1))
		{
			B.sets.setItem(i+1, 2);
		}
	}
	
	public void computeBisimulation()
	{
		minimize();
	}
	
	public NFAWithTwoSets getMinimizedNFA(NFAWithTwoSets nfa)
	{
		NFAWithTwoSets minimizedNFA = new NFAWithTwoSets(B.nr_sets, this.nr_labels);
		
		if(tran == null)
		{
			tran = new BitSet[B.nr_sets][this.nr_labels];
			for(int i=0;i<B.nr_sets;i++)
			{
				for(int j=0;j<this.nr_labels;++j)
				{
					tran[i][j] = new BitSet(B.nr_sets);
				}
			}
			
			int tranNum = this.nr_l_out;
			int given_tr = 0;
			int tail=0, label=0,head=0;
			for(int tr = 0;tr<tranNum;++tr)
			{
				if(given_tr >=this.nr_l_trans)
					break;
				int st = 0;
				do
				{
					++given_tr;
					st = l_tails.getItem(given_tr);
					tail = B.sets.getItem(st);
				} while (st != B.elems.getItem(B.begins.getItem(tail)) || this.t_counts.getItem(this.links.getItem(given_tr)) != 0);
				
				this.t_counts.setItem(this.links.getItem(given_tr), 1);
				label = this.l_labels.getItem(given_tr);
				head = B.sets.getItem(this.l_heads.getItem(given_tr));
				
				//CAUTION!!
				tran[tail - 1][label -1].set(head-1);
			}
			minimizedNFA.setTransitionArray(tran);
		}
		else
		{
			minimizedNFA.setTransitionArray(tran);
		}
		
		BitSet acc = new BitSet(B.nr_sets);
		for(int st=nfa.getAcceptStateSet().nextSetBit(0);st>=0;st=nfa.getAcceptStateSet().nextSetBit(st+1))
		{
			acc.set(B.sets.getItem(st+1)-1);
		}
		minimizedNFA.setAcceptStateSet(acc);
		
		BitSet set1 = new BitSet(B.nr_sets);
		for(int st=nfa.getStateSet_1().nextSetBit(0);st>=0;st=nfa.getStateSet_1().nextSetBit(st+1))
		{
			set1.set(B.sets.getItem(st+1)-1);
		}
		minimizedNFA.setStateSet_1(set1);
		
		BitSet set2 = new BitSet(B.nr_sets);
		for(int st=nfa.getStateSet_2().nextSetBit(0);st>=0;st=nfa.getStateSet_2().nextSetBit(st+1))
		{
			set2.set(B.sets.getItem(st+1)-1);
		}
		minimizedNFA.setStateSet_2(set2);
		
		return minimizedNFA;
	}
	
	
	
	public BitSet[] getRelation()
	{
		BitSet[] set = new BitSet[nr_states];
		for(int i=0;i<nr_states;i++)
		{
			set[i] = new BitSet(nr_states);
		}
		for(int i=0;i<nr_states;i++)
		{
			for(int j=i;j<nr_states;j++)
			{
				if(B.sets.getItem(i+1) == B.sets.getItem(j+1))
				{
					set[i].set(j);
					set[j].set(i);
				}
			}
		}
		
		return set;
	}
	
	private void fix_heap(MyArray elems, int beg, int parent, int end, MyArray key)
	{
	  int child = 2 * parent - beg + 1;
	  int tmp_el = elems.getItem(parent);
	  while (child < end)
	  {
		if (child + 1 < end && key.getItem(elems.getItem(child)) < key.getItem(elems.getItem(child+1)))
		{
			++child;
		}
		if (!(key.getItem(tmp_el) < key.getItem(elems.getItem(child))))
		{
			break;
		}
		elems.setItem(parent, elems.getItem(child));

		parent = child;
		child = 2 * parent - beg + 1;
	  }
	  elems.setItem(parent,tmp_el);
	}

	private void heapsort(MyArray elems, int beg, int end, MyArray key)
	{
	  if (end == 0)
	  {
		  return;
	  }
	  
	  for (int i1 = (end + beg) / 2; i1 > beg;)
	  {
		--i1;
		fix_heap(elems, beg, i1, end, key);
	  }

	  for (int i1 = end; --i1 > beg;)
	  {
		int tmp_el = elems.getItem(beg);
		elems.setItem(beg, elems.getItem(i1));
		elems.setItem(i1, tmp_el);
		fix_heap(elems, beg, beg, i1, key);
	  }

	}
	
	private void make_adjacent_tr_set(int nr_trans, MyArray tr_states, MyArray adj_trans, MyArray adj_ends)
	{
		for(int st = 0; st <= nr_states; ++st )
		{
			adj_ends.setItem(st, 0);
		}
		for( int tr = 1; tr <= nr_trans; ++tr )
		{
			adj_ends.plusOne(tr_states.getItem(tr));
		}
		
		int sum = 0, tmp;
		for( int st = 0; st <= nr_states; ++st )
		{
			tmp = adj_ends.getItem(st);
			adj_ends.setItem(st, sum);
			sum += tmp;
		}
		
		for(int tr = 1;tr<= nr_trans; ++tr )
		{
			int st = tr_states.getItem(tr);
			adj_trans.setItem(adj_ends.getItem(st), tr);
			adj_ends.plusOne(st);
		}
	}

	private void group_l_transitions()
	{
		if(nr_l_trans == 0)
			return;
		
		if(nr_labels > 11*nr_states + 7*nr_l_trans)
		{
			for( int tr = 1; tr <= nr_l_trans; ++tr )
			{
				T.elems.setItem(tr, tr);
			}
			heapsort( T.elems, 1, nr_l_trans + 1, l_labels );
			T.nr_sets = 1;
			
			int lb = l_labels.getItem(T.elems.getItem(1));
			for( int tr = 2; tr <= nr_l_trans; ++tr )
			{
				if(l_labels.getItem(T.elems.getItem(tr)) != lb)
				{
					T.ends.setItem(T.nr_sets, tr);
					++T.nr_sets;
					lb = l_labels.getItem(T.elems.getItem(tr));
				}
			}
			T.ends.setItem(T.nr_sets, nr_l_trans + 1);
		}
		else
		{
			MyArray index = new MyArray();
			index.make_size(nr_labels);
			
			T.nr_sets = 0;
			for(int tr = 1; tr <= nr_l_trans; ++tr )
			{
				int lb = l_labels.getItem(tr);
				int id = index.getItem(lb);
				
				if(id<1 || id>T.nr_sets || T.ends.getItem(id)!= lb)
				{
					id = ++T.nr_sets;
					T.ends.setItem(id, lb);
					index.setItem(lb, id);
					T.elems.setItem(id, 1);
				}
				else
				{
					T.elems.plusOne(id);
				}
			}
			
			T.ends.setItem(1, 1);
			for( int id = 1; id < T.nr_sets; ++id )
			{
				T.ends.setItem(id+1, T.ends.getItem(id)+T.elems.getItem(id));
			}
			
			for( int tr = 1; tr <= nr_l_trans; ++tr )
			{
				int id = index.getItem(l_labels.getItem(tr));
				T.elems.setItem(T.ends.getItem(id), tr);
				T.ends.plusOne(id);
			}
			
			index.del();
		}
		
		T.ends.setItem(0, 1);
		for( int id = 1; id <= T.nr_sets; ++id )
		{
			heapsort( T.elems, T.ends.getItem(id-1), T.ends.getItem(id), l_tails );
		}
	}
	
	
	private void split_according_initial_partition()
	{
		for( int st = 1; st <= nr_states; ++st )
		{
			B.elems.setItem(st, st);
		}
		heapsort( B.elems, 1, nr_states + 1, B.sets );
		for( int lc = 1; lc <= nr_states; ++lc )
		{
			B.locs.setItem(B.elems.getItem(lc), lc);
		}
		
		B.begins.setItem(1, 1);
		B.mids.setItem(1, 1);
		B.nr_sets = 1;
		
		int bl1 = B.sets.getItem(B.elems.getItem(1));
		for( int lc = 2; lc <= nr_states; ++lc )
		{
			int bl2 = B.sets.getItem(B.elems.getItem(lc));
			
			if(bl1 != bl2)
			{
				B.ends.setItem(B.nr_sets, lc);
				++B.nr_sets;
				B.begins.setItem(B.nr_sets, lc);
				B.mids.setItem(B.nr_sets, lc);
				bl1 = bl2;
			}
		}
		
		B.ends.setItem(B.nr_sets, nr_states + 1);
	}
	
	private void update_cords()
	{
		for( ; cord_splitter <= B.nr_sets; ++cord_splitter )
		{
			for(int lc = B.begins.getItem(cord_splitter); lc < B.ends.getItem(cord_splitter);++lc)
			{
				int st = B.elems.getItem(lc);
				for(int pl = in_l_ends.getItem(st-1);pl<in_l_ends.getItem(st);++pl)
				{
					int cd = T.mark(in_l_trans.getItem(pl));
					if(cd != 0)
					{
						touched_sets.add(cd);
					}
				}
			}
			
			while(touched_sets.stack_size() != 0)
			{
				T.split(touched_sets.remove());
			}
		}
	}
	
	
	private void bisim_split()
	{
		for( ; bisim_splitter <= T.nr_sets; ++bisim_splitter )
		{
			for(int lc = T.begins.getItem(bisim_splitter);lc<T.ends.getItem(bisim_splitter);++lc)
			{
				s_counts.plusOne(l_tails.getItem(T.elems.getItem(lc)));
			}
			
			for(int lc = T.begins.getItem(bisim_splitter);lc<T.ends.getItem(bisim_splitter);++lc)
			{
				int tr = T.elems.getItem(lc);
				int st = l_tails.getItem(tr);
				int lntr = links.getItem(tr);
				
				if(s_counts.getItem(st) == t_counts.getItem(lntr))
				{
					int bl = B.mark(st);
					if(bl != 0)
					{
						touched_sets.add(bl);
					}
					s_counts.setItem(st, 0);
				}
			}
			
			
			while(touched_sets.stack_size() != 0)
			{
				B.split(touched_sets.remove());
			}
			
			for(int lc = T.begins.getItem(bisim_splitter);lc<T.ends.getItem(bisim_splitter);++lc)
			{
				int tr = T.elems.getItem(lc);
				int st = l_tails.getItem(tr);
				int lntr = links.getItem(tr);
				
				if(s_counts.getItem(st) >= nr_l_max1)
				{
					links.setItem(tr, s_counts.getItem(st) - nr_l_max1);
				}
				else if(s_counts.getItem(st)>0)
				{
					int bl = B.mark(st);
					if(bl != 0)
						touched_sets.add(bl);
					++nr_counters;
					t_counts.setItem(nr_counters, s_counts.getItem(st));
					t_counts.minus(lntr, s_counts.getItem(st));
					s_counts.setItem(st, nr_counters + nr_l_max1);
					links.setItem(tr, nr_counters);
				}
			}
			
			
			for(int lc = T.begins.getItem(bisim_splitter);lc<T.ends.getItem(bisim_splitter);++lc)
			{
				s_counts.setItem(l_tails.getItem(T.elems.getItem(lc)), 0);
			}
			
			while(touched_sets.stack_size() != 0)
			{
				B.split(touched_sets.remove());
			}
			update_cords();
		}
	}
	
	public void minimize()
	{
//		if(phase != phase_type.sizes_given)
//		{
//			System.out.println("Illeagal input");
//		}
//		if(l_cnt < nr_l_trans)
//		{
//			System.out.println("too few transitions");
//		}
//		
//		if(nr_states == 0)
//		{
//			phase = phase_type.minimized;
//			return;
//		}
//		
//		if(nr_l_trans == 0)
//		{
//			l_tails.make_size( nr_l_trans );
//			l_labels.make_size( nr_l_trans );
//		    l_heads.make_size( nr_l_trans );
//		    T.elems.make_size( nr_l_trans ); 
//		    T.ends.make_size( nr_l_trans );
//		}
		
		T.sets.make_size(nr_l_trans);
		int id = 1;
		for(int lc = 1;lc <= nr_l_trans; ++lc)
		{
			if(lc>= T.ends.getItem(id))
			{
				++id;
			}
			T.sets.setItem(T.elems.getItem(lc), id);
		}
		
		T.locs.make_size(nr_l_trans);
		for(int lc = 1;lc <= nr_l_trans; ++lc)
		{
			T.locs.setItem(T.elems.getItem(lc), lc);
		}
		
		T.begins.make_size(nr_l_trans);
		T.mids.make_size( nr_l_trans );
		if(T.nr_sets != 0)
		{
			T.begins.setItem(1, 1);
			T.mids.setItem(1, 1);
		}
		for(int id2 = 2; id2 <= T.nr_sets; ++id2)
		{
			T.begins.setItem(id2, T.ends.getItem(id2 - 1));
			T.mids.setItem(id2, T.ends.getItem(id2 - 1));
		}
		
		touched_sets.make_size(( nr_states > nr_l_trans ? nr_states : nr_l_trans ) - 1);
		touched_states.make_size( nr_states - 1 );
		
		links.make_size( nr_l_trans );
		t_counts.make_size( nr_l_trans );
		s_counts.make_size( nr_states );
		int prev_tail = 0, prev_label = 0;
		for(int lc = 1;lc <= nr_l_trans; ++lc)
		{
			int tr = T.elems.getItem(lc);
			if(l_tails.getItem(tr) != prev_tail || l_labels.getItem(tr) != prev_label)
			{
				prev_tail = l_tails.getItem(tr);
				prev_label = l_labels.getItem(tr);
				++nr_counters;
				t_counts.setItem(nr_counters, 0);
			}
			
			links.setItem(tr, nr_counters);
			t_counts.plusOne(nr_counters);
		}
		for(int st = 1; st<= nr_states;++st)
		{
			s_counts.setItem(st, 0);
		}
		
		in_l_trans.make_size( nr_l_trans - 1 ); 
		in_l_ends.make_size( nr_states );
		make_adjacent_tr_set( nr_l_trans, l_heads, in_l_trans, in_l_ends );
		
		if( !B.sets.active())
		{
			B.sets.make_size( nr_states );
			for( int st = 1; st <= nr_states; ++st )
			{
				B.sets.setItem(st, 1);
			}
		}
		
		B.elems.make_size( nr_states ); 
		B.locs.make_size( nr_states );  
		B.begins.make_size( nr_states ); 
		B.ends.make_size( nr_states );
		B.mids.make_size( nr_states );
		
		split_according_initial_partition();
		if( B.nr_sets != nr_init_blocks )
		{
			System.out.println("Empty block");	
		}
		
		init_block_borders.make_size( nr_init_blocks );
		init_block_borders.setItem(0, 1);
		for( int bl = 1; bl <= nr_init_blocks; ++bl )
		{
			init_block_borders.setItem(bl, B.ends.getItem(bl));
		}
		
		
		while(bisim_splitter <= T.nr_sets)
		{
			bisim_split();
		}
		
		
		for(int tr = 1; tr <= nr_l_trans; ++tr)
		{
			int st = l_tails.getItem(tr);
			if(st != B.elems.getItem(B.begins.getItem(B.sets.getItem(st))))
			{
				continue;
			}
			if(t_counts.getItem(links.getItem(tr)) == 0)
			{
				continue;
			}
			t_counts.setItem(links.getItem(tr), 0);
			++nr_l_out;
		}
		
//		for( int bl = 1; bl <= B.nr_sets; ++bl )
//		{
//			B.mids.setItem(bl, 0);
//		}
	}
}
