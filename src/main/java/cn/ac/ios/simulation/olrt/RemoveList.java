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
import java.util.List;

public class RemoveList {

	private List<StateElement> remove;
	
	public RemoveList()
	{
		this.remove = new ArrayList<StateElement>();
	}
	
	public RemoveList(List<StateElement> remove)
	{
		this.remove = remove;
	}
	
	public RemoveList(RemoveList remove)
	{
		this.remove = new ArrayList<StateElement>(remove.remove);
	}
	
	public List<StateElement> getList()
	{
		return remove;
	}
	
	public int size()
	{
		return this.remove.size();
	}
	
	public boolean isEmpty()
	{
		return remove.isEmpty();
	}
	
	public void add(StateElement elem)
	{
		this.remove.add(elem);
	}
}
