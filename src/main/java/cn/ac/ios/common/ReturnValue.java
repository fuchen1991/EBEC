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
package cn.ac.ios.common;

public class ReturnValue {
	private boolean result;
	private int touchedPairsNumber;
	private int todoPairsNumber;
	private int rPairsNumber;
	
	public ReturnValue(boolean result, int exp, int todo, int r)
	{
		this.result = result;
		this.touchedPairsNumber = exp;
		this.rPairsNumber = r;
		this.todoPairsNumber = todo;
	}

	public void add(ReturnValue value)
	{
		if(!(this.result && value.result))
		{
			this.result = false;
		}
		this.touchedPairsNumber += value.touchedPairsNumber;
		this.rPairsNumber += value.rPairsNumber;
		this.todoPairsNumber += value.todoPairsNumber;	
	}
	
	public boolean isResult() {
		return result;
	}

	public int getTouchedPairsNumber() {
		return touchedPairsNumber;
	}

	public int getTodoPairsNumber() {
		return todoPairsNumber;
	}

	public int getrPairsNumber() {
		return rPairsNumber;
	}
	
	
}
