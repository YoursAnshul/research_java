package com.dimetyd.bot.helper;

public  class GlobalSession {
	
	private static GlobalSessionName session= null;
	
	public static GlobalSessionName getGlobalSession()
	{	
		if(session==null)
			session = new GlobalSessionName();
		
		
		
		
		return session;
	}
}
