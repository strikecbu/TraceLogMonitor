/*
 * Copyright (c) 2009-2012 International Integrated System, Inc.
 * 11F, No.133, Sec.4, Minsheng E. Rd., Taipei, 10574, Taiwan, R.O.C.
 * All Rights Reserved.
 *
 * Licensed Materials - Property of International Integrated System, Inc.
 *
 * This software is confidential and proprietary information of
 * International Integrated System, Inc. ("Confidential Information").
 */
package com.citi.util;

/**
 * 
 * <p>
 * 字串處理.
 * </p>
 * 
 * 
 * @author malo
 * @version <ul>
 *          <li>2010/6/1,iristu,新增trimLineSeparator(),處理字串中斷行符號.
 *          <li>2011/9/7,SunkistWang,update checkRegularMatch(), 避開NPE.
 *          <li>2011/11/1,rodeschen,from cap
 *          </ul>
 */
public class CapString {

	/**
	 * 判斷字串是否為空白.
	 *
	 * @param s
	 *            字串
	 * @return boolean
	 */
	public static boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

}
