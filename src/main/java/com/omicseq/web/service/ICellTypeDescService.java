package com.omicseq.web.service;

import java.util.Map;

public interface ICellTypeDescService {

	Map<String, String> findAll();

	String findByCellRegex(String cell);
}
