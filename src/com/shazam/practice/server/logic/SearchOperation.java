package com.shazam.practice.server.logic;

import java.util.List;

import com.shazam.practice.shared.model.SongParam;

public interface SearchOperation {

	List<SongParam> getResults(String query) throws Exception;
}
