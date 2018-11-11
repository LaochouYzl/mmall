package com.mmall.service;

import java.util.List;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

public interface ICategoryService {
	
	ServerResponse<String> addCategory(String categoryName, Integer parentId);
	
	ServerResponse<String> updateCategoryName(Integer categoryId, String categoryName);
	
	ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);
	
	ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);

}
