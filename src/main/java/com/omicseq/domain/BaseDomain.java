package com.omicseq.domain;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class BaseDomain implements Serializable {
	
	private static final long serialVersionUID = 7790145014352426472L;
	private String _id;
	
	public String get_id() {
    	return _id;
    }

	public void set_id(String _id) {
    	this._id = _id;
    }

	@Override
	public String toString() {
	    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
