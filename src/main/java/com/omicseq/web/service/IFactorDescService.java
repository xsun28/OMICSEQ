package com.omicseq.web.service;

import java.util.Map;

public interface IFactorDescService {

	Map<String, String> findAll();

	String findByFactorRegex(String factor);
}
