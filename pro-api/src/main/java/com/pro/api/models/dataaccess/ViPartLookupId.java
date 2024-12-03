package com.pro.api.models.dataaccess;

import java.io.Serializable;
import java.util.Objects;

public class ViPartLookupId implements Serializable {
	private String phonetype;
	private Integer upid;
	// private Integer partcontactsid;
	public String getPhonetype() {
		return phonetype;
	}
	public void setPhonetype(String phonetype) {
		this.phonetype = phonetype;
	}
	public Integer getUpid() {
		return upid;
	}
	public void setUpid(Integer upid) {
		this.upid = upid;
	}
//	public Integer getPartcontactsid() {
//		return partcontactsid;
//	}
//	public void setPartcontactsid(Integer partcontactsid) {
//		this.partcontactsid = partcontactsid;
//	}

}
