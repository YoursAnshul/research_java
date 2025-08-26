package com.dimetyd.bot.model;

public enum JobType {
	ASIN ("ASIN");
	
   private final String name;       

   private JobType(String s) {
       name = s;
   }

   public boolean equalsName(String otherName) {
       // (otherName == null) check is not needed because name.equals(null) returns false 
       return name.equals(otherName);
   }

   public String toString() {
      return this.name;
   }
}
